import React from 'react';
import { toastEmitter } from '../services/api';

export default function Header({ isCollapsed, setIsCollapsed, activeTab }) {
  const getTabTitle = () => {
    switch (activeTab) {
      case 'dashboard': return { title: 'Bảng Giám Sát An Toàn Thông Tin', sub: 'Theo dõi lưu lượng truy cập và cảnh báo an ninh thời gian thực' };
      case 'users': return { title: 'Quản Lý Người Dùng & Phân Quyền', sub: 'Kiểm soát tài khoản, vai trò và quyền hạn truy cập (RBAC)' };
      case 'logs': return { title: 'Nhật Ký Kiểm Toán & Sự Cố An Ninh', sub: 'Ghi nhận và phân tích lịch sử các cuộc gọi API' };
      case 'security': return { title: 'Chính Sách & Tham Số Bảo Mật', sub: 'Cấu hình thời hạn JWT, quy tắc tường lửa và bảo vệ tài nguyên' };
      default: return { title: 'Hệ Thống ATTT', sub: 'Dashboard' };
    }
  };

  const { title, sub } = getTabTitle();

  return (
    <header className="header">
      <div className="header-left">
        <button 
          className="toggle-btn" 
          onClick={() => setIsCollapsed(!isCollapsed)}
          title="Thu gọn / Mở rộng Sidebar"
        >
          <i className={`fa-solid ${isCollapsed ? 'fa-indent' : 'fa-outdent'}`}></i>
        </button>
        <div className="page-breadcrumb">
          <div className="page-title">{title}</div>
          <div className="page-subtitle">{sub}</div>
        </div>
      </div>

      <div className="header-right">
        <div className="status-pill" title="Hệ thống Axios Interceptor đang hoạt động">
          <div className="pulse-dot"></div>
          <span>Axios Interceptor Active</span>
        </div>

        <button 
          className="header-action-btn"
          onClick={() => toastEmitter.show('Thông báo hệ thống', 'Hệ thống an toàn thông tin đang hoạt động ổn định.', 'info')}
          title="Thông báo"
        >
          <i className="fa-regular fa-bell"></i>
        </button>
      </div>
    </header>
  );
}
