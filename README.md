# Cart Service - E-commerce Microservices

Shopping Cart Service cho hệ thống E-commerce Fashion Microservices

## 🚀 Công nghệ sử dụng

- **Framework**: Spring Boot 4.0.3
- **Java**: 17
- **Database**: PostgreSQL 15
- **Cache**: Redis 7
- **Security**: OAuth2 Resource Server (Keycloak)
- **Build Tool**: Maven
- **Port**: 8083

## 📋 Tính năng

✅ Quản lý giỏ hàng cho người dùng đã đăng nhập
✅ Quản lý giỏ hàng cho khách (guest cart)
✅ Thêm/Cập nhật/Xóa sản phẩm trong giỏ
✅ Tính toán tổng giá trị đơn hàng
✅ Cache Redis cho hiệu suất cao
✅ Merge giỏ hàng guest -> user khi đăng nhập
✅ Kiểm tra tồn kho với Product Service
✅ Bảo mật với JWT từ Keycloak

## 📦 Cấu trúc dự án

```
src/main/java/com/ecommerce/cartservice/
├── CartserviceApplication.java          # Main application
├── config/                               # Cấu hình (3 files)
│   ├── SecurityConfig.java              # OAuth2 security
│   ├── RedisConfig.java                 # Redis cache
│   └── WebClientConfig.java             # WebClient cho gọi API
├── entity/                               # JPA entities (2 files)
│   ├── Cart.java
│   └── CartItem.java
├── repository/                           # JPA repositories (2 files)
│   ├── CartRepository.java
│   └── CartItemRepository.java
├── dto/                                  # Data Transfer Objects
│   ├── request/ (4 files)
│   │   ├── AddToCartRequest.java
│   │   ├── UpdateCartItemRequest.java
│   │   ├── ApplyCouponRequest.java
│   │   └── MergeCartRequest.java
│   └── response/ (5 files)
│       ├── ApiResponse.java
│       ├── CartResponse.java
│       ├── CartItemResponse.java
│       ├── CartSummaryResponse.java
│       └── ProductResponse.java
├── model/                                # Redis models (2 files)
│   ├── RedisCart.java
│   └── RedisCartItem.java
├── client/                               # Service clients (1 file)
│   └── ProductServiceClient.java
├── service/                              # Business logic (3 files)
│   ├── CartService.java
│   ├── CartItemService.java
│   └── RedisCartService.java
├── controller/                           # REST controllers (1 file)
│   └── CartController.java
├── mapper/                               # MapStruct mapper (1 file)
│   └── CartMapper.java
└── exception/                            # Exception handling (3 files)
    ├── ResourceNotFoundException.java
    ├── BadRequestException.java
    └── GlobalExceptionHandler.java

Tổng: 28 files
```

## 🔧 Cài đặt và chạy

### 1. Yêu cầu

- Java 17+
- Maven 3.8+
- Docker & Docker Compose
- Product Service đang chạy ở port 8082
- Keycloak đang chạy ở port 8080

### 2. Khởi động Database & Redis

```bash
docker-compose up -d
```

Kiểm tra:
```bash
docker ps | grep -E "cart-postgres|cart-redis"
```

### 3. Build project

```bash
mvn clean install
```

### 4. Chạy service

```bash
mvn spring-boot:run
```

Hoặc:
```bash
java -jar target/cartservice-0.0.1-SNAPSHOT.jar
```

## 🔍 Kiểm tra

### Health Check
```bash
curl http://localhost:8083/actuator/health
```

### Get Cart (cần JWT token)
```bash
curl -H "Authorization: Bearer <JWT_TOKEN>" \
     http://localhost:8083/api/cart
```

### Add to Cart
```bash
curl -X POST http://localhost:8083/api/cart/items \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "product-uuid",
    "quantity": 2
  }'
```

## 📡 API Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/api/cart` | Lấy giỏ hàng hiện tại | ✅ |
| GET | `/api/cart/guest/{sessionId}` | Lấy giỏ hàng guest | ❌ |
| POST | `/api/cart/items` | Thêm sản phẩm vào giỏ | ✅ |
| PUT | `/api/cart/items/{itemId}` | Cập nhật số lượng | ✅ |
| DELETE | `/api/cart/items/{itemId}` | Xóa sản phẩm | ✅ |
| DELETE | `/api/cart` | Xóa toàn bộ giỏ hàng | ✅ |
| GET | `/api/cart/summary` | Lấy tổng kết giỏ hàng | ✅ |
| POST | `/api/cart/merge` | Merge guest cart -> user cart | ✅ |

Chi tiết API xem file: `apicontract.md`

## ⚙️ Cấu hình

Xem file `src/main/resources/application.yaml`:

- **Database**: PostgreSQL trên port 5433
- **Redis**: Redis trên port 6380
- **JWT**: Keycloak tại `http://localhost:8080`
- **Product Service**: `http://localhost:8082`

## 🗄️ Database Schema

### Table: carts
```sql
- id (UUID, PK)
- user_id (VARCHAR, NOT NULL)
- session_id (VARCHAR)
- status (VARCHAR(20), NOT NULL)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

### Table: cart_items
```sql
- id (UUID, PK)
- cart_id (UUID, FK -> carts)
- product_id (VARCHAR, NOT NULL)
- product_variant_id (VARCHAR)
- quantity (INTEGER, NOT NULL)
- price (DECIMAL(19,2), NOT NULL)
- product_name (VARCHAR(500))
- product_image_url (VARCHAR(500))
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)
```

## 🔄 Luồng hoạt động

### 1. Guest Cart
1. User chưa đăng nhập thêm sản phẩm vào giỏ
2. Tạo cart với `sessionId` (UUID random)
3. Lưu vào PostgreSQL và cache Redis

### 2. User Login và Merge Cart
1. User đăng nhập -> nhận JWT token
2. Frontend gọi API `/api/cart/merge` với `guestSessionId`
3. Backend merge items từ guest cart vào user cart
4. Xóa guest cart

### 3. Add to Cart
1. Gọi Product Service kiểm tra sản phẩm và tồn kho
2. Thêm/cập nhật item trong database
3. Sync cache Redis
4. Return cart response

## 📦 Dependencies chính

```xml
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-data-redis
- spring-boot-starter-security
- spring-boot-starter-oauth2-resource-server
- spring-boot-starter-webflux (cho WebClient)
- postgresql
- lombok
- mapstruct (1.5.5.Final)
```

## 🧪 Testing

```bash
mvn test
```

## 📝 Logs

Logs được cấu hình ở mức DEBUG cho:
- `com.ecommerce.cartservice`
- `org.springframework.security`
- `org.springframework.data.redis`

## 🐛 Troubleshooting

### Lỗi kết nối PostgreSQL
```bash
# Kiểm tra container
docker logs cart-postgres

# Restart
docker-compose restart postgres-cart
```

### Lỗi kết nối Redis
```bash
# Kiểm tra container
docker logs cart-redis

# Test connection
docker exec -it cart-redis redis-cli ping
```

### Lỗi JWT Authentication
- Kiểm tra Keycloak đang chạy
- Verify `issuer-uri` trong application.yaml
- Check JWT token còn hạn

## 📚 Tài liệu tham khảo

- [API Contract](apicontract.md)
- [Setup Guide](setup.md)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Redis Documentation](https://redis.io/documentation)

## 👥 Liên hệ

Dự án: E-commerce Fashion Microservices
Service: Cart Service
Port: 8083

---

**Status**: ✅ Completed - All 28 files implemented

