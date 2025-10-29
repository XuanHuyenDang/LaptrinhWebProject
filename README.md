# 🌸 **FLORIO FLOWER SHOP MANAGEMENT SYSTEM** 🌸  
> 💐 *Ứng dụng web bán hoa trực tuyến được phát triển bằng Spring Boot MVC, hướng tới sự hiện đại, tiện ích và bảo mật.*  

---

## 🏫 Thông tin dự án
- **Tên đề tài:** Xây dựng website bán hoa Florio bằng Spring Boot  
- **Môn học:** Lập trình Web  
- **Trường:** Đại học Sư phạm Kỹ thuật TP. Hồ Chí Minh – Khoa CNTT  
- **Giảng viên hướng dẫn:** ThS. Nguyễn Hữu Trung  
- **Nhóm thực hiện:** Nhóm 07  
  - Bùi Quốc Hậu – 23110211  
  - Triệu Phúc Hiếu – 23110217  
  - Đặng Xuân Huyền – 23110232  
  - Nguyễn Trần Quốc Thi – 23110331  
- **Học kỳ:** 1 – Năm học 2025–2026  

---

## 🌷 Giới thiệu dự án

> 💖 *"Hoa là lời nói của trái tim – Florio là nơi để những lời nói ấy nở rộ!"* 🌹  

**Florio Flower Shop** là một ứng dụng web thương mại điện tử bán hoa trực tuyến.  
Người dùng có thể duyệt sản phẩm, thêm vào giỏ hàng, đặt hoa, thanh toán, chat với cửa hàng và theo dõi đơn hàng của mình.  
Hệ thống hỗ trợ quản trị viên trong việc quản lý sản phẩm, danh mục, khách hàng, khuyến mãi và thống kê doanh thu.

Trang web được xây dựng theo **mô hình MVC** trên nền **Spring Boot 3**, kết hợp **Thymeleaf**, **Bootstrap 5**, **SQL Server** và **Spring Security** để đảm bảo an toàn và hiệu năng cao.

---

## 💐 Mục tiêu & Phạm vi

### 🎯 Mục tiêu
- Xây dựng website bán hoa trực tuyến có khả năng:
  - Hiển thị và tìm kiếm sản phẩm
  - Quản lý đơn hàng, người dùng, khuyến mãi, phản hồi khách hàng
  - Tích hợp xác thực OTP qua email
  - Hỗ trợ **chat thời gian thực** bằng WebSocket
- Áp dụng kiến thức lập trình web, cơ sở dữ liệu, mô hình MVC và các công nghệ Java Spring hiện đại.

### 📦 Phạm vi
- Hệ thống bao gồm 2 vai trò chính: **Admin** và **User**.  
- Cung cấp đầy đủ chức năng thương mại điện tử cơ bản.  
- Hoạt động trong môi trường thử nghiệm (localhost).  
- Tích hợp cổng thanh toán thực tế (VNPay, Momo).  

---

## 🌺 Tính năng hệ thống

### 👑 Dành cho **Quản trị viên (Admin)**
- Quản lý danh mục và sản phẩm (CRUD)  
- Quản lý người dùng & phân quyền  
- Quản lý đơn hàng và yêu cầu trả hàng  
- Quản lý chương trình khuyến mãi và mã giảm giá  
- Trả lời tin nhắn khách hàng qua chat  
- Xem thống kê doanh thu, sản phẩm bán chạy  
- Bảo mật hệ thống với **Spring Security & JWT**

### 💕 Dành cho **Khách hàng (User)**
- Đăng ký tài khoản với **xác thực OTP qua email**  
- Đăng nhập / đăng xuất / quên mật khẩu  
- Tìm kiếm, xem chi tiết sản phẩm  
- Thêm sản phẩm vào **giỏ hàng**  
- Đặt hàng, thanh toán (COD, VNPay, Momo - mô phỏng)  
- Quản lý thông tin cá nhân và lịch sử mua hàng  
- Chat trực tuyến với cửa hàng qua WebSocket  
- Sử dụng mã giảm giá, đánh giá sản phẩm  

---

## 💻 Công nghệ sử dụng

| Thành phần | Công nghệ |
|-------------|------------|
| **Backend** | Spring Boot 3, Spring MVC, Spring Data JPA |
| **Frontend** | Thymeleaf, Bootstrap 5, HTML5, CSS3, JS |
| **Bảo mật** | Spring Security |
| **Realtime** | WebSocket |
| **Database** | Microsoft SQL Server |
| **Build Tool** | Maven |
| **IDE** | IntelliJ IDEA / Eclipse / Spring Tool Suite |
| **Ngôn ngữ** | Java 17+ |

---

## 🗄️ Cấu trúc cơ sở dữ liệu

Cơ sở dữ liệu Florio được thiết kế theo **mô hình quan hệ (RDBMS)** gồm 12 bảng chính:

| STT | Bảng | Mô tả |
|-----|------|-------|
| 1 | `Accounts` | Lưu thông tin người dùng, vai trò, mật khẩu mã hóa |
| 2 | `Categories` | Danh mục sản phẩm (Bó hoa, Giỏ hoa, Chậu hoa, v.v.) |
| 3 | `Products` | Thông tin chi tiết từng sản phẩm |
| 4 | `Orders` | Thông tin đơn hàng, người nhận, phương thức thanh toán |
| 5 | `OrderDetails` | Chi tiết sản phẩm trong từng đơn hàng |
| 6 | `Wishlist` | Danh sách sản phẩm yêu thích của người dùng |
| 7 | `Reviews` | Đánh giá, bình luận của khách hàng |
| 8 | `DiscountCodes` | Mã giảm giá (voucher) |
| 9 | `Promotions` | Chương trình khuyến mãi toàn hệ thống |
| 10 | `Product_Promotions` | Liên kết giữa sản phẩm và khuyến mãi |
| 11 | `OrderReturnRequests` | Yêu cầu trả hàng, hoàn tiền |
| 12 | `ChatMessages` | Tin nhắn realtime giữa user và admin |

