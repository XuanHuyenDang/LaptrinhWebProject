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
Hệ thống hỗ trợ:
- Quản lý sản phẩm (hoa, lẵng hoa, bó hoa,…)
- Quản lý đơn hàng & thanh toán
- Phân quyền người dùng (Admin, Khách hàng)
- Quản lý thông tin khách hàng, đánh giá sản phẩm

Dự án được phát triển theo mô hình **3 lớp (Controller - Service - Repository)** trên nền **Spring Boot**, sử dụng **Thymeleaf** làm công cụ template và **MySQL** làm cơ sở dữ liệu chính.

---

## 🌼 Demo giao diện

| Trang chủ | Danh sách sản phẩm | Giỏ hàng | Quản lý Admin |
|------------|-------------------|-----------|----------------|
| ![Home](docs/screens/home.png) | ![Products](docs/screens/products.png) | ![Cart](docs/screens/cart.png) | ![Admin](docs/screens/admin.png) |

> 💡 *Ảnh demo được đặt trong thư mục `docs/screens/`.*

---

## 🏗️ Kiến trúc hệ thống

```mermaid
flowchart TD
    A[Client Browser] -->|HTTP Request| B(Spring Controller)
    B --> C[Service Layer]
    C --> D[Repository (JPA)]
    D --> E[(MySQL Database)]
    C --> F[Spring Security]
    B --> G[Thymeleaf Templates]
