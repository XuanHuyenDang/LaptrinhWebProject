package vn.flower.controllers;

import java.io.File; // Thêm nếu thiếu
import java.nio.file.Files; // Thêm nếu thiếu
import java.nio.file.Path; // Thêm nếu thiếu
import java.nio.file.Paths; // Thêm nếu thiếu
import java.nio.file.StandardCopyOption; // Thêm nếu thiếu
import java.util.List;
import java.util.ArrayList; // Đảm bảo import ArrayList

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute; // Thêm nếu thiếu
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import vn.flower.entities.Category;
import vn.flower.entities.CustomerDTO;
import vn.flower.entities.Order;
import vn.flower.entities.Product;
import vn.flower.entities.Review;
import vn.flower.repositories.AccountRepository;
import vn.flower.repositories.AdminOrderRepository;
import vn.flower.repositories.CategoryRepository;
import vn.flower.repositories.ProductRepository;
import vn.flower.repositories.ReviewRepository;
import vn.flower.services.CategoryService;
import vn.flower.services.CustomerService;
import java.time.LocalDateTime;
import vn.flower.entities.OrderReturnRequest;
import vn.flower.services.OrderReturnService;
// import vn.flower.entities.OrderReturnRequest.ReturnStatus; // Không cần nếu không dùng trực tiếp

@Controller
@RequestMapping("/admin")
public class AdminController {

	// --- Các @Autowired ---
	@Autowired
	private CategoryService categoryService;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private AdminOrderRepository orderRepository; // Sử dụng Repo trực tiếp

	@Autowired
	private CustomerService customerService;

	@Autowired
	private ReviewRepository reviewRepository;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private OrderReturnService orderReturnService;

    // --- Các phương thức quản lý trả hàng (giữ nguyên) ---
    @GetMapping("/returns")
    public String showReturnRequests(Model model) {
        model.addAttribute("pendingRequests", orderReturnService.getPendingReturnRequests());
        return "admin/admin-returns";
    }

    @GetMapping("/returns/{requestId}")
    public String showReturnRequestDetail(@PathVariable Long requestId, Model model) {
        try {
            OrderReturnRequest request = orderReturnService.getReturnRequestById(requestId);
            model.addAttribute("returnRequest", request);
            return "admin/admin-return-detail";
        } catch (Exception e) {
            return "redirect:/admin/returns?error=NotFound";
        }
    }

    @PostMapping("/returns/process")
    public String processReturnRequest(@RequestParam Long requestId,
                                       @RequestParam boolean approve,
                                       @RequestParam String adminNotes,
                                       RedirectAttributes ra) {
        try {
            orderReturnService.processReturnRequest(requestId, approve, adminNotes);
            ra.addFlashAttribute("success", "Đã xử lý yêu cầu thành công.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi xử lý yêu cầu: " + e.getMessage());
        }
        return "redirect:/admin/returns";
    }

	// --- Tong quan (giữ nguyên) ---
	@GetMapping("/dashboard")
	public String dashboard(Model model) {
		long totalOrders = orderRepository.count();
		long totalCustomers = accountRepository.countByRole("customer");
		long totalComments = reviewRepository.count();
		var latestOrders = orderRepository.findTop5ByOrderByOrderDateDesc();

		model.addAttribute("totalOrders", totalOrders);
		model.addAttribute("totalCustomers", totalCustomers);
		model.addAttribute("totalComments", totalComments);
		model.addAttribute("latestOrders", latestOrders);

		return "admin/admin-dashboard";
	}

	// --- San pham (giữ nguyên) ---
	@GetMapping("/products")
	public String listProducts(@RequestParam(value = "category", required = false) Long categoryId,
			@RequestParam(value = "keyword", required = false) String keyword, Model model) {
		List<Category> categories = categoryRepository.findAll();
		List<Product> products;
		if (categoryId != null) {
			if (keyword != null && !keyword.isBlank()) {
				products = productRepository.findByCategory_IdAndProductNameContainingIgnoreCase(categoryId, keyword);
			} else {
				products = productRepository.findByCategoryId(categoryId);
			}
			model.addAttribute("selectedCategory", categoryRepository.findById(categoryId).orElse(null));
		} else {
			if (keyword != null && !keyword.isBlank()) {
				products = productRepository.findByProductNameContainingIgnoreCase(keyword);
			} else {
				products = productRepository.findAll();
			}
			model.addAttribute("selectedCategory", null);
		}
		model.addAttribute("categories", categories);
		model.addAttribute("products", products);
		model.addAttribute("keyword", keyword);
		model.addAttribute("categoryId", categoryId);
		return "admin/admin-products";
	}

