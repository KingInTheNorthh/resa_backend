# RESA Backend - Postman Testing Guide

This document describes how to exercise all API features exposed by this codebase using Postman.

## Prerequisites

- Java and Maven installed
- A running PostgreSQL database
- Environment variables (used by `src/main/resources/application.yml`):
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`
  - `JWT_SECRET` (must be at least 32 chars for HS256)
  - `GOOGLE_OAUTH_CLIENT_ID` and `GOOGLE_OAUTH_CLIENT_SECRET` (for OAuth)
  - Optional owner account seed:
    - `APP_OWNER_EMAIL` (default `owner@example.com`)
    - `APP_OWNER_PASSWORD` (default `change-me-strong`)

Start the app:

```bash
mvn spring-boot:run
```

Base URL:

```
http://localhost:8080
```

Postman tips:

- Create an environment with `baseUrl` and `jwt`.
- For authenticated routes, set header `Authorization: Bearer {{jwt}}`.
- The auth endpoints return `{ "token": "..." }`.

## 1) Health Check

```
GET {{baseUrl}}/actuator/health
```

Expected: `200 OK` with status JSON.

## 2) Auth - Email/Password

### Register Customer

```
POST {{baseUrl}}/api/auth/register
Content-Type: application/json

{
  "email": "customer1@example.com",
  "password": "password123"
}
```

Copy the `token` into `{{jwt}}`.

### Register Seller

```
POST {{baseUrl}}/api/auth/register-seller
Content-Type: application/json

{
  "email": "seller1@example.com",
  "password": "password123"
}
```

Sellers start as `seller_verified=false` and cannot create products until approved by the owner.

### Login (Customer/Seller/Owner)

```
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "email": "owner@example.com",
  "password": "change-me-strong"
}
```

Copy the `token` into `{{jwt}}` for subsequent requests.

## 3) Auth - OAuth (Google)

OAuth is configured for Google and returns a JWT in the response body after successful login.

1. Open a browser (or use Postman’s built-in browser) and visit:

```
GET {{baseUrl}}/oauth2/authorization/google
```

2. Complete the Google sign-in.
3. The server responds with JSON like:

```json
{ "token": "<jwt>" }
```

4. Copy the token into `{{jwt}}`.

Notes:
- The JWT subject is the user email; no user ID is included.
- The OAuth user is auto-created with role `CUSTOMER`.

## 4) Admin (Owner Only)

The owner account is created on startup using `APP_OWNER_EMAIL` and `APP_OWNER_PASSWORD`.

### List Sellers

```
GET {{baseUrl}}/api/admin/sellers
Authorization: Bearer {{jwt}}
```

### Approve Seller

```
POST {{baseUrl}}/api/admin/sellers/{sellerId}/approve
Authorization: Bearer {{jwt}}
```

You can get `{sellerId}` from the list sellers response or by querying the database.

## 5) Products

### List Products (Public)

```
GET {{baseUrl}}/api/products
```

### Get Product by ID (Public)

```
GET {{baseUrl}}/api/products/{productId}
```

### Create Product (Owner or Verified Seller)

```
POST {{baseUrl}}/api/products
Authorization: Bearer {{jwt}}
Content-Type: multipart/form-data

Form fields:
- product (application/json):
  {
    "name": "Canvas Tote",
    "description": "Heavy canvas, reinforced straps",
    "price": 49.99,
    "stockQuantity": 100
  }
- images (file): one or more image files (repeat `images` for multiple)
```

Cloud storage setup (required for this app now):
- Set `APP_STORAGE_TYPE=cloudinary` (default is already `cloudinary` in `application.yml`).
- Set `CLOUDINARY_URL=cloudinary://<api_key>:<api_secret>@<cloud_name>`.
- Optional: `CLOUDINARY_FOLDER=product-images`.

