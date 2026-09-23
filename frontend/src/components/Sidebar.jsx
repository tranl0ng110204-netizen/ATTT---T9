import React, { useState, useEffect } from 'react';
import { authStorage } from '../services/api';

export default function Sidebar({ isCollapsed, activeTab, setActiveTab }) {
  const [user, setUser] = useState(authStorage.getUser());

  useEffect(() => {
    const handleAuthChange = () => setUser(authStorage.getUser());
    window.addEventListener('auth-change', handleAuthChange);
    return () => window.removeEventListener('auth-change', handleAuthChange);
  }, []);

  const menuItems = [
    { id: 'dashboard', title: 'Tổng Quan Hệ Thống', icon: 'fa-solid fa-chart-line', category: 'GIÁM SÁT' },
    { id: 'assessments', title: 'Danh Sách Assessment', icon: 'fa-solid fa-shield-virus', badge: 'Core', category: 'ĐÁNH GIÁ AN NINH' },
    { id: 'create-assessment', title: 'Tạo Assessment Mới', icon: 'fa-solid fa-plus-circle', category: 'ĐÁNH GIÁ AN NINH' },
    { id: 'users', title: 'Quản Lý Người Dùng', icon: 'fa-solid fa-users-gear', category: 'QUẢN TRỊ' },
    { id: 'logs', title: 'Nhật Ký Bảo Mật', icon: 'fa-solid fa-shield-halved', badge: 'Live', category: 'GIÁM SÁT' },
    { id: 'security', title: 'Chính Sách & Cấu Hình', icon: 'fa-solid fa-sliders', category: 'HỆ THỐNG' },
  ];

  let currentCategory = null;

  return (
    <aside className={`sidebar ${isCollapsed ? 'collapsed' : ''}`}>
      <div className="sidebar-header">
        <a 
          href="#" 
          className="brand-logo" 
          onClick={(e) => { e.preventDefault(); setActiveTab('dashboard'); }}
        >
          <div className="brand-icon">
            <i className="fa-solid fa-shield-virus"></i>
          </div>
          <div className="brand-text">
            ATTT PORTAL
            <span>Trung tâm giám sát an ninh</span>
          </div>
        </a>
      </div>

      <ul className="sidebar-menu">
        {menuItems.map((item) => {
          const showCategory = item.category !== currentCategory;
          if (showCategory) currentCategory = item.category;

          return (
            <React.Fragment key={item.id}>
              {showCategory && <div className="menu-category">{item.category}</div>}
              <li
                className={`menu-item ${activeTab === item.id ? 'active' : ''}`}
                onClick={() => setActiveTab(item.id)}
                title={isCollapsed ? item.title : ''}
              >
                <i className={`${item.icon} menu-icon`}></i>
                <span className="menu-title">{item.title}</span>
                {item.badge && <span className="badge-tag">{item.badge}</span>}
              </li>
            </React.Fragment>
          );
        })}
      </ul>

      <div className="sidebar-footer">
        <div className="user-avatar-sm">
          <i className="fa-solid fa-user-shield"></i>
        </div>
        <div className="user-info-text">
          <div className="user-name">{user?.name || 'Chuyên viên ATTT'}</div>
          <div className="user-role">{user?.role || 'ROLE_ADMIN'}</div>
        </div>
      </div>
    </aside>
  );
}
