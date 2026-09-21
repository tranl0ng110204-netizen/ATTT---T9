# Dự án Spring Boot + React (Cấu trúc Tinh Gọn)

Dự án được thiết lập tinh gọn, nhẹ máy và dễ phát triển:
- **Backend**: Java 21 + Spring Boot 3 (chỉ dùng Web Starter cơ bản).
- **Frontend**: React + Vite (không phụ thuộc thư viện rườm rà).

---

## 🚀 Hướng Dẫn Chạy Dự Án

### 1. Chạy Backend (Spring Boot - Port 8080)
Mở một cửa sổ Terminal:
```bash
cd backend
./mvnw.cmd spring-boot:run
```
*(Hoặc mở thư mục `backend` bằng IntelliJ IDEA / Eclipse / VS Code và nhấn Run `DemoApplication.java`)*

- API kiểm tra: `http://localhost:8080/api/hello`

---

### 2. Chạy Frontend (React + Vite - Port 5173)
Mở một cửa sổ Terminal khác:
```bash
cd frontend
npm run dev
```
- Mở trình duyệt tại: `http://localhost:5173`

---

## 📁 Cấu Trúc File Chính
```
ATTT---T9/
├── backend/
│   ├── pom.xml                                               # Quản lý dependency Maven tối giản
│   └── src/main/java/com/example/demo/
│       ├── DemoApplication.java                              # Điểm khởi chạy Spring Boot
│       └── controller/ApiController.java                     # Viết các API REST tại đây
└── frontend/
    ├── package.json                                          # Quản lý thư viện React/Vite
    └── src/
        ├── App.jsx                                           # Giao diện chính kết nối Backend
        └── App.css                                           # Giao diện CSS thuần nhẹ nhàng
```
