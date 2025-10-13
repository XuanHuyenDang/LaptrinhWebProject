--------------------------------------------------------------------------------
-- TỔNG QUAN KỊCH BẢN
-- I.   KHỞI TẠO CƠ SỞ DỮ LIỆU
-- II.  XÓA CÁC BẢNG HIỆN TẠI (ĐỂ CHẠY LẠI SCRIPT NẾU CẦN)
-- III. TẠO CẤU TRÚC CÁC BẢNG THEO ĐÚNG THỨ TỰ PHỤ THUỘC
-- IV.  CHÈN DỮ LIỆU MẪU
--------------------------------------------------------------------------------

--------------------------------------------------------------------------------
-- I. KHỞI TẠO CƠ SỞ DỮ LIỆU
--------------------------------------------------------------------------------
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'FlowerShopDB')
BEGIN
    CREATE DATABASE FlowerShopDB;
END
GO

USE FlowerShopDB;
GO

--------------------------------------------------------------------------------
-- II. XÓA CÁC BẢNG HIỆN TẠI (Chạy từ dưới lên để không vi phạm khóa ngoại)
--------------------------------------------------------------------------------
DROP TABLE IF EXISTS ChatMessages;
DROP TABLE IF EXISTS OrderDetails;
DROP TABLE IF EXISTS Orders;
DROP TABLE IF EXISTS Reviews;
DROP TABLE IF EXISTS Product_Promotions;
DROP TABLE IF EXISTS Product_Attributes;
DROP TABLE IF EXISTS Promotions;
DROP TABLE IF EXISTS Products;
DROP TABLE IF EXISTS Categories;
DROP TABLE IF EXISTS Accounts;
GO

--------------------------------------------------------------------------------
-- III. TẠO CẤU TRÚC CÁC BẢNG (Sắp xếp theo thứ tự ưu tiên)
--------------------------------------------------------------------------------

-- Bảng 1: Accounts - Lưu trữ tài khoản khách hàng và quản trị
PRINT 'Creating table: Accounts...';
CREATE TABLE Accounts (
    Id INT PRIMARY KEY IDENTITY(1,1),
    FullName NVARCHAR(100) NOT NULL,
    Email VARCHAR(100) UNIQUE NOT NULL,
    PhoneNumber VARCHAR(20),
    [Address] NVARCHAR(255),
    -- QUAN TRỌNG: Trong ứng dụng thực tế, cột này phải lưu mật khẩu đã được mã hóa (hashed)
    [Password] VARCHAR(255) NOT NULL,
    -- Phân quyền người dùng: 'customer' hoặc 'admin'
    [Role] VARCHAR(20) NOT NULL DEFAULT 'customer',
    CreatedAt DATETIME DEFAULT GETDATE()
);
GO

-- Bảng 2: Categories - Lưu danh mục sản phẩm (Bó hoa, Giỏ hoa,...)
PRINT 'Creating table: Categories...';
CREATE TABLE Categories (
    Id INT PRIMARY KEY IDENTITY(1,1),
    CategoryName NVARCHAR(255) UNIQUE NOT NULL,
    -- Mô tả ngắn cho danh mục, hiển thị ở trang admin
    CategoryDescription NVARCHAR(500)
);
GO

-- Bảng 3: Products - Bảng trung tâm lưu trữ thông tin sản phẩm
PRINT 'Creating table: Products...';
CREATE TABLE Products (
    Id INT PRIMARY KEY IDENTITY(1,1),
    ProductName NVARCHAR(255) NOT NULL,
    ProductDescription NVARCHAR(MAX),
    ImageUrl VARCHAR(500),
    -- Giá gốc của sản phẩm
    Price DECIMAL(18, 2) NOT NULL,
    -- Giá khuyến mãi (nếu có), dùng cho các giảm giá đơn giản, cố định
    SalePrice DECIMAL(18, 2),
    -- Khóa ngoại liên kết tới bảng Categories
    CategoryId INT,
    FOREIGN KEY (CategoryId) REFERENCES Categories(Id)
);
GO


