package vn.flower.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.flower.services.ProductService;

@Controller
public class HomeController {

    private final ProductService productService;

    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping({"/", "/index"})
    public String index(Model model) {
        model.addAttribute("topSoldProducts", productService.getTop10BestSellers());
        model.addAttribute("latestProducts", productService.getLatest5Products());
        model.addAttribute("saleProducts",     productService.getTop10SaleProducts());
        return "index"; // templates/index.html
    }
}
