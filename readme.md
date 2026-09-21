# Dự Án Tối Giản: Spring Boot + React (Import Trực Tiếp)

Cấu trúc siêu nhẹ, không cần `node_modules`, không cần `npm`:

---

## 📁 Cấu Trúc File

```
ATTT---T9/
├── backend/
│   ├── pom.xml                                               # Quản lý Spring Boot
│   ├── mvnw / mvnw.cmd                                       # Maven Wrapper chạy trực tiếp
│   └── src/main/java/com/example/demo/
│       ├── DemoApplication.java                              # Spring Boot Main
│       └── resources/application.properties                  # Cấu hình port 8080
└── frontend/                                                 # React import trực tiếp qua CDN (0 MB cài đặt)
    ├── index.html                                            # File HTML import React & Babel
    ├── app.js                                                # Code React JSX chính
    └── style.css                                             # CSS giao diện
```

---

## 🚀 Cách Chạy

### 1. Frontend:
- Click đúp chuột mở trực tiếp file [`frontend/index.html`](file:///d:/ATTT---T9/frontend/index.html) bằng trình duyệt (hoặc dùng Live Server).
- Không cần cài `npm`, không cần `node_modules`.

### 2. Backend:
- Mở terminal:
  ```powershell
  cd backend
  .\mvnw.cmd spring-boot:run
  ```
  *(Hoặc mở và nhấn Run trong IntelliJ/Eclipse/VS Code)*