-- Bảng 5: Promotions - Quản lý các chương trình khuyến mãi theo sự kiện, thời gian
PRINT 'Creating table: Promotions...';
CREATE TABLE Promotions (
    Id INT PRIMARY KEY IDENTITY(1,1),
    PromotionName NVARCHAR(255) NOT NULL, -- Ví dụ: 'Khuyến mãi 8/3', 'Black Friday Sale'
    DiscountPercent DECIMAL(5, 2) NOT NULL, -- % giảm giá
    StartDate DATETIME NOT NULL,
    EndDate DATETIME NOT NULL
);
GO

-- Bảng 6: Product_Promotions - Bảng nối, áp dụng một chương trình khuyến mãi cho nhiều sản phẩm
PRINT 'Creating table: Product_Promotions...';
CREATE TABLE Product_Promotions (
    ProductId INT,
    PromotionId INT,
    PRIMARY KEY (ProductId, PromotionId),
    FOREIGN KEY (ProductId) REFERENCES Products(Id),
    FOREIGN KEY (PromotionId) REFERENCES Promotions(Id)
);
GO

-- Bảng 7: Reviews - Lưu đánh giá và bình luận của khách hàng
PRINT 'Creating table: Reviews...';
CREATE TABLE Reviews (
    Id INT PRIMARY KEY IDENTITY(1,1),
    ProductId INT,
    AccountId INT,
    -- Số sao đánh giá, ràng buộc chỉ từ 1 đến 5
    Rating INT NOT NULL CHECK (Rating >= 1 AND Rating <= 5),
    Comment NVARCHAR(MAX),
    CreatedAt DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (ProductId) REFERENCES Products(Id),
    FOREIGN KEY (AccountId) REFERENCES Accounts(Id)
);
GO

-- Bảng 8: Orders - Lưu thông tin chung của một đơn hàng
PRINT 'Creating table: Orders...';
CREATE TABLE Orders (
    Id INT PRIMARY KEY IDENTITY(1,1),
    AccountId INT,
    -- Thông tin người nhận hàng
    RecipientName NVARCHAR(100) NOT NULL,
    ShippingAddress NVARCHAR(500) NOT NULL,
    RecipientPhone VARCHAR(20) NOT NULL,
    -- Ghi chú của khách hàng cho đơn hàng
    Note NVARCHAR(500),
    OrderDate DATETIME DEFAULT GETDATE(),
    ShippingFee DECIMAL(18, 2) DEFAULT 0,
    TotalAmount DECIMAL(18, 2) NOT NULL,
    PaymentMethod NVARCHAR(100),
    -- Trạng thái đơn hàng: 'Đang xử lý', 'Đang giao', 'Hoàn tất', 'Đã hủy'...
    [Status] NVARCHAR(50) DEFAULT N'Đang xử lý',
    FOREIGN KEY (AccountId) REFERENCES Accounts(Id)
);
GO

-- Bảng 9: OrderDetails - Lưu chi tiết các sản phẩm trong một đơn hàng
PRINT 'Creating table: OrderDetails...';
CREATE TABLE OrderDetails (
    OrderId INT,
    ProductId INT,
    Quantity INT NOT NULL CHECK (Quantity > 0),
    -- Lưu lại giá của sản phẩm tại thời điểm mua để đảm bảo tính chính xác
    Price DECIMAL(18, 2) NOT NULL,
    PRIMARY KEY (OrderId, ProductId),
    FOREIGN KEY (OrderId) REFERENCES Orders(Id),
    FOREIGN KEY (ProductId) REFERENCES Products(Id)
);
GO

