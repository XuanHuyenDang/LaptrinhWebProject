/* 1. SỬ DỤNG DATABASE MASTER ĐỂ CHUẨN BỊ */
USE [master]
GO

/* 2. TỰ ĐỘNG XÓA DATABASE CŨ NẾU CÓ ĐỂ TRÁNH LỖI */
IF DB_ID('FlowerShopDB') IS NOT NULL
BEGIN
    ALTER DATABASE [FlowerShopDB] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE [FlowerShopDB];
END
GO

/* 3. TẠO DATABASE ĐÃ SỬA CHO SQL SERVER 2019 */
CREATE DATABASE [FlowerShopDB]
 CONTAINMENT = NONE
 ON  PRIMARY 
 /* ĐÃ SỬA ĐƯỜNG DẪN TỪ MSSQL16 -> MSSQL15 */
( NAME = N'FlowerShopDB', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL15.MSSQLSERVER\MSSQL\DATA\FlowerShopDB.mdf' , SIZE = 8192KB , MAXSIZE = UNLIMITED, FILEGROWTH = 65536KB )
 LOG ON 
( NAME = N'FlowerShopDB_log', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL15.MSSQLSERVER\MSSQL\DATA\FlowerShopDB_log.ldf' , SIZE = 8192KB , MAXSIZE = 2048GB , FILEGROWTH = 65536KB )
 /* ĐÃ XÓA 'LEDGER = OFF' KHÔNG TƯƠNG THÍCH VỚI 2019 */
 WITH CATALOG_COLLATION = DATABASE_DEFAULT
GO

/* ĐÃ SỬA COMPATIBILITY_LEVEL TỪ 160 -> 150 */
ALTER DATABASE [FlowerShopDB] SET COMPATIBILITY_LEVEL = 150
GO

IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [FlowerShopDB].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [FlowerShopDB] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [FlowerShopDB] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [FlowerShopDB] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [FlowerShopDB] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [FlowerShopDB] SET ARITHABORT OFF 
GO
ALTER DATABASE [FlowerShopDB] SET AUTO_CLOSE OFF 
GO
ALTER DATABASE [FlowerShopDB] SET AUTO_SHRINK OFF 
GO
ALTER DATABASE [FlowerShopDB] SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE [FlowerShopDB] SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE [FlowerShopDB] SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE [FlowerShopDB] SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE [FlowerShopDB] SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE [FlowerShopDB] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [FlowerShopDB] SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE [FlowerShopDB] SET  ENABLE_BROKER 
GO
ALTER DATABASE [FlowerShopDB] SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE [FlowerShopDB] SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
ALTER DATABASE [FlowerShopDB] SET TRUSTWORTHY OFF 
GO
ALTER DATABASE [FlowerShopDB] SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE [FlowerShopDB] SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE [FlowerShopDB] SET READ_COMMITTED_SNAPSHOT OFF 
GO
ALTER DATABASE [FlowerShopDB] SET HONOR_BROKER_PRIORITY OFF 
GO
ALTER DATABASE [FlowerShopDB] SET RECOVERY FULL 
GO
ALTER DATABASE [FlowerShopDB] SET  MULTI_USER 
GO
ALTER DATABASE [FlowerShopDB] SET PAGE_VERIFY CHECKSUM  
GO
ALTER DATABASE [FlowerShopDB] SET DB_CHAINING OFF 
GO
ALTER DATABASE [FlowerShopDB] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE [FlowerShopDB] SET TARGET_RECOVERY_TIME = 60 SECONDS 
GO
ALTER DATABASE [FlowerShopDB] SET DELAYED_DURABILITY = DISABLED 
GO
ALTER DATABASE [FlowerShopDB] SET ACCELERATED_DATABASE_RECOVERY = OFF  
GO
EXEC sys.sp_db_vardecimal_storage_format N'FlowerShopDB', N'ON'
GO
ALTER DATABASE [FlowerShopDB] SET QUERY_STORE = ON
GO
ALTER DATABASE [FlowerShopDB] SET QUERY_STORE (OPERATION_MODE = READ_WRITE, CLEANUP_POLICY = (STALE_QUERY_THRESHOLD_DAYS = 30), DATA_FLUSH_INTERVAL_SECONDS = 900, INTERVAL_LENGTH_MINUTES = 60, MAX_STORAGE_SIZE_MB = 1000, QUERY_CAPTURE_MODE = AUTO, SIZE_BASED_CLEANUP_MODE = AUTO, MAX_PLANS_PER_QUERY = 200, WAIT_STATS_CAPTURE_MODE = ON)
GO

/* 4. CHUYỂN SANG DATABASE VỪA TẠO ĐỂ TIẾP TỤC */
USE [FlowerShopDB]
GO
/****** Object:  Table [dbo].[Accounts]    Script Date: 10/28/2025 3:39:48 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Accounts](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[FullName] [nvarchar](100) NOT NULL,
	[Email] [varchar](100) NOT NULL,
	[PhoneNumber] [varchar](20) NULL,
	[Address] [nvarchar](255) NULL,
	[Password] [varchar](255) NOT NULL,
	[Role] [varchar](20) NOT NULL,
	[CreatedAt] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Categories]    Script Date: 10/28/2025 3:39:48 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Categories](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[CategoryName] [nvarchar](255) NOT NULL,
	[CategoryDescription] [nvarchar](500) NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[ChatMessages]    Script Date: 10/28/2025 3:39:48 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[ChatMessages](
	[Id] [bigint] IDENTITY(1,1) NOT NULL,
	[SenderId] [int] NOT NULL,
	[ReceiverId] [int] NOT NULL,
	[MessageContent] [nvarchar](max) NOT NULL,
	[CreatedAt] [datetime] NULL,
	[IsRead] [bit] NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[DiscountCodes]    Script Date: 10/28/2025 3:39:48 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[DiscountCodes](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[Code] [varchar](50) NOT NULL,
	[DiscountPercent] [decimal](5, 2) NULL,
	[DiscountAmount] [decimal](18, 2) NULL,
	[MinOrderAmount] [decimal](18, 2) NULL,
	[MaxDiscountAmount] [decimal](18, 2) NULL,
	[StartDate] [datetime] NOT NULL,
	[EndDate] [datetime] NOT NULL,
	[MaxUsage] [int] NULL,
	[CurrentUsage] [int] NOT NULL,
	[IsActive] [bit] NOT NULL,
	[Description] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[OrderDetails]    Script Date: 10/28/2025 3:39:48 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[OrderDetails](
	[OrderId] [int] NOT NULL,
	[ProductId] [int] NOT NULL,
	[Quantity] [int] NOT NULL,
	[Price] [decimal](18, 2) NOT NULL,
	[ShippingMethod] [nvarchar](20) NULL,
PRIMARY KEY CLUSTERED 
(
	[OrderId] ASC,
	[ProductId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[OrderReturnRequests]    Script Date: 10/28/2025 3:39:48 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[OrderReturnRequests](
	[id] [bigint] IDENTITY(1,1) NOT NULL,
	[OrderId] [int] NOT NULL,
	[RequestDate] [datetime] NOT NULL,
	[Reason] [nvarchar](max) NOT NULL,
	[EvidenceUrl] [nvarchar](500) NULL,
	[Status] [varchar](20) NOT NULL,
	[AdminNotes] [nvarchar](max) NULL,
	[ProcessedDate] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Orders]    Script Date: 10/28/2025 3:39:48 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Orders](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[AccountId] [int] NULL,
	[RecipientName] [nvarchar](100) NOT NULL,
	[ShippingAddress] [nvarchar](500) NOT NULL,
	[RecipientPhone] [varchar](20) NOT NULL,
	[Note] [nvarchar](500) NULL,
	[OrderDate] [datetime] NULL,
	[ShippingFee] [decimal](18, 2) NULL,
	[TotalAmount] [decimal](18, 2) NOT NULL,
	[PaymentMethod] [nvarchar](100) NULL,
	[Status] [nvarchar](50) NULL,
	[ShippingMethod] [nvarchar](20) NOT NULL,
	[CompletedDate] [datetime] NULL,
	[DiscountCodeId] [int] NULL,
	[DiscountAmount] [decimal](18, 2) NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Product_Promotions]    Script Date: 10/28/2025 3:39:48 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Product_Promotions](
	[ProductId] [int] NOT NULL,
	[PromotionId] [int] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[ProductId] ASC,
	[PromotionId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Products]    Script Date: 10/28/2025 3:39:48 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Products](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[ProductName] [nvarchar](255) NOT NULL,
	[ProductDescription] [nvarchar](max) NULL,
	[ImageUrl] [varchar](500) NULL,
	[Price] [decimal](18, 2) NOT NULL,
	[SalePrice] [decimal](18, 2) NULL,
	[CategoryId] [int] NULL,
	[Status] [bit] NULL,
	[Sold] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Promotions]    Script Date: 10/28/2025 3:39:48 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Promotions](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[PromotionName] [nvarchar](255) NOT NULL,
	[DiscountPercent] [decimal](5, 2) NOT NULL,
	[StartDate] [datetime] NOT NULL,
	[EndDate] [datetime] NOT NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Reviews]    Script Date: 10/28/2025 3:39:48 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Reviews](
	[Id] [int] IDENTITY(1,1) NOT NULL,
	[ProductId] [int] NULL,
	[AccountId] [int] NULL,
	[Rating] [int] NOT NULL,
	[Comment] [nvarchar](max) NULL,
	[CreatedAt] [datetime] NULL,
PRIMARY KEY CLUSTERED 
(
	[Id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO
/****** Object:  Table [dbo].[Wishlist]    Script Date: 10/28/2025 3:39:48 PM ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Wishlist](
	[AccountId] [int] NOT NULL,
	[ProductId] [int] NOT NULL,
	[CreatedAt] [datetime] NULL,
 CONSTRAINT [PK_Wishlist] PRIMARY KEY CLUSTERED 
(
	[AccountId] ASC,
	[ProductId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO
SET IDENTITY_INSERT [dbo].[Accounts] ON 

INSERT [dbo].[Accounts] ([Id], [FullName], [Email], [PhoneNumber], [Address], [Password], [Role], [CreatedAt]) VALUES (1, N'Quản Trị Viên', N'admin@flowershop.com', N'0123456789', N'123 Đường ABC, Quận 1, TP.HCM', N'$2a$10$Rg9s4qo/0cIwTq.h6klCOOwccNwnqsLXRm6Vah3AFaOL24.iGQ0Mq', N'admin', CAST(N'2025-10-14T19:54:58.827' AS DateTime))
INSERT [dbo].[Accounts] ([Id], [FullName], [Email], [PhoneNumber], [Address], [Password], [Role], [CreatedAt]) VALUES (2, N'Nguyễn Văn An', N'an.nguyen@email.com', N'0911111111', N'10 Nguyễn Trãi, Quận 5, TP.HCM', N'$2a$10$Rg9s4qo/0cIwTq.h6klCOOwccNwnqsLXRm6Vah3AFaOL24.iGQ0Mq', N'customer', CAST(N'2025-10-14T19:54:58.827' AS DateTime))
INSERT [dbo].[Accounts] ([Id], [FullName], [Email], [PhoneNumber], [Address], [Password], [Role], [CreatedAt]) VALUES (3, N'Trần Thị Bích', N'bich.tran@email.com', N'0922222222', N'25 Lê Lợi, Quận 3, TP.HCM', N'hashed_password_for_customer', N'customer', CAST(N'2025-10-14T19:54:58.827' AS DateTime))
INSERT [dbo].[Accounts] ([Id], [FullName], [Email], [PhoneNumber], [Address], [Password], [Role], [CreatedAt]) VALUES (4, N'Lê Minh Cường', N'cuong.le@email.com', N'0933333333', N'33 Võ Văn Tần, Quận 3, TP.HCM', N'hashed_password_for_customer', N'customer', CAST(N'2025-10-14T19:54:58.827' AS DateTime))
SET IDENTITY_INSERT [dbo].[Accounts] OFF
GO
SET IDENTITY_INSERT [dbo].[Categories] ON 

INSERT [dbo].[Categories] ([Id], [CategoryName], [CategoryDescription]) VALUES (1, N'Bó hoa', N'Hoa được bó thủ công, thích hợp làm quà tặng tình yêu, sinh nhật.')
INSERT [dbo].[Categories] ([Id], [CategoryName], [CategoryDescription]) VALUES (2, N'Giỏ hoa', N'Hoa được cắm nghệ thuật trong giỏ, phù hợp cho các dịp khai trương, chúc mừng.')
INSERT [dbo].[Categories] ([Id], [CategoryName], [CategoryDescription]) VALUES (3, N'Chậu hoa', N'Hoa trồng trong chậu, dùng để trang trí không gian sống và văn phòng.')
SET IDENTITY_INSERT [dbo].[Categories] OFF
GO
SET IDENTITY_INSERT [dbo].[ChatMessages] ON 

INSERT [dbo].[ChatMessages] ([Id], [SenderId], [ReceiverId], [MessageContent], [CreatedAt], [IsRead]) VALUES (1, 2, 1, N'alo', CAST(N'2025-10-21T16:17:29.643' AS DateTime), 0)
INSERT [dbo].[ChatMessages] ([Id], [SenderId], [ReceiverId], [MessageContent], [CreatedAt], [IsRead]) VALUES (2, 1, 2, N'hello fen', CAST(N'2025-10-21T16:22:12.907' AS DateTime), 0)
INSERT [dbo].[ChatMessages] ([Id], [SenderId], [ReceiverId], [MessageContent], [CreatedAt], [IsRead]) VALUES (3, 2, 1, N'bán cho chậu hoa 500k', CAST(N'2025-10-21T16:22:30.300' AS DateTime), 0)
INSERT [dbo].[ChatMessages] ([Id], [SenderId], [ReceiverId], [MessageContent], [CreatedAt], [IsRead]) VALUES (4, 1, 2, N'oke', CAST(N'2025-10-21T16:22:33.237' AS DateTime), 0)
INSERT [dbo].[ChatMessages] ([Id], [SenderId], [ReceiverId], [MessageContent], [CreatedAt], [IsRead]) VALUES (5, 2, 1, N'oke chưa fen', CAST(N'2025-10-23T14:23:21.613' AS DateTime), 0)
INSERT [dbo].[ChatMessages] ([Id], [SenderId], [ReceiverId], [MessageContent], [CreatedAt], [IsRead]) VALUES (6, 1, 2, N'oke rồi fen', CAST(N'2025-10-23T14:23:34.233' AS DateTime), 0)
SET IDENTITY_INSERT [dbo].[ChatMessages] OFF
GO
SET IDENTITY_INSERT [dbo].[DiscountCodes] ON 

INSERT [dbo].[DiscountCodes] ([Id], [Code], [DiscountPercent], [DiscountAmount], [MinOrderAmount], [MaxDiscountAmount], [StartDate], [EndDate], [MaxUsage], [CurrentUsage], [IsActive], [Description]) VALUES (1, N'ABC', CAST(10.00 AS Decimal(5, 2)), NULL, CAST(0.00 AS Decimal(18, 2)), NULL, CAST(N'2025-10-26T17:00:00.000' AS DateTime), CAST(N'2025-10-28T17:01:00.000' AS DateTime), NULL, 1, 1, N'')
SET IDENTITY_INSERT [dbo].[DiscountCodes] OFF
GO
INSERT [dbo].[OrderDetails] ([OrderId], [ProductId], [Quantity], [Price], [ShippingMethod]) VALUES (30, 2, 1, CAST(520000.00 AS Decimal(18, 2)), NULL)
INSERT [dbo].[OrderDetails] ([OrderId], [ProductId], [Quantity], [Price], [ShippingMethod]) VALUES (30, 8, 4, CAST(329000.00 AS Decimal(18, 2)), NULL)
INSERT [dbo].[OrderDetails] ([OrderId], [ProductId], [Quantity], [Price], [ShippingMethod]) VALUES (30, 9, 1, CAST(480000.00 AS Decimal(18, 2)), NULL)
INSERT [dbo].[OrderDetails] ([OrderId], [ProductId], [Quantity], [Price], [ShippingMethod]) VALUES (30, 11, 3, CAST(1200000.00 AS Decimal(18, 2)), NULL)
INSERT [dbo].[OrderDetails] ([OrderId], [ProductId], [Quantity], [Price], [ShippingMethod]) VALUES (30, 25, 1, CAST(120000.00 AS Decimal(18, 2)), NULL)
INSERT [dbo].[OrderDetails] ([OrderId], [ProductId], [Quantity], [Price], [ShippingMethod]) VALUES (30, 28, 1, CAST(450000.00 AS Decimal(18, 2)), NULL)
INSERT [dbo].[OrderDetails] ([OrderId], [ProductId], [Quantity], [Price], [ShippingMethod]) VALUES (30, 29, 1, CAST(220000.00 AS Decimal(18, 2)), NULL)
INSERT [dbo].[OrderDetails] ([OrderId], [ProductId], [Quantity], [Price], [ShippingMethod]) VALUES (30, 30, 1, CAST(280000.00 AS Decimal(18, 2)), NULL)
INSERT [dbo].[OrderDetails] ([OrderId], [ProductId], [Quantity], [Price], [ShippingMethod]) VALUES (31, 30, 1, CAST(280000.00 AS Decimal(18, 2)), NULL)
GO
SET IDENTITY_INSERT [dbo].[Orders] ON 

INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (1, 2, N'Nguyễn Văn An', N'10 Nguyễn Trãi, Quận 5, TP.HCM', N'0911111111', N'Giao trong giờ hành chính.', CAST(N'2025-10-14T19:54:58.887' AS DateTime), CAST(0.00 AS Decimal(18, 2)), CAST(499000.00 AS Decimal(18, 2)), NULL, N'Hoàn tất', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (2, 3, N'Trần Thị Bích', N'25 Lê Lợi, Quận 3, TP.HCM', N'0922222222', NULL, CAST(N'2025-10-14T19:54:58.887' AS DateTime), CAST(0.00 AS Decimal(18, 2)), CAST(1350000.00 AS Decimal(18, 2)), NULL, N'Hoàn tất', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (3, 4, N'Lê Minh Cường', N'33 Võ Văn Tần, Quận 3, TP.HCM', N'0933333333', N'Vui lòng gọi trước khi giao.', CAST(N'2025-10-14T19:54:58.887' AS DateTime), CAST(0.00 AS Decimal(18, 2)), CAST(980000.00 AS Decimal(18, 2)), NULL, N'Đang giao', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (4, 2, N'Người nhận khác', N'200 Pasteur, Quận 3, TP.HCM', N'0909090909', N'Đây là quà tặng, vui lòng không tiết lộ giá.', CAST(N'2025-10-14T19:54:58.887' AS DateTime), CAST(0.00 AS Decimal(18, 2)), CAST(480000.00 AS Decimal(18, 2)), NULL, N'Đang xử lý', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (5, 3, N'Trần Thị Bích', N'25 Lê Lợi, Quận 3, TP.HCM', N'0922222222', N'Khách hàng báo hủy.', CAST(N'2025-10-14T19:54:58.887' AS DateTime), CAST(0.00 AS Decimal(18, 2)), CAST(250000.00 AS Decimal(18, 2)), NULL, N'Đã hủy', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (6, 4, N'Lê Minh Cường', N'33 Võ Văn Tần, Quận 3, TP.HCM', N'0933333333', NULL, CAST(N'2025-10-14T19:54:58.887' AS DateTime), CAST(0.00 AS Decimal(18, 2)), CAST(1200000.00 AS Decimal(18, 2)), NULL, N'Hoàn tất', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (7, 2, N'Nguyễn Văn An', N'10 Nguyễn Trãi, Quận 5, TP.HCM', N'0911111111', NULL, CAST(N'2025-10-14T19:54:58.887' AS DateTime), CAST(0.00 AS Decimal(18, 2)), CAST(1350000.00 AS Decimal(18, 2)), NULL, N'Đang giao', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (8, 3, N'Bạn của Bích', N'180 Lý Tự Trọng, Quận 1, TP.HCM', N'0988888888', N'Giao gấp trong hôm nay.', CAST(N'2025-10-14T19:54:58.887' AS DateTime), CAST(0.00 AS Decimal(18, 2)), CAST(580000.00 AS Decimal(18, 2)), NULL, N'Đang xử lý', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (9, 4, N'Lê Minh Cường', N'33 Võ Văn Tần, Quận 3, TP.HCM', N'0933333333', NULL, CAST(N'2025-10-14T19:54:58.887' AS DateTime), CAST(0.00 AS Decimal(18, 2)), CAST(1800000.00 AS Decimal(18, 2)), NULL, N'Hoàn tất', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (10, 2, N'Nguyễn Văn An', N'10 Nguyễn Trãi, Quận 5, TP.HCM', N'0911111111', NULL, CAST(N'2025-10-14T19:54:58.887' AS DateTime), CAST(0.00 AS Decimal(18, 2)), CAST(220000.00 AS Decimal(18, 2)), NULL, N'Hoàn tất', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (11, 2, N'Nguyễn trần quốc thi', N's5.01 vinhomes grandpark', N'+84937199872', N'', CAST(N'2025-10-19T02:27:00.910' AS DateTime), CAST(30000.00 AS Decimal(18, 2)), CAST(1030000.00 AS Decimal(18, 2)), N'COD', N'Đang xử lý', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (12, 2, N'Nguyễn trần quốc thi', N's5.01 vinhomes grandpark', N'0937199872', N'', CAST(N'2025-10-19T13:28:57.403' AS DateTime), CAST(30000.00 AS Decimal(18, 2)), CAST(1510000.00 AS Decimal(18, 2)), N'COD', N'Đang xử lý', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (13, 2, N'Nguyễn Trần Quốc Thi', N'Ấp Đất Mới', N'0356551044', N'', CAST(N'2025-10-19T13:41:09.487' AS DateTime), CAST(30000.00 AS Decimal(18, 2)), CAST(1230000.00 AS Decimal(18, 2)), N'COD', N'Đang xử lý', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (14, 2, N'Nguyễn Trần Quốc Thi', N'Ấp Đất Mới', N'0356551044', NULL, CAST(N'2025-10-19T13:44:41.527' AS DateTime), CAST(30000.00 AS Decimal(18, 2)), CAST(359000.00 AS Decimal(18, 2)), N'COD', N'Đang xử lý', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (15, 2, N'Nguyễn Trần Quốc Thi', N'Ấp Đất Mới', N'0356551044', NULL, CAST(N'2025-10-19T14:19:37.717' AS DateTime), CAST(30000.00 AS Decimal(18, 2)), CAST(250000.00 AS Decimal(18, 2)), N'COD', N'Đang xử lý', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (16, 2, N'Nguyễn trần quốc thi', N's5.01 vinhomes grandpark', N'0937199872', NULL, CAST(N'2025-10-19T14:32:39.240' AS DateTime), CAST(30000.00 AS Decimal(18, 2)), CAST(359000.00 AS Decimal(18, 2)), N'COD', N'Hoàn tất', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (17, 2, N'Nguyễn trần quốc thi', N's5.01 vinhomes grandpark', N'0937199872', NULL, CAST(N'2025-10-20T01:32:02.750' AS DateTime), CAST(30000.00 AS Decimal(18, 2)), CAST(550000.00 AS Decimal(18, 2)), N'COD', N'Đã hủy', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (18, 2, N'Nguyễn Trần Quốc Thi', N'Ấp Đất Mới', N'0356551044', NULL, CAST(N'2025-10-20T01:57:38.670' AS DateTime), CAST(30000.00 AS Decimal(18, 2)), CAST(359000.00 AS Decimal(18, 2)), N'COD', N'Đã hủy', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (19, 2, N'Nguyễn Trần Quốc Thi', N'Ấp Đất Mới', N'0356551044', NULL, CAST(N'2025-10-20T02:01:37.433' AS DateTime), CAST(30000.00 AS Decimal(18, 2)), CAST(990000.00 AS Decimal(18, 2)), N'COD', N'Đã trả hàng', N'FAST', CAST(N'2025-10-23T15:53:27.587' AS DateTime), NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (20, 2, N'Nguyễn Trần Quốc Thi', N'Ấp Đất Mới', N'0356551044', NULL, CAST(N'2025-10-23T16:10:52.953' AS DateTime), CAST(0.00 AS Decimal(18, 2)), CAST(520000.00 AS Decimal(18, 2)), N'COD', N'Đã từ chối trả hàng', N'FAST', CAST(N'2025-10-23T16:11:03.147' AS DateTime), NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (21, 1, N'', N'', N'', NULL, CAST(N'2025-10-21T16:25:34.817' AS DateTime), CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), NULL, N'CART', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (22, 2, N'Nguyễn Trần Quốc Thi', N'Ấp Đất Mới', N'0356551044', NULL, CAST(N'2025-10-23T16:14:06.083' AS DateTime), CAST(0.00 AS Decimal(18, 2)), CAST(520000.00 AS Decimal(18, 2)), N'COD', N'Yêu cầu trả hàng', N'FAST', CAST(N'2025-10-23T16:14:18.997' AS DateTime), NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (23, 2, N'Nguyễn Văn An', N'10 Nguyễn Trãi, Quận 5, TP.HCM', N'0911111111', NULL, CAST(N'2025-10-24T15:50:10.357' AS DateTime), CAST(0.00 AS Decimal(18, 2)), CAST(520000.00 AS Decimal(18, 2)), N'VNPAY', N'Chờ thanh toán', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (24, 2, N'Nguyễn Văn An', N'10 Nguyễn Trãi, Quận 5, TP.HCM', N'0911111111', NULL, CAST(N'2025-10-24T15:51:22.650' AS DateTime), CAST(30000.00 AS Decimal(18, 2)), CAST(510000.00 AS Decimal(18, 2)), N'VNPAY', N'Đang xử lý', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (25, 2, N'Nguyễn Văn An', N'10 Nguyễn Trãi, Quận 5, TP.HCM', N'0911111111', NULL, CAST(N'2025-10-24T16:55:03.617' AS DateTime), CAST(30000.00 AS Decimal(18, 2)), CAST(359000.00 AS Decimal(18, 2)), N'VNPAY', N'Chờ thanh toán', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (26, 2, N'Nguyễn Văn An', N'10 Nguyễn Trãi, Quận 5, TP.HCM', N'0911111111', NULL, CAST(N'2025-10-24T16:55:09.590' AS DateTime), CAST(30000.00 AS Decimal(18, 2)), CAST(359000.00 AS Decimal(18, 2)), N'VNPAY', N'Chờ thanh toán', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (27, 2, N'Nguyễn Văn An', N'10 Nguyễn Trãi, Quận 5, TP.HCM', N'0911111111', NULL, CAST(N'2025-10-24T21:34:22.397' AS DateTime), CAST(0.00 AS Decimal(18, 2)), CAST(1129000.00 AS Decimal(18, 2)), N'COD', N'Đang xử lý', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (28, 2, N'Nguyễn Văn An', N'10 Nguyễn Trãi, Quận 5, TP.HCM', N'0911111111', NULL, CAST(N'2025-10-24T21:34:37.590' AS DateTime), CAST(30000.00 AS Decimal(18, 2)), CAST(510000.00 AS Decimal(18, 2)), N'VNPAY', N'Đang xử lý', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (29, 2, N'Nguyễn Văn An', N'10 Nguyễn Trãi, Quận 5, TP.HCM', N'0911111111', NULL, CAST(N'2025-10-24T23:51:11.630' AS DateTime), CAST(30000.00 AS Decimal(18, 2)), CAST(310000.00 AS Decimal(18, 2)), N'VNPAY', N'Chờ thanh toán', N'FAST', NULL, NULL, NULL)
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (30, 2, N'Nguyễn Văn An', N'10 Nguyễn Trãi, Quận 5, TP.HCM', N'0911111111', NULL, CAST(N'2025-10-27T17:33:38.577' AS DateTime), CAST(0.00 AS Decimal(18, 2)), CAST(6287400.00 AS Decimal(18, 2)), N'COD', N'Đang xử lý', N'FAST', NULL, 1, CAST(698600.00 AS Decimal(18, 2)))
INSERT [dbo].[Orders] ([Id], [AccountId], [RecipientName], [ShippingAddress], [RecipientPhone], [Note], [OrderDate], [ShippingFee], [TotalAmount], [PaymentMethod], [Status], [ShippingMethod], [CompletedDate], [DiscountCodeId], [DiscountAmount]) VALUES (31, 2, N'Nguyễn Văn An', N'10 Nguyễn Trãi, Quận 5, TP.HCM', N'0911111111', NULL, CAST(N'2025-10-28T03:40:46.767' AS DateTime), CAST(60000.00 AS Decimal(18, 2)), CAST(312000.00 AS Decimal(18, 2)), N'VNPAY', N'Đang xử lý', N'EXPRESS', NULL, 1, CAST(28000.00 AS Decimal(18, 2)))
SET IDENTITY_INSERT [dbo].[Orders] OFF
GO
SET IDENTITY_INSERT [dbo].[Products] ON 

INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (1, N'Bó hoa hồng đỏ', N'Bó hoa tình yêu nồng cháy, biểu tượng của sự lãng mạn.', N'images/Bohoa/BohoaHongdo.png', CAST(550000.00 AS Decimal(18, 2)), CAST(499000.00 AS Decimal(18, 2)), 1, 1, 10)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (2, N'Bó hoa hồng trắng', N'Vẻ đẹp tinh khôi, trong sáng, phù hợp cho những khởi đầu mới.', N'images/bohoa/BohoaHongtrang.png', CAST(520000.00 AS Decimal(18, 2)), NULL, 1, 1, 25)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (3, N'Bó hoa tulip hồng', N'Sự ngọt ngào và lời chúc hạnh phúc gửi trao đến người thương.', N'images/Bohoa/BohoaTuliphong.png', CAST(650000.00 AS Decimal(18, 2)), CAST(599000.00 AS Decimal(18, 2)), 1, 1, 1)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (4, N'Bó hoa tulip vàng', N'Tượng trưng cho ánh nắng rạng rỡ và tình bạn chân thành.', N'images/Bohoa/BohoaTulipvang.png', CAST(620000.00 AS Decimal(18, 2)), NULL, 1, 1, 3)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (5, N'Bó hoa baby trắng', N'Tình yêu ngây thơ và vĩnh cửu, nhẹ nhàng và tinh tế.', N'images/Bohoa/BohoaBabytrang.png', CAST(450000.00 AS Decimal(18, 2)), CAST(400000.00 AS Decimal(18, 2)), 1, 1, 15)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (6, N'Bó hoa baby hồng', N'Sự dịu dàng, đáng yêu, món quà hoàn hảo cho phái nữ.', N'images/Bohoa/BohoaBabyhong.png', CAST(480000.00 AS Decimal(18, 2)), NULL, 1, 1, 23)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (7, N'Bó hoa hướng dương', N'Năng lượng tích cực và sự lạc quan vươn tới tương lai.', N'images/Bohoa/BohoaHuongduong.png', CAST(580000.00 AS Decimal(18, 2)), CAST(500000.00 AS Decimal(18, 2)), 1, 1, 10)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (8, N'Bó hoa cúc họa mi', N'Vẻ đẹp mộc mạc, giản dị của những bông cúc trắng tinh khôi.', N'images/Bohoa/BohoaCuchoami.png', CAST(350000.00 AS Decimal(18, 2)), CAST(329000.00 AS Decimal(18, 2)), 1, 1, 39)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (9, N'Bó hoa cẩm chướng hồng', N'Thể hiện lòng biết ơn và sự ngưỡng mộ sâu sắc.', N'images/Bohoa/BohoaCamchuonghong.png', CAST(480000.00 AS Decimal(18, 2)), NULL, 1, 1, 18)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (10, N'Bó hoa mix pastel', N'Sự kết hợp hài hòa của các loài hoa mang tông màu nhẹ nhàng.', N'images/Bohoa/BohoaMixpastel.png', CAST(750000.00 AS Decimal(18, 2)), NULL, 1, 1, 15)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (11, N'Giỏ hoa hồng pastel', N'Sự sang trọng và thanh lịch cho các dịp quan trọng.', N'images/Giohoa/GiohoaHongpastel.png', CAST(1200000.00 AS Decimal(18, 2)), NULL, 2, 1, 14)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (12, N'Giỏ hoa hồng đỏ', N'Món quà ấn tượng và ý nghĩa cho đối tác hoặc người thân.', N'images/Bohoa/BohoaHongdo.png', CAST(1350000.00 AS Decimal(18, 2)), NULL, 2, 1, NULL)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (13, N'Giỏ hoa hồng trắng', N'Sự trang trọng, tinh khôi, phù hợp cho các sự kiện chúc mừng.', N'images/Bohoa/BohoaHongtrang.png', CAST(1100000.00 AS Decimal(18, 2)), NULL, 2, 1, NULL)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (14, N'Giỏ hoa lan hồ điệp', N'Đẳng cấp, quý phái, món quà mừng khai trương hồng phát.', N'images/Giohoa/GiohoaLanhoDiep.png', CAST(2500000.00 AS Decimal(18, 2)), CAST(2399000.00 AS Decimal(18, 2)), 2, 0, NULL)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (15, N'Giỏ hoa ly vàng', N'Tượng trưng cho sự giàu sang, thịnh vượng và hạnh phúc.', N'images/Giohoa/GiohoaLy.png', CAST(950000.00 AS Decimal(18, 2)), NULL, 2, 1, NULL)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (16, N'Giỏ hoa tulip đỏ', N'Lời tỏ tình hoàn hảo và lãng mạn không thể chối từ.', N'images/Giohoa/GiohoaTulipdo.png', CAST(1150000.00 AS Decimal(18, 2)), NULL, 2, 1, NULL)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (17, N'Giỏ hoa cẩm tú cầu', N'Lòng biết ơn chân thành và sự quan tâm sâu sắc.', N'images/Giohoa/GiohoaCamtucau.png', CAST(850000.00 AS Decimal(18, 2)), NULL, 2, 1, NULL)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (18, N'Giỏ hoa sen', N'Sự thanh cao, tôn nghiêm, món quà đặc biệt cho người lớn tuổi.', N'images/Giohoa/GiohoaSen.png', CAST(1500000.00 AS Decimal(18, 2)), NULL, 2, 1, NULL)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (19, N'Giỏ hoa hướng dương rực rỡ', N'Mang lại niềm vui, sức sống và năng lượng cho ngày mới.', N'images/Bohoa/BohoaHuongduong.png', CAST(980000.00 AS Decimal(18, 2)), NULL, 2, 1, NULL)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (20, N'Giỏ hoa mix mùa xuân', N'Sự tươi mới, khởi đầu may mắn và thành công.', N'images/Giohoa/GiohoaMixmuaxuan.png', CAST(1300000.00 AS Decimal(18, 2)), CAST(1199000.00 AS Decimal(18, 2)), 2, 1, NULL)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (21, N'Chậu lan hồ điệp trắng', N'Vẻ đẹp sang trọng, bền lâu cho không gian sống và làm việc.', N'images/Chauhoa/ChauLanHodiepTrang.png', CAST(1800000.00 AS Decimal(18, 2)), NULL, 3, 0, NULL)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (22, N'Chậu lan hồ điệp vàng', N'Mang lại may mắn, tài lộc và sự thịnh vượng cho gia chủ.', N'images/Chauhoa/ChauLanHodiepVang.png', CAST(1900000.00 AS Decimal(18, 2)), NULL, 3, 0, NULL)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (23, N'Chậu lan tím', N'Sự thủy chung, son sắt, món quà kỷ niệm ngày cưới ý nghĩa.', N'images/Chauhoa/ChauLantim.png', CAST(1750000.00 AS Decimal(18, 2)), NULL, 3, 0, NULL)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (24, N'Chậu sen đá mini', N'Sức sống bền bỉ và mãnh liệt, phù hợp để bàn làm việc.', N'images/Giohoa/GiohoaSen.png', CAST(150000.00 AS Decimal(18, 2)), NULL, 3, 1, NULL)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (25, N'Chậu xương rồng nhỏ', N'Mạnh mẽ, kiên cường, món quà độc đáo cho bạn bè.', N'images/Chauhoa/ChauXuongrongnho.png', CAST(120000.00 AS Decimal(18, 2)), NULL, 3, 1, NULL)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (26, N'Chậu hồng tỉ muội', N'Tượng trưng cho tình chị em, tình bạn thân thiết, gắn kết.', N'images/Chauhoa/ChauHongtimuoi.png', CAST(350000.00 AS Decimal(18, 2)), NULL, 3, 1, NULL)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (27, N'Chậu hoa cúc mini', N'Mang lại niềm vui, sự lạc quan và không khí trong lành.', N'images/Chauhoa/ChauHoacucMini.png', CAST(250000.00 AS Decimal(18, 2)), NULL, 3, 1, NULL)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (28, N'Chậu lan ý', N'Giúp điều hòa không khí, mang lại sự bình yên và hạnh phúc.', N'images/Chauhoa/ChauLany.png', CAST(450000.00 AS Decimal(18, 2)), NULL, 3, 1, NULL)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (29, N'Chậu sống đời', N'Cầu chúc sức khỏe, sự trường thọ cho ông bà, cha mẹ.', N'images/Chauhoa/ChauSongdoi.png', CAST(220000.00 AS Decimal(18, 2)), NULL, 3, 1, 1)
INSERT [dbo].[Products] ([Id], [ProductName], [ProductDescription], [ImageUrl], [Price], [SalePrice], [CategoryId], [Status], [Sold]) VALUES (30, N'Chậu dạ yến thảo', N'Vẻ đẹp rực rỡ, sai hoa, tô điểm cho ban công và sân vườn.', N'images/Chauhoa/ChauDayenthao.png', CAST(280000.00 AS Decimal(18, 2)), NULL, 3, 1, 2)
SET IDENTITY_INSERT [dbo].[Products] OFF
GO
SET IDENTITY_INSERT [dbo].[Reviews] ON 

INSERT [dbo].[Reviews] ([Id], [ProductId], [AccountId], [Rating], [Comment], [CreatedAt]) VALUES (1, 1, 2, 5, N'Hoa rất đẹp, giao đúng giờ. Bạn gái mình rất thích!', CAST(N'2025-10-14T19:54:58.913' AS DateTime))
INSERT [dbo].[Reviews] ([Id], [ProductId], [AccountId], [Rating], [Comment], [CreatedAt]) VALUES (2, 12, 3, 5, N'Giỏ hoa sang trọng, shop tư vấn nhiệt tình. Sẽ ủng hộ lần sau.', CAST(N'2025-10-14T19:54:58.913' AS DateTime))
INSERT [dbo].[Reviews] ([Id], [ProductId], [AccountId], [Rating], [Comment], [CreatedAt]) VALUES (3, 19, 4, 4, N'Hoa tươi, nhiều nụ. Tuy nhiên shipper giao hơi trễ so với dự kiến.', CAST(N'2025-10-14T19:54:58.913' AS DateTime))
INSERT [dbo].[Reviews] ([Id], [ProductId], [AccountId], [Rating], [Comment], [CreatedAt]) VALUES (4, 7, 2, 5, N'Bó hoa hướng dương rực rỡ y như hình, rất hài lòng.', CAST(N'2025-10-14T19:54:58.913' AS DateTime))
INSERT [dbo].[Reviews] ([Id], [ProductId], [AccountId], [Rating], [Comment], [CreatedAt]) VALUES (5, 21, 4, 4, N'Chậu lan đẹp, nhưng giá hơi cao một chút.', CAST(N'2025-10-14T19:54:58.913' AS DateTime))
INSERT [dbo].[Reviews] ([Id], [ProductId], [AccountId], [Rating], [Comment], [CreatedAt]) VALUES (6, 14, 2, 3, N'Giỏ hoa lan hồ điệp bị dập mất một vài bông, hơi buồn.', CAST(N'2025-10-14T19:54:58.913' AS DateTime))
INSERT [dbo].[Reviews] ([Id], [ProductId], [AccountId], [Rating], [Comment], [CreatedAt]) VALUES (7, 24, 3, 5, N'Sen đá mini siêu cưng, để bàn làm việc hợp lý.', CAST(N'2025-10-14T19:54:58.913' AS DateTime))
INSERT [dbo].[Reviews] ([Id], [ProductId], [AccountId], [Rating], [Comment], [CreatedAt]) VALUES (8, 10, 4, 5, N'Hoa đẹp, tươi lâu, màu sắc hài hòa.', CAST(N'2025-10-14T19:54:58.913' AS DateTime))
INSERT [dbo].[Reviews] ([Id], [ProductId], [AccountId], [Rating], [Comment], [CreatedAt]) VALUES (9, 3, 2, 4, N'Bó hoa tulip hồng có vài lá bị úa, còn lại thì ổn.', CAST(N'2025-10-14T19:54:58.913' AS DateTime))
INSERT [dbo].[Reviews] ([Id], [ProductId], [AccountId], [Rating], [Comment], [CreatedAt]) VALUES (10, 6, 2, 5, N'sản phẩm tốt vô cùng', CAST(N'2025-10-21T22:49:22.307' AS DateTime))
INSERT [dbo].[Reviews] ([Id], [ProductId], [AccountId], [Rating], [Comment], [CreatedAt]) VALUES (11, 2, 2, 5, N'sanr pham xieu deth', CAST(N'2025-10-21T23:21:04.283' AS DateTime))
INSERT [dbo].[Reviews] ([Id], [ProductId], [AccountId], [Rating], [Comment], [CreatedAt]) VALUES (12, 2, 2, 4, N'mmmmmmmmmmmmmmmm', CAST(N'2025-10-21T23:21:12.543' AS DateTime))
INSERT [dbo].[Reviews] ([Id], [ProductId], [AccountId], [Rating], [Comment], [CreatedAt]) VALUES (14, 8, 2, 5, N'aaaaaaaaaaaaaaaaaaaaaaaa', CAST(N'2025-10-21T23:40:11.240' AS DateTime))
INSERT [dbo].[Reviews] ([Id], [ProductId], [AccountId], [Rating], [Comment], [CreatedAt]) VALUES (15, 8, 2, 4, N'aaaaaaaaaaaaaaaaaaaaaaaaa', CAST(N'2025-10-21T23:40:16.637' AS DateTime))
INSERT [dbo].[Reviews] ([Id], [ProductId], [AccountId], [Rating], [Comment], [CreatedAt]) VALUES (16, 8, 2, 3, N'aaaaaaaaaaaaaaaaaaaaaa', CAST(N'2025-10-21T23:48:44.127' AS DateTime))
INSERT [dbo].[Reviews] ([Id], [ProductId], [AccountId], [Rating], [Comment], [CreatedAt]) VALUES (17, 8, 2, 3, N'3333333333333333333333333', CAST(N'2025-10-21T23:48:50.053' AS DateTime))
INSERT [dbo].[Reviews] ([Id], [ProductId], [AccountId], [Rating], [Comment], [CreatedAt]) VALUES (18, 8, 2, 3, N'333333333333333333333333', CAST(N'2025-10-21T23:48:55.783' AS DateTime))
INSERT [dbo].[Reviews] ([Id], [ProductId], [AccountId], [Rating], [Comment], [CreatedAt]) VALUES (19, 8, 2, 2, N'3333333333333333333333', CAST(N'2025-10-21T23:49:00.547' AS DateTime))
INSERT [dbo].[Reviews] ([Id], [ProductId], [AccountId], [Rating], [Comment], [CreatedAt]) VALUES (20, 8, 2, 1, N'1111111111111111111', CAST(N'2025-10-22T21:57:47.600' AS DateTime))
SET IDENTITY_INSERT [dbo].[Reviews] OFF
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__Accounts__A9D10534C6022C3A]    Script Date: 10/28/2025 3:39:48 PM ******/
ALTER TABLE [dbo].[Accounts] ADD UNIQUE NONCLUSTERED 
(
	[Email] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__Categori__8517B2E0D1AE262B]    Script Date: 10/28/2025 3:39:48 PM ******/
ALTER TABLE [dbo].[Categories] ADD UNIQUE NONCLUSTERED 
(
	[CategoryName] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
SET ANSI_PADDING ON
GO
/****** Object:  Index [UQ__Discount__A25C5AA7277BE07F]    Script Date: 10/28/2025 3:39:48 PM ******/
ALTER TABLE [dbo].[DiscountCodes] ADD UNIQUE NONCLUSTERED 
(
	[Code] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
/****** Object:  Index [UQ__OrderRet__C3905BCEAC913C0C]    Script Date: 10/28/2025 3:39:48 PM ******/
ALTER TABLE [dbo].[OrderReturnRequests] ADD UNIQUE NONCLUSTERED 
(
	[OrderId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
GO
ALTER TABLE [dbo].[Accounts] ADD  DEFAULT ('customer') FOR [Role]
GO
ALTER TABLE [dbo].[Accounts] ADD  DEFAULT (getdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[ChatMessages] ADD  DEFAULT (getdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[ChatMessages] ADD  DEFAULT ((0)) FOR [IsRead]
GO
ALTER TABLE [dbo].[DiscountCodes] ADD  DEFAULT ((0)) FOR [CurrentUsage]
GO
ALTER TABLE [dbo].[DiscountCodes] ADD  DEFAULT ((1)) FOR [IsActive]
GO
ALTER TABLE [dbo].[Orders] ADD  DEFAULT (getdate()) FOR [OrderDate]
GO
ALTER TABLE [dbo].[Orders] ADD  DEFAULT ((0)) FOR [ShippingFee]
GO
ALTER TABLE [dbo].[Orders] ADD  DEFAULT (N'Đang xử lý') FOR [Status]
GO
ALTER TABLE [dbo].[Orders] ADD  CONSTRAINT [DF_Orders_ShippingMethod]  DEFAULT ('FAST') FOR [ShippingMethod]
GO
ALTER TABLE [dbo].[Orders] ADD  CONSTRAINT [DF_Orders_DiscountAmount]  DEFAULT ((0)) FOR [DiscountAmount]
GO
ALTER TABLE [dbo].[Reviews] ADD  DEFAULT (getdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[Wishlist] ADD  DEFAULT (getdate()) FOR [CreatedAt]
GO
ALTER TABLE [dbo].[ChatMessages]  WITH CHECK ADD FOREIGN KEY([ReceiverId])
REFERENCES [dbo].[Accounts] ([Id])
GO
ALTER TABLE [dbo].[ChatMessages]  WITH CHECK ADD FOREIGN KEY([SenderId])
REFERENCES [dbo].[Accounts] ([Id])
GO
ALTER TABLE [dbo].[OrderDetails]  WITH CHECK ADD FOREIGN KEY([OrderId])
REFERENCES [dbo].[Orders] ([Id])
GO
ALTER TABLE [dbo].[OrderDetails]  WITH CHECK ADD FOREIGN KEY([ProductId])
REFERENCES [dbo].[Products] ([Id])
GO
ALTER TABLE [dbo].[OrderReturnRequests]  WITH CHECK ADD  CONSTRAINT [FK_OrderReturnRequests_Orders] FOREIGN KEY([OrderId])
REFERENCES [dbo].[Orders] ([Id])
GO
ALTER TABLE [dbo].[OrderReturnRequests] CHECK CONSTRAINT [FK_OrderReturnRequests_Orders]
GO
ALTER TABLE [dbo].[Orders]  WITH CHECK ADD FOREIGN KEY([AccountId])
REFERENCES [dbo].[Accounts] ([Id])
GO
ALTER TABLE [dbo].[Orders]  WITH CHECK ADD  CONSTRAINT [FK_Orders_DiscountCodes] FOREIGN KEY([DiscountCodeId])
REFERENCES [dbo].[DiscountCodes] ([Id])
ON DELETE SET NULL
GO
ALTER TABLE [dbo].[Orders] CHECK CONSTRAINT [FK_Orders_DiscountCodes]
GO
ALTER TABLE [Dbo].[Product_Promotions]  WITH CHECK ADD FOREIGN KEY([ProductId])
REFERENCES [dbo].[Products] ([Id])
GO
ALTER TABLE [dbo].[Product_Promotions]  WITH CHECK ADD FOREIGN KEY([PromotionId])
REFERENCES [dbo].[Promotions] ([Id])
GO
ALTER TABLE [dbo].[Products]  WITH CHECK ADD FOREIGN KEY([CategoryId])
REFERENCES [dbo].[Categories] ([Id])
GO
ALTER TABLE [dbo].[Reviews]  WITH CHECK ADD FOREIGN KEY([AccountId])
REFERENCES [dbo].[Accounts] ([Id])
GO
ALTER TABLE [dbo].[Reviews]  WITH CHECK ADD FOREIGN KEY([ProductId])
REFERENCES [dbo].[Products] ([Id])
GO
ALTER TABLE [dbo].[Wishlist]  WITH CHECK ADD  CONSTRAINT [FK_Wishlist_Account] FOREIGN KEY([AccountId])
REFERENCES [dbo].[Accounts] ([Id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[Wishlist] CHECK CONSTRAINT [FK_Wishlist_Account]
GO
ALTER TABLE [dbo].[Wishlist]  WITH CHECK ADD  CONSTRAINT [FK_Wishlist_Product] FOREIGN KEY([ProductId])
REFERENCES [dbo].[Products] ([Id])
ON DELETE CASCADE
GO
ALTER TABLE [dbo].[Wishlist] CHECK CONSTRAINT [FK_Wishlist_Product]
GO
ALTER TABLE [dbo].[OrderDetails]  WITH CHECK ADD CHECK  (([Quantity]>(0)))
GO
ALTER TABLE [dbo].[Reviews]  WITH CHECK ADD CHECK  (([Rating]>=(1) AND [Rating]<=(5)))
GO
USE [master]
GO
ALTER DATABASE [FlowerShopDB] SET  READ_WRITE 
GO