	@GetMapping("/products/add")
	public String showAddProductForm(@RequestParam("categoryId") Long categoryId, Model model) {
		Category category = categoryService.findById(categoryId);
		Product product = new Product();
		product.setCategory(category);
		model.addAttribute("product", product);
		model.addAttribute("category", category);
		return "admin/admin-add-product";
	}

	@PostMapping("/products/add")
	public String saveProduct(@ModelAttribute("product") Product product,
			@RequestParam("imageFile") MultipartFile imageFile) {
		try {
			Category category = categoryRepository.findById(Long.valueOf(product.getCategory().getId())).orElse(null);
			if (category != null && !imageFile.isEmpty()) { // Kiểm tra category null
				String categoryName = normalizeCategoryName(category.getCategoryName());
				String fileName = imageFile.getOriginalFilename();
				String uploadDir = "src/main/resources/static/assets/images/" + categoryName; // Chỉnh đường dẫn lưu file ảnh
				File dir = new File(uploadDir);
				if (!dir.exists()) dir.mkdirs();
				Path filePath = Paths.get(uploadDir, fileName);
				Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
				product.setImageUrl("images/" + categoryName + "/" + fileName); // Chỉnh đường dẫn lưu vào DB
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		productRepository.save(product);
		return "redirect:/admin/products?category=" + product.getCategory().getId();
	}

	@GetMapping("/products/edit/{id}")
	public String editForm(@PathVariable("id") Integer id, Model model) {
		Product p = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm có ID: " + id));
		List<Category> categories = categoryRepository.findAll();
		model.addAttribute("product", p);
		model.addAttribute("categories", categories);
		return "admin/admin-edit-product";
	}

	@PostMapping("/products/update")
	public String editProduct(@ModelAttribute("product") Product product,
			@RequestParam("imageFile") MultipartFile imageFile) {
		try {
			Category category = categoryRepository.findById(Long.valueOf(product.getCategory().getId())).orElse(null);
			product.setCategory(category);
			if (category != null && !imageFile.isEmpty()) { // Kiểm tra category null
				String categoryName = normalizeCategoryName(category.getCategoryName());
				String fileName = imageFile.getOriginalFilename();
				String uploadDir = "src/main/resources/static/assets/images/" + categoryName; // Chỉnh đường dẫn lưu file ảnh
				File dir = new File(uploadDir);
				if (!dir.exists()) dir.mkdirs();
				Path filePath = Paths.get(uploadDir, fileName);
				Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
				product.setImageUrl("images/" + categoryName + "/" + fileName); // Chỉnh đường dẫn lưu vào DB
			} else {
				Product oldProduct = productRepository.findById(product.getId()).orElse(null);
				if (oldProduct != null) {
					product.setImageUrl(oldProduct.getImageUrl());
				}
			}
			productRepository.save(product);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/admin/products?category=" + product.getCategory().getId();
	}

	@GetMapping("/products/delete/{id}")
	public String deleteProduct(@PathVariable("id") Integer id) {
		Product product = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm có ID: " + id));
		Integer categoryId = (product.getCategory() != null) ? product.getCategory().getId() : null;
		productRepository.deleteById(id);
		if (categoryId != null) {
			return "redirect:/admin/products?category=" + categoryId;
		} else {
			return "redirect:/admin/products";
		}
	}

	private String normalizeCategoryName(String input) {
		if (input == null) return "unknown";
		String normalized = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
		normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
		normalized = normalized.replaceAll("[^a-zA-Z0-9\\-]", "_"); // Thay ký tự đặc biệt bằng gạch dưới
        normalized = normalized.toLowerCase(); // Chuyển thành chữ thường
		return normalized;
	}


	// === PHƯƠNG THỨC QUẢN LÝ ĐƠN HÀNG ĐÃ CẬP NHẬT ===
	@GetMapping("/orders")
	public String orders(@RequestParam(required = false) String status,
                         @RequestParam(required = false) String paymentMethod, // Giữ param lọc paymentMethod
                         // Bỏ param paid
                         Model model) {

        List<Order> orders;
        String currentFilterDisplay = "Tất cả"; // Text hiển thị bộ lọc hiện tại

		if (paymentMethod != null && !paymentMethod.isEmpty()) {
            // Lọc theo paymentMethod (ví dụ: VNPAY)
            orders = orderRepository.findByPaymentMethodIgnoreCase(paymentMethod);
            currentFilterDisplay = paymentMethod.toUpperCase(); // Hiển thị tên phương thức
        } else if (status != null && !status.isEmpty()) {
			// Lọc theo trạng thái như cũ
            orders = orderRepository.findByStatus(status);
			currentFilterDisplay = status;
		} else {
            // Không lọc gì cả, lấy tất cả
			orders = orderRepository.findAll();
		}

		model.addAttribute("orders", orders);
		model.addAttribute("selectedStatus", currentFilterDisplay); // Sử dụng biến hiển thị bộ lọc

		// Tính toán số lượng cho các card
		model.addAttribute("countDangXuLy", orderRepository.countByStatus("Đang xử lý"));
		model.addAttribute("countDangGiao", orderRepository.countByStatus("Đang giao"));
		model.addAttribute("countHoanTat", orderRepository.countByStatus("Hoàn tất"));
        // Đếm tất cả đơn VNPAY
        model.addAttribute("countVnpay", orderRepository.countByPaymentMethodIgnoreCase("VNPAY"));

		return "admin/admin-orders";
	}

    @GetMapping("/orders/{id}")
	public String viewOrderDetail(@PathVariable("id") Long id, Model model) {
		Order order = orderRepository.findById(id).orElse(null);
        if (order == null) {
             return "redirect:/admin/orders?error=notfound";
        }
		model.addAttribute("order", order);
		return "admin/admin-order-detail";
	}

    @PostMapping("/orders/update-status")
	public String updateOrderStatus(@RequestParam("orderId") Long orderId,
                                    @RequestParam("status") String status,
			                        RedirectAttributes redirectAttributes) {
		Order order = orderRepository.findById(orderId).orElse(null);
		if (order != null) {
             if (order.getReturnRequest() != null &&
                 List.of("Yêu cầu trả hàng", "Đã trả hàng", "Đã từ chối trả hàng").contains(order.getStatus())) {
                  redirectAttributes.addFlashAttribute("error", "Không thể cập nhật trạng thái đơn hàng đang/đã yêu cầu trả hàng.");
                  return "redirect:/admin/orders/" + orderId;
             }

            // Logic ghi nhận ngày hoàn tất
            if ("Hoàn tất".equals(status) && order.getCompletedDate() == null) {
                order.setCompletedDate(LocalDateTime.now());
            }

			order.setStatus(status);
			orderRepository.save(order);
			redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công!");
		} else {
			redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng!");
		}
		return "redirect:/admin/orders/" + orderId;
	}

	// --- Danh mục (giữ nguyên) ---
	@GetMapping("/categories")
	public String listCategories(Model model) {
		model.addAttribute("categories", categoryRepository.findAll());
		model.addAttribute("category", new Category());
		return "admin/admin-categories";
	}

	@PostMapping("/categories/save")
	public String saveCategory(@ModelAttribute("category") Category category) {
		categoryService.save(category);
		return "redirect:/admin/categories";
	}

	@GetMapping("/categories/edit/{id}")
	public String editCategory(@PathVariable("id") Long id, Model model) {
		Category category = categoryService.findById(id);
		if (category == null) {
			throw new IllegalArgumentException("Invalid category Id:" + id);
		}
		model.addAttribute("category", category);
		model.addAttribute("categories", categoryService.findAll());
		return "admin/admin-categories";
	}

	@GetMapping("/categories/delete/{id}")
	public String deleteCategory(@PathVariable("id") Long id) {
		categoryService.deleteById(id);
		return "redirect:/admin/categories";
	}

	// --- Khach hang (giữ nguyên) ---
	@GetMapping("/customers")
	public String showCustomers(Model model) {
		List<CustomerDTO> customers = customerService.getAllCustomers();
		model.addAttribute("customers", customers);
		return "admin/admin-customers";
	}

	@GetMapping("customers/{id}/orders")
	public String customerOrders(@PathVariable Integer id, Model model) {
		List<Order> orders = orderRepository.findByAccountId(id);
		model.addAttribute("orders", orders);
        accountRepository.findById(Long.valueOf(id)).ifPresent(account -> {
            model.addAttribute("customerName", account.getFullName());
        });
		return "admin/admin-customer-orders";
	}

	// --- Danh gia (giữ nguyên) ---
	@GetMapping("/comments")
	public String listComments(@RequestParam(value = "keyword", required = false) String keyword,
			@RequestParam(value = "filterBy", required = false) String filterBy, Model model) {
		List<Review> reviews;
		if (keyword != null && !keyword.isEmpty()) {
			reviews = reviewRepository.findByProductProductNameContainingIgnoreCaseOrAccountFullNameContainingIgnoreCase(keyword, keyword);
		} else if ("product".equals(filterBy)) {
			reviews = reviewRepository.findAllByOrderByProductProductNameAsc();
		} else if ("customer".equals(filterBy)) {
			reviews = reviewRepository.findAllByOrderByAccountFullNameAsc();
		} else {
			reviews = reviewRepository.findAll();
		}
		model.addAttribute("reviews", reviews);
		model.addAttribute("keyword", keyword);
		model.addAttribute("filterBy", filterBy);
		return "admin/admin-comments";
	}

	@PostMapping("/comments/delete/{id}")
	public String deleteReview(@PathVariable("id") Integer id) {
		reviewRepository.deleteById(id);
		return "redirect:/admin/comments";
	}

}