-- Bảng 10: ChatMessages - Lưu lịch sử trò chuyện giữa khách hàng và admin
PRINT 'Creating table: ChatMessages...';
CREATE TABLE ChatMessages (
    Id BIGINT PRIMARY KEY IDENTITY(1,1),
    SenderId INT NOT NULL,
    ReceiverId INT NOT NULL,
    MessageContent NVARCHAR(MAX) NOT NULL,
    CreatedAt DATETIME DEFAULT GETDATE(),
    -- Trạng thái tin nhắn: 0 = chưa đọc, 1 = đã đọc
    IsRead BIT DEFAULT 0,
    FOREIGN KEY (SenderId) REFERENCES Accounts(Id),
    FOREIGN KEY (ReceiverId) REFERENCES Accounts(Id)
);
GO

--------------------------------------------------------------------------------
-- IV. CHÈN DỮ LIỆU MẪU (ĐÃ CẬP NHẬT THEO SCHEMA MỚI)
--------------------------------------------------------------------------------
PRINT 'Clearing old sample data...';
-- Xóa dữ liệu cũ theo đúng thứ tự để không vi phạm khóa ngoại (đã bỏ Product_Attributes)
DELETE FROM OrderDetails;
DELETE FROM Orders;
DELETE FROM Reviews;
DELETE FROM Product_Promotions;
DELETE FROM Promotions;
DELETE FROM Products;
DELETE FROM Categories;
DELETE FROM Accounts;
GO

PRINT 'Inserting detailed sample data...';
GO

--------------------------------------------------------------------------------
-- 1. Thêm tài khoản (Không đổi)
--------------------------------------------------------------------------------
PRINT 'Inserting accounts...';
INSERT INTO Accounts (FullName, Email, PhoneNumber, [Address], [Password], [Role])
VALUES
    (N'Quản Trị Viên', 'admin@flowershop.com', '0123456789', N'123 Đường ABC, Quận 1, TP.HCM', 'hashed_password_for_admin', 'admin'),
    (N'Nguyễn Văn An', 'an.nguyen@email.com', '0911111111', N'10 Nguyễn Trãi, Quận 5, TP.HCM', 'hashed_password_for_customer', 'customer'),
    (N'Trần Thị Bích', 'bich.tran@email.com', '0922222222', N'25 Lê Lợi, Quận 3, TP.HCM', 'hashed_password_for_customer', 'customer'),
    (N'Lê Minh Cường', 'cuong.le@email.com', '0933333333', N'33 Võ Văn Tần, Quận 3, TP.HCM', 'hashed_password_for_customer', 'customer');
GO

--------------------------------------------------------------------------------
-- 2. Thêm danh mục (Cập nhật tên cột)
--------------------------------------------------------------------------------
PRINT 'Inserting categories...';
INSERT INTO Categories (CategoryName, CategoryDescription)
VALUES
    (N'Bó hoa', N'Hoa được bó thủ công, thích hợp làm quà tặng tình yêu, sinh nhật.'),
    (N'Giỏ hoa', N'Hoa được cắm nghệ thuật trong giỏ, phù hợp cho các dịp khai trương, chúc mừng.'),
    (N'Chậu hoa', N'Hoa trồng trong chậu, dùng để trang trí không gian sống và văn phòng.');
GO

--------------------------------------------------------------------------------
-- 3. Thêm 30 sản phẩm (Cập nhật tên cột và bỏ các cột đã xóa)
--------------------------------------------------------------------------------
PRINT 'Inserting 30 products...';
-- Ghi chú: Vì các cột Stock, CreatedAt, FeaturedStatus đã bị loại bỏ, logic để xác định
-- "sản phẩm mới" và "bán chạy" giờ đây sẽ cần được xử lý ở tầng ứng dụng.
-- - Sản phẩm mới: Thường là các sản phẩm có Id cao nhất (được thêm gần đây nhất).
-- - Sản phẩm bán chạy: Cần tính toán từ bảng OrderDetails.
-- - Sản phẩm khuyến mãi: Được xác định bằng cột SalePrice có giá trị.

