# API CONTRACT - CART SERVICE

**Backend URL:** `http://localhost:8083`  
**Version:** 1.0.0

---

## 📡 API RESPONSE FORMAT

All APIs return:
```typescript
{
  success: boolean;
  message?: string;
  data: T;
  timestamp: string;
}
```

---

## 🔐 AUTHENTICATION

All endpoints require JWT token from User Service:
```
Headers:
Authorization: Bearer {accessToken}
```

---

## 🛒 CART APIs

### GET /api/cart
Get current user's cart

**Response:**
```typescript
{
  success: true,
  data: {
    id: string;
    userId: string;
    sessionId: string;
    status: "ACTIVE" | "ABANDONED" | "CONVERTED_TO_ORDER";
    items: [
      {
        id: string;
        productId: string;
        productVariantId: string | null;
        productName: string;
        productImageUrl: string;
        quantity: number;
        price: number;
        total: number;
        inStock: boolean;
        createdAt: string;
      }
    ];
    totalItems: number;
    subtotal: number;
    discount: number;
    total: number;
    createdAt: string;
    updatedAt: string;
  }
}
```

### GET /api/cart/guest/{sessionId}
Get guest cart

### POST /api/cart/items
Add item to cart

**Request:**
```typescript
{
  productId: string;      // Required
  quantity: number;       // Required, 1-100
  productVariantId?: string;
}
```

**Response:** CartResponse

### PUT /api/cart/items/{itemId}
Update cart item quantity

**Request:**
```typescript
{
  quantity: number;  // 0 to remove
}
```

### DELETE /api/cart/items/{itemId}
Remove item from cart

**Response:** 204 No Content

### DELETE /api/cart
Clear all items

### GET /api/cart/summary
Get cart summary with totals

**Response:**
```typescript
{
  success: true,
  data: {
    totalItems: number;
    subtotal: number;
    discount: number;
    shipping: number;
    tax: number;
    total: number;
  }
}
```

### POST /api/cart/merge
Merge guest cart to user cart (after login)

**Request:**
```typescript
{
  guestSessionId: string;
}
```

---

## 📝 TYPESCRIPT TYPES

```typescript
interface Cart {
  id: string;
  userId: string;
  sessionId: string;
  status: 'ACTIVE' | 'ABANDONED' | 'CONVERTED_TO_ORDER';
  items: CartItem[];
  totalItems: number;
  subtotal: number;
  discount: number;
  total: number;
  createdAt: string;
  updatedAt: string;
}

interface CartItem {
  id: string;
  productId: string;
  productVariantId: string | null;
  productName: string;
  productImageUrl: string;
  quantity: number;
  price: number;
  total: number;
  inStock: boolean;
  createdAt: string;
}

interface CartSummary {
  totalItems: number;
  subtotal: number;
  discount: number;
  shipping: number;
  tax: number;
  total: number;
}
```