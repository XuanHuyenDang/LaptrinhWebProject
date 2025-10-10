<h1 align="center">🌸 Flower Shop Management System</h1>
<h3 align="center">Ứng dụng Web bán hoa trực tuyến phát triển bằng Spring Boot</h3>

---

## 📘 Mục lục
- [1. Giới thiệu](#-giới-thiệu)
- [2. Demo giao diện](#-demo-giao-diện)
- [3. Kiến trúc hệ thống](#-kiến-trúc-hệ-thống)
- [4. Tính năng chính](#-tính-năng-chính)
- [5. Công nghệ sử dụng](#-công-nghệ-sử-dụng)
- [6. Cài đặt và chạy dự án](#-cài-đặt-và-chạy-dự-án)
- [7. Cấu trúc thư mục](#-cấu-trúc-thư-mục)
- [8. API endpoints](#-api-endpoints)
- [9. Định hướng phát triển](#-định-hướng-phát-triển)
- [10. Tác giả](#-tác-giả)
- [11. Giấy phép](#-giấy-phép)

---

## 🌸 Giới thiệu

**Flower Shop Management System** là một nền tảng **thương mại điện tử mini** cho phép người dùng đặt mua các sản phẩm hoa tươi trực tuyến.  
Hệ thống bao gồm các chức năng quản lý sản phẩm, giỏ hàng, đơn hàng, người dùng và quyền truy cập (Admin - User).  
Dự án được xây dựng dựa trên mô hình **3 lớp (Controller - Service - Repository)**, sử dụng **Spring Boot** làm backend, **Thymeleaf** làm frontend, và **MySQL** để lưu trữ dữ liệu.

---

## 🌼 Demo giao diện

| Trang chủ | Danh sách sản phẩm | Giỏ hàng | Quản lý Admin |
|------------|-------------------|-----------|----------------|
| ![Home](docs/screens/home.png) | ![Products](docs/screens/products.png) | ![Cart](docs/screens/cart.png) | ![Admin](docs/screens/admin.png) |

> 💡 *Ảnh demo nằm trong thư mục `docs/screens/`.*

---

## 🏗️ Kiến trúc hệ thống

Hệ thống được thiết kế theo mô hình **MVC (Model - View - Controller)**:
- **Controller Layer**: Tiếp nhận request và điều hướng dữ liệu giữa view & service.
- **Service Layer**: Xử lý logic nghiệp vụ (quản lý sản phẩm, đơn hàng, người dùng, thanh toán,…).
- **Repository Layer**: Tương tác với database thông qua **Spring Data JPA**.
- **Security Layer**: Quản lý xác thực, phân quyền, và bảo vệ route bằng **Spring Security**.
- **View Layer**: Hiển thị giao diện người dùng với **Thymeleaf + Bootstrap**.

---

## ✨ Tính năng chính

### 👩‍💼 Admin
- Quản lý danh mục và sản phẩm hoa (thêm, sửa, xóa, tìm kiếm).
- Quản lý người dùng và vai trò.
- Quản lý đơn hàng & trạng thái giao hàng.
- Thống kê doanh thu, sản phẩm bán chạy.
- Cập nhật thông tin cửa hàng.

### 🌷 Khách hàng
- Đăng ký và đăng nhập tài khoản.
- Duyệt danh mục và tìm kiếm sản phẩm.
- Thêm sản phẩm vào giỏ hàng, thanh toán đơn hàng.
- Xem lịch sử giao dịch.
- Đánh giá sản phẩm và gửi phản hồi.

### 🔒 Hệ thống
- Mã hóa mật khẩu bằng `BCryptPasswordEncoder`.
- Xác thực & phân quyền tự động với Spring Security.
- Trang đăng nhập/đăng ký riêng biệt cho người dùng.

---

## 🧰 Công nghệ sử dụng

| Thành phần | Công nghệ |
|-------------|------------|
| **Ngôn ngữ** | Java 17 |
| **Framework** | Spring Boot (MVC, Data JPA, Security, Thymeleaf) |
| **Frontend** | HTML, CSS, Bootstrap, JavaScript |
| **CSDL** | MySQL / SQL Server |
| **ORM** | Hibernate |
| **Build tool** | Maven |
| **Server** | Embedded Tomcat |
| **Testing** | JUnit 5 |
| **IDE khuyên dùng** | IntelliJ IDEA / Eclipse / STS |