INSERT INTO Products (CategoryId, ProductName, ProductDescription, Price, SalePrice, ImageUrl) VALUES
-- BÓ HOA (10 sản phẩm, CategoryId = 1)
(1, N'Bó hoa hồng đỏ', N'Bó hoa tình yêu nồng cháy, biểu tượng của sự lãng mạn.', 550000, 499000, 'images/bohoa/hong_do.jpg'),
(1, N'Bó hoa hồng trắng', N'Vẻ đẹp tinh khôi, trong sáng, phù hợp cho những khởi đầu mới.', 520000, NULL, 'images/bohoa/hong_trang.jpg'),
(1, N'Bó hoa tulip hồng', N'Sự ngọt ngào và lời chúc hạnh phúc gửi trao đến người thương.', 650000, 599000, 'images/bohoa/tulip_hong.jpg'),
(1, N'Bó hoa tulip vàng', N'Tượng trưng cho ánh nắng rạng rỡ và tình bạn chân thành.', 620000, NULL, 'images/bohoa/tulip_vang.jpg'),
(1, N'Bó hoa baby trắng', N'Tình yêu ngây thơ và vĩnh cửu, nhẹ nhàng và tinh tế.', 450000, NULL, 'images/bohoa/baby_trang.jpg'),
(1, N'Bó hoa baby hồng', N'Sự dịu dàng, đáng yêu, món quà hoàn hảo cho phái nữ.', 480000, NULL, 'images/bohoa/baby_hong.jpg'),
(1, N'Bó hoa hướng dương', N'Năng lượng tích cực và sự lạc quan vươn tới tương lai.', 580000, NULL, 'images/bohoa/huong_duong.jpg'),
(1, N'Bó hoa cúc họa mi', N'Vẻ đẹp mộc mạc, giản dị của những bông cúc trắng tinh khôi.', 350000, 329000, 'images/bohoa/cuc_hoa_mi.jpg'),
(1, N'Bó hoa cẩm chướng hồng', N'Thể hiện lòng biết ơn và sự ngưỡng mộ sâu sắc.', 480000, NULL, 'images/bohoa/cam_chuong.jpg'),
(1, N'Bó hoa mix pastel', N'Sự kết hợp hài hòa của các loài hoa mang tông màu nhẹ nhàng.', 750000, NULL, 'images/bohoa/mix_pastel.jpg'),

-- GIỎ HOA (10 sản phẩm, CategoryId = 2)
(2, N'Giỏ hoa hồng pastel', N'Sự sang trọng và thanh lịch cho các dịp quan trọng.', 1200000, NULL, 'images/giohoa/hong_pastel.jpg'),
(2, N'Giỏ hoa hồng đỏ', N'Món quà ấn tượng và ý nghĩa cho đối tác hoặc người thân.', 1350000, NULL, 'images/giohoa/hong_do.jpg'),
(2, N'Giỏ hoa hồng trắng', N'Sự trang trọng, tinh khôi, phù hợp cho các sự kiện chúc mừng.', 1100000, NULL, 'images/giohoa/hong_trang.jpg'),
(2, N'Giỏ hoa lan hồ điệp', N'Đẳng cấp, quý phái, món quà mừng khai trương hồng phát.', 2500000, 2399000, 'images/giohoa/lan_ho_diep.jpg'),
(2, N'Giỏ hoa ly vàng', N'Tượng trưng cho sự giàu sang, thịnh vượng và hạnh phúc.', 950000, NULL, 'images/giohoa/ly_vang.jpg'),
(2, N'Giỏ hoa tulip đỏ', N'Lời tỏ tình hoàn hảo và lãng mạn không thể chối từ.', 1150000, NULL, 'images/giohoa/tulip_do.jpg'),
(2, N'Giỏ hoa cẩm tú cầu', N'Lòng biết ơn chân thành và sự quan tâm sâu sắc.', 850000, NULL, 'images/giohoa/cam_tu_cau.jpg'),
(2, N'Giỏ hoa sen', N'Sự thanh cao, tôn nghiêm, món quà đặc biệt cho người lớn tuổi.', 1500000, NULL, 'images/giohoa/sen.jpg'),
(2, N'Giỏ hoa hướng dương rực rỡ', N'Mang lại niềm vui, sức sống và năng lượng cho ngày mới.', 980000, NULL, 'images/giohoa/huong_duong.jpg'),
(2, N'Giỏ hoa mix mùa xuân', N'Sự tươi mới, khởi đầu may mắn và thành công.', 1300000, 1199000, 'images/giohoa/mix_mua_xuan.jpg'),