If the seller is not verified, the API returns `403 Forbidden`.
If you send JSON only, the API returns `415 Unsupported Media Type` because this
endpoint expects multipart form data with both the JSON and image file(s).

Postman tip:
- Body → `form-data`
- Add `product` with type `Text`, then paste the JSON body above
- Add `images` with type `File` and choose an image
- For multiple images, add more `images` keys (one per file)

### End-to-end test: seller creates a product with images, customer views it

1) Configure Cloudinary (if you want cloud storage instead of local):
   - `APP_STORAGE_TYPE=cloudinary`
   - `CLOUDINARY_URL=cloudinary://<api_key>:<api_secret>@<cloud_name>`
   - Optional: `CLOUDINARY_FOLDER=product-images`

2) Login as owner and approve a seller (required before seller can create products):

```
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "email": "owner@example.com",
  "password": "change-me-strong"
}
```

```
GET {{baseUrl}}/api/admin/sellers
Authorization: Bearer {{jwt}}
```

```
POST {{baseUrl}}/api/admin/sellers/{sellerId}/approve
Authorization: Bearer {{jwt}}
```

3) Login as the approved seller, then create the product with a description and image:

```
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "email": "seller1@example.com",
  "password": "password123"
}
```

```
POST {{baseUrl}}/api/products
Authorization: Bearer {{jwt}}
Content-Type: multipart/form-data

Form fields:
- product (application/json):
  {
    "name": "Canvas Tote",
    "description": "Heavy canvas, reinforced straps",
    "price": 49.99,
    "stockQuantity": 100
  }
- images (file): choose one image file
```

4) Login as a customer and verify the product appears with an image:

```
POST {{baseUrl}}/api/auth/login
Content-Type: application/json

{
  "email": "customer1@example.com",
  "password": "password123"
}
```

```
GET {{baseUrl}}/api/products
```

The response includes an `images` array with URLs like:

```
/api/products/images/{imageId}
```

### List My Products (Seller Only)

```
GET {{baseUrl}}/api/seller/products
Authorization: Bearer {{jwt}}
```

## 6) Orders

Create order requires a buyer ID and a shipping address ID. There are no API endpoints
in this project to create addresses or fetch the current user ID, so you will need to
insert data directly or read IDs from the database.

### Example SQL to find IDs

```sql
-- Find user ID by email
SELECT id, email, role, seller_verified FROM app_users WHERE email = 'customer1@example.com';

-- Insert address for a user (replace user_id)
INSERT INTO addresses (
  user_id, label, line1, line2, city, region, postal_code, country, phone_number, is_default
) VALUES (
  1, 'Home', '123 Main St', NULL, 'Lagos', 'LA', '100001', 'NG', '+2348000000000', TRUE
);

-- Get address ID
SELECT id, user_id, label FROM addresses WHERE user_id = 1;
```

### Create Order (Customer Only)

```
POST {{baseUrl}}/api/orders
Authorization: Bearer {{jwt}}
Content-Type: application/json

{
  "buyerId": 1,
  "shippingAddressId": 10,
  "items": [
    { "productId": 1, "quantity": 2 },
    { "productId": 2, "quantity": 1 }
  ]
}
```

### List Orders (Owner Only)

```
GET {{baseUrl}}/api/orders
Authorization: Bearer {{jwt}}
```

### Get Order (Owner Only)

```
GET {{baseUrl}}/api/orders/{orderId}
Authorization: Bearer {{jwt}}
```

## 7) Notes on Authorization and Roles

- `CUSTOMER`: can create orders.
- `SELLER`: can list own products; must be verified to create products.
- `OWNER`: can list/approve sellers and view all orders; can create products.

## 8) Feature Coverage Summary

Exposed API features covered above:

- User registration (customer, seller)
- Email/password login
- OAuth login (Google)
- Seller approval (owner)
- Product listing and creation
- Seller product listing
- Order creation and owner order management

If you add endpoints for addresses, carts, or reviews later, add them to this guide.
