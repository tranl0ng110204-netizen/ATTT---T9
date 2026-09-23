import axios from 'axios';

// 1. Hệ thống Event Bus phát thông báo Toast tập trung
export const toastEmitter = {
  listeners: [],
  subscribe(fn) {
    this.listeners.push(fn);
    return () => {
      this.listeners = this.listeners.filter((listener) => listener !== fn);
    };
  },
  show(title, message, type = 'info', duration = 4500) {
    this.listeners.forEach((fn) =>
      fn({ id: Date.now() + Math.random(), title, message, type, duration })
    );
  },
};

// 2. Quản lý Auth Token & Thông tin phiên đăng nhập
export const authStorage = {
  getToken: () => localStorage.getItem('access_token') || '',
  setToken: (token) => {
    localStorage.setItem('access_token', token);
    window.dispatchEvent(new Event('auth-change'));
  },
  removeToken: () => {
    localStorage.removeItem('access_token');
    localStorage.removeItem('user_info');
    window.dispatchEvent(new Event('auth-change'));
  },
  getUser: () => {
    const raw = localStorage.getItem('user_info');
    return raw ? JSON.parse(raw) : { name: 'Chuyên viên ATTT', role: 'ROLE_ADMIN', email: 'admin@attt.edu.vn' };
  },
};

// 3. Khởi tạo Axios Instance chính của hệ thống
export const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ==============================================================================
// 4. REQUEST INTERCEPTOR: Tự động đính kèm JWT Bearer Token
// ==============================================================================
apiClient.interceptors.request.use(
  (config) => {
    const token = authStorage.getToken();
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    toastEmitter.show('Lỗi khởi tạo yêu cầu', 'Không thể tạo request gửi tới máy chủ.', 'error');
    return Promise.reject(error);
  }
);

// ==============================================================================
// 5. RESPONSE INTERCEPTOR: Bắt lỗi API tập trung
// ==============================================================================
apiClient.interceptors.response.use(
  (response) => {
    return response.data;
  },
  (error) => {
    if (error.response) {
      const status = error.response.status;
      const data = error.response.data;
      const message = data?.message || data?.error || 'Đã xảy ra lỗi khi xử lý yêu cầu.';

      switch (status) {
        case 400:
          toastEmitter.show('400 - Dữ liệu không hợp lệ', message, 'warning');
          break;

        case 401:
          toastEmitter.show('401 - Hết phiên đăng nhập', 'Phiên làm việc đã hết hạn. Vui lòng đăng nhập lại.', 'error');
          authStorage.removeToken();
          break;

        case 403:
          toastEmitter.show('403 - Từ chối truy cập', 'Bạn không có quyền hạn truy cập tài nguyên này.', 'error');
          break;

        case 404:
          toastEmitter.show('404 - Không tìm thấy', message || 'Tài nguyên yêu cầu không tồn tại trên máy chủ.', 'warning');
          break;

        case 422:
          toastEmitter.show('422 - Lỗi nghiệp vụ', message, 'warning');
          break;

        case 500:
          toastEmitter.show('500 - Lỗi máy chủ', 'Hệ thống máy chủ gặp sự cố nội bộ. Vui lòng thử lại sau.', 'error');
          break;

        case 502:
        case 503:
        case 504:
          toastEmitter.show(`${status} - Máy chủ không khả dụng`, 'Dịch vụ máy chủ tạm thời gián đoạn hoặc quá tải.', 'error');
          break;

        default:
          toastEmitter.show(`Lỗi ${status}`, message, 'error');
          break;
      }
    } else if (error.request) {
      toastEmitter.show(
        'Mất kết nối máy chủ',
        'Không thể kết nối đến Backend (http://localhost:8080). Hãy đảm bảo ứng dụng Spring Boot đang chạy.',
        'error',
        6000
      );
    } else {
      toastEmitter.show('Lỗi hệ thống', error.message || 'Đã xảy ra lỗi không xác định.', 'error');
    }

    return Promise.reject(error);
  }
);

// Gán thêm ra window để linh hoạt
if (typeof window !== 'undefined') {
  window.apiClient = apiClient;
  window.toastEmitter = toastEmitter;
  window.authStorage = authStorage;
}

export default apiClient;