-- CHẬU HOA (10 sản phẩm, CategoryId = 3)
(3, N'Chậu lan hồ điệp trắng', N'Vẻ đẹp sang trọng, bền lâu cho không gian sống và làm việc.', 1800000, NULL, 'images/chauhoa/lan_trang.jpg'),
(3, N'Chậu lan hồ điệp vàng', N'Mang lại may mắn, tài lộc và sự thịnh vượng cho gia chủ.', 1900000, NULL, 'images/chauhoa/lan_vang.jpg'),
(3, N'Chậu lan tím', N'Sự thủy chung, son sắt, món quà kỷ niệm ngày cưới ý nghĩa.', 1750000, NULL, 'images/chauhoa/lan_tim.jpg'),
(3, N'Chậu sen đá mini', N'Sức sống bền bỉ và mãnh liệt, phù hợp để bàn làm việc.', 150000, NULL, 'images/chauhoa/sen_da.jpg'),
(3, N'Chậu xương rồng nhỏ', N'Mạnh mẽ, kiên cường, món quà độc đáo cho bạn bè.', 120000, NULL, 'images/chauhoa/xuong_rong.jpg'),
(3, N'Chậu hồng tỉ muội', N'Tượng trưng cho tình chị em, tình bạn thân thiết, gắn kết.', 350000, NULL, 'images/chauhoa/hong_ti_muoi.jpg'),
(3, N'Chậu hoa cúc mini', N'Mang lại niềm vui, sự lạc quan và không khí trong lành.', 250000, NULL, 'images/chauhoa/cuc_mini.jpg'),
(3, N'Chậu lan ý', N'Giúp điều hòa không khí, mang lại sự bình yên và hạnh phúc.', 450000, NULL, 'images/chauhoa/lan_y.jpg'),
(3, N'Chậu sống đời', N'Cầu chúc sức khỏe, sự trường thọ cho ông bà, cha mẹ.', 220000, NULL, 'images/chauhoa/song_doi.jpg'),
(3, N'Chậu dạ yến thảo', N'Vẻ đẹp rực rỡ, sai hoa, tô điểm cho ban công và sân vườn.', 280000, NULL, 'images/chauhoa/da_yen_thao.jpg');
GO

