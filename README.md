### **1. The Hand-Off: `README.md**`

Create a file named `README.md` in your **Root Folder** and paste this code. It gives your team the "One-Click Start."

```markdown
# 🛒 E-Commerce Microservices Platform

A distributed e-commerce system built with Spring Boot, PostgreSQL, and Docker.

## 🚀 Quick Start (How to Run)

**Prerequisites:**
* Docker & Docker Compose installed.
* Java 17 or 21 (only if you want to run locally without Docker).

### 1. Configure Secrets
Create a `.env` file in the root directory (same level as this README):
```env
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

```

### 2. Launch the System

Open a terminal in the root folder and run:

```bash
docker-compose up --build

```

*Wait until you see "Netty started on port 8080" in the logs.*

### 3. Access Points

* **API Gateway:** `http://localhost:8080` (Use this for all requests)
* **Auth Service:** `http://localhost:8080/auth`
* **Product Service:** `http://localhost:8080/products`
* **Database:** `localhost:5432` (User: `admin`, Pass: `password`)

---

## 🛠 Service Architecture

| Service | Port (Internal) | Database | Description |
| --- | --- | --- | --- |
| **API Gateway** | `8080` | N/A | Entry point. Routes /auth/** and /products/**. |
| **Auth Service** | `8081` | `auth_db` | Handles Login/Register & JWT Tokens. |
| **Product Service** | `8082` | `product_db` | Manages Catalog & Image Uploads. |
| **PostgreSQL** | `5432` | Multiple | Contains separate DBs for each service. |

## 🧪 Testing with Postman

1. **Register:** `POST /auth/register` -> `{"username": "test", "email": "test@test.com", "password": "pass"}`
2. **Login:** `POST /auth/token` -> `{"username": "test", "password": "pass"}`
3. **Get Products:** `GET /products` (Requires Bearer Token)

```

---