### ⚙️ Quan hệ giữa các bảng
- **1-N:** `Accounts` – `Orders`, `Orders` – `OrderDetails`  
- **N-N:** `Products` – `Promotions`, `Accounts` – `Wishlist`  
- **1-1:** `Orders` – `OrderReturnRequests`  

---

## 🧱 Cấu trúc thư mục dự án

```
Florio/
 ├── src/
 │   ├── main/
 │   │   ├── java/vn/flower/
 │   │   │   ├── controllers/      → Xử lý request, mapping URL
 │   │   │   ├── models/           → Entity, DTO, JPA mapping
 │   │   │   ├── repositories/     → Giao tiếp CSDL (Spring Data JPA)
 │   │   │   ├── services/         → Xử lý nghiệp vụ, logic hệ thống
 │   │   │   └── config/           → Cấu hình bảo mật, WebSocket, JWT
 │   │   ├── resources/
 │   │   │   ├── templates/        → Giao diện Thymeleaf (.html)
 │   │   │   ├── static/           → CSS, JS, images
 │   │   │   └── application.properties
 │   └── test/                     → Unit Test
 ├── pom.xml                       → Cấu hình Maven
 └── README.md
```

---

## ⚙️ Hướng dẫn cài đặt & chạy

### 🔧 Yêu cầu môi trường
| Thành phần | Yêu cầu |
|-------------|----------|
| Java | 17 hoặc cao hơn |
| Spring Boot | 3.x |
| SQL Server | 2019 trở lên |
| Maven | 4.0+ |
| IDE | IntelliJ / Eclipse / STS |

### 🪴 Các bước cài đặt
1. Clone dự án:  
   ```bash
   git clone https://github.com/XuanHuyenDang/LaptrinhWebProject.git
   cd flowerShop
   ```
2. Tạo database trong SQL Server
3. Cấu hình file `application.properties` (Mẫu):
   ```
   spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=Florio
   spring.datasource.username=sa
   spring.datasource.password=your_password
   spring.jpa.hibernate.ddl-auto=update
   ```
4. Chạy ứng dụng:
   ```bash
   mvn spring-boot:run
   ```
5. Mở trình duyệt:  
   👉 `http://localhost:8080`

---

## 🎨 Giao diện chính

| Vai trò | Trang tiêu biểu |
|----------|----------------|
| **Guest** | Trang chủ, Giới thiệu, Đăng nhập, Đăng ký OTP |
| **User** | Trang sản phẩm, Giỏ hàng, Thanh toán, Lịch sử mua hàng, Chat |
| **Admin** | Dashboard, Quản lý sản phẩm, Đơn hàng, Khách hàng, Chat |

Giao diện thiết kế với **Bootstrap 5**, tối ưu trải nghiệm người dùng, tone màu pastel nhẹ nhàng, tông hồng chủ đạo 💗.  
Các form được kiểm tra đầu vào kỹ lưỡng và có thông báo lỗi rõ ràng.

---

## 🔔 Chức năng Realtime & Bảo mật
- **WebSocket:** hỗ trợ chat trực tuyến giữa khách hàng và admin.  
- **Spring Security:** chặn truy cập trái phép, mã hóa mật khẩu, bảo vệ endpoint.  
- **OTP Email Service:** xác nhận tài khoản qua mã OTP gửi tới email người dùng.  

---

## 🧪 Kết quả & Kiểm thử

### ✅ Kết quả đạt được
- Hệ thống chạy ổn định, đầy đủ các chức năng nghiệp vụ.  
- Giao diện trực quan, tốc độ phản hồi nhanh.  
- Tích hợp thành công WebSocket và JWT.  
- Dữ liệu đồng nhất giữa giao diện và CSDL.  

### 🧾 Kiểm thử
- Đăng ký, đăng nhập, OTP hoạt động ổn định  
- Giỏ hàng, thanh toán, mã giảm giá hiển thị đúng  
- Admin quản lý được sản phẩm, đơn hàng, khách hàng  
- Tính năng chat và trả hàng hoạt động đúng luồng  

---

## 🏁 Kết luận & Hướng phát triển

### Ưu điểm
- Cấu trúc chuẩn, dễ mở rộng  
- Giao diện đẹp, thân thiện  
- Hệ thống bảo mật, dữ liệu thống nhất  

### Nhược điểm
- Chưa có mobile app  

### Hướng phát triển
- Tích hợp **Dịch vụ vận chuyển**  
- Xây dựng **ứng dụng di động Florio App (Flutter/React Native)**  
- Cải tiến **Dashboard thống kê bằng Chart.js hoặc Power BI**  
- Triển khai website thực tế trên **AWS / Azure / VPS Việt Nam**  

---

## 📚 Tài liệu tham khảo
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)  
- [Spring Security Guide](https://spring.io/projects/spring-security)  
- [Thymeleaf Template Engine](https://www.thymeleaf.org/)  
- [Bootstrap 5 Documentation](https://getbootstrap.com/)  
- [SQL Server Docs](https://learn.microsoft.com/en-us/sql/sql-server/)  

---

<p align="center">
🌸💐🌷 *Florio Flower Shop – Nơi gửi gắm yêu thương qua từng cánh hoa!* 🌺🌻🌼  
</p>