--------------------------------------------------------------------------------
-- 4. Thêm đơn hàng mẫu (Không đổi)
--------------------------------------------------------------------------------
PRINT 'Inserting sample orders...';
INSERT INTO Orders (AccountId, RecipientName, ShippingAddress, RecipientPhone, TotalAmount, Status, Note) VALUES
(2, N'Nguyễn Văn An', N'10 Nguyễn Trãi, Quận 5, TP.HCM', '0911111111', 499000, N'Hoàn tất', N'Giao trong giờ hành chính.'),
(3, N'Trần Thị Bích', N'25 Lê Lợi, Quận 3, TP.HCM', '0922222222', 1350000, N'Hoàn tất', NULL),
(4, N'Lê Minh Cường', N'33 Võ Văn Tần, Quận 3, TP.HCM', '0933333333', 980000, N'Đang giao', N'Vui lòng gọi trước khi giao.'),
(2, N'Người nhận khác', N'200 Pasteur, Quận 3, TP.HCM', '0909090909', 480000, N'Đang xử lý', N'Đây là quà tặng, vui lòng không tiết lộ giá.'),
(3, N'Trần Thị Bích', N'25 Lê Lợi, Quận 3, TP.HCM', '0922222222', 250000, N'Đã hủy', N'Khách hàng báo hủy.'),
(4, N'Lê Minh Cường', N'33 Võ Văn Tần, Quận 3, TP.HCM', '0933333333', 1200000, N'Hoàn tất', NULL),
(2, N'Nguyễn Văn An', N'10 Nguyễn Trãi, Quận 5, TP.HCM', '0911111111', 1350000, N'Đang giao', NULL),
(3, N'Bạn của Bích', N'180 Lý Tự Trọng, Quận 1, TP.HCM', '0988888888', 580000, N'Đang xử lý', N'Giao gấp trong hôm nay.'),
(4, N'Lê Minh Cường', N'33 Võ Văn Tần, Quận 3, TP.HCM', '0933333333', 1800000, N'Hoàn tất', NULL),
(2, N'Nguyễn Văn An', N'10 Nguyễn Trãi, Quận 5, TP.HCM', '0911111111', 220000, N'Hoàn tất', NULL);
GO

--------------------------------------------------------------------------------
-- 5. Thêm chi tiết đơn hàng (Cập nhật ProductId và giá cho khớp)
--------------------------------------------------------------------------------
PRINT 'Inserting sample order details...';
INSERT INTO OrderDetails (OrderId, ProductId, Quantity, Price) VALUES 
(1, 1, 1, 499000),   -- An mua Bó hồng đỏ (khuyến mãi)
(2, 12, 1, 1350000), -- Bích mua Giỏ hồng đỏ
(3, 19, 1, 980000),  -- Cường mua Giỏ hướng dương
(4, 6, 1, 480000),   -- An mua tặng Bó baby hồng
(5, 27, 1, 250000),  -- Bích hủy Chậu cúc mini
(6, 11, 1, 1200000), -- Cường mua Giỏ hồng pastel
(7, 12, 1, 1350000), -- An mua Giỏ hồng đỏ
(8, 7, 1, 580000),   -- Bích mua tặng Bó hướng dương
(9, 21, 1, 1800000), -- Cường mua Chậu lan trắng
(10, 29, 1, 220000); -- An mua Chậu sống đời
GO

--------------------------------------------------------------------------------
-- 6. Thêm đánh giá mẫu (Cập nhật ProductId cho khớp)
--------------------------------------------------------------------------------
PRINT 'Inserting sample reviews...';
INSERT INTO Reviews (ProductId, AccountId, Rating, Comment) VALUES
(1, 2, 5, N'Hoa rất đẹp, giao đúng giờ. Bạn gái mình rất thích!'),
(12, 3, 5, N'Giỏ hoa sang trọng, shop tư vấn nhiệt tình. Sẽ ủng hộ lần sau.'),
(19, 4, 4, N'Hoa tươi, nhiều nụ. Tuy nhiên shipper giao hơi trễ so với dự kiến.'),
(7, 2, 5, N'Bó hoa hướng dương rực rỡ y như hình, rất hài lòng.'),
(21, 4, 4, N'Chậu lan đẹp, nhưng giá hơi cao một chút.'),
(14, 2, 3, N'Giỏ hoa lan hồ điệp bị dập mất một vài bông, hơi buồn.'),
(24, 3, 5, N'Sen đá mini siêu cưng, để bàn làm việc hợp lý.'),
(10, 4, 5, N'Hoa đẹp, tươi lâu, màu sắc hài hòa.'),
(3, 2, 4, N'Bó hoa tulip hồng có vài lá bị úa, còn lại thì ổn.');
GO

PRINT 'Database script executed successfully!';
GO