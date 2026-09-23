import React, { useState } from 'react';
import { toastEmitter } from '../services/api';

export default function UsersView() {
  const [users, setUsers] = useState([
    { id: 1, name: 'Nguyễn Văn An', email: 'an.nguyen@attt.edu.vn', role: 'ROLE_ADMIN', status: 'ACTIVE', lastLogin: '10 phút trước' },
    { id: 2, name: 'Trần Thị Bình', email: 'binh.tran@attt.edu.vn', role: 'ROLE_SECURITY', status: 'ACTIVE', lastLogin: '1 giờ trước' },
    { id: 3, name: 'Lê Hoàng Cường', email: 'cuong.le@attt.edu.vn', role: 'ROLE_AUDITOR', status: 'ACTIVE', lastLogin: '3 giờ trước' },
    { id: 4, name: 'Phạm Minh Đức', email: 'duc.pham@attt.edu.vn', role: 'ROLE_USER', status: 'LOCKED', lastLogin: '2 ngày trước' },
    { id: 5, name: 'Vũ Thị Hoa', email: 'hoa.vu@attt.edu.vn', role: 'ROLE_USER', status: 'PENDING', lastLogin: 'Chưa đăng nhập' },
  ]);

  const [searchTerm, setSearchTerm] = useState('');

  const filteredUsers = users.filter(u => 
    u.name.toLowerCase().includes(searchTerm.toLowerCase()) || 
    u.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
    u.role.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="card-panel">
      <div className="card-title-row">
        <div className="card-title">
          <i className="fa-solid fa-users" style={{ color: '#38bdf8' }}></i>
          Danh Sách Tài Khoản & Phân Quyền
        </div>
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <div className="search-box">
            <i className="fa-solid fa-magnifying-glass"></i>
            <input 
              type="text" 
              placeholder="Tìm kiếm tài khoản, email, vai trò..." 
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          <button 
            className="btn btn-primary" 
            onClick={() => toastEmitter.show('Thêm tài khoản', 'Mở form thêm người dùng mới', 'info')}
          >
            <i className="fa-solid fa-user-plus"></i> Thêm Tài Khoản
          </button>
        </div>
      </div>

      <table className="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Họ Và Tên</th>
            <th>Email Liên Hệ</th>
            <th>Vai Trò (RBAC)</th>
            <th>Trạng Thái</th>
            <th>Lần Đăng Nhập Cuối</th>
            <th style={{ textAlign: 'right' }}>Thao Tác</th>
          </tr>
        </thead>
        <tbody>
          {filteredUsers.map((u) => (
            <tr key={u.id}>
              <td style={{ color: '#64748b' }}>#{u.id}</td>
              <td style={{ fontWeight: 600 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
                  <div className="avatar-circle">{u.name.charAt(0)}</div>
                  {u.name}
                </div>
              </td>
              <td style={{ color: '#94a3b8' }}>{u.email}</td>
              <td>
                <span className={`badge-role ${u.role === 'ROLE_ADMIN' ? 'admin' : u.role === 'ROLE_SECURITY' ? 'security' : 'user'}`}>
                  {u.role}
                </span>
              </td>
              <td>
                <span className={`badge-status ${u.status === 'ACTIVE' ? 'success' : u.status === 'LOCKED' ? 'danger' : 'warning'}`}>
                  ● {u.status}
                </span>
              </td>
              <td style={{ color: '#94a3b8', fontSize: '0.85rem' }}>{u.lastLogin}</td>
              <td style={{ textAlign: 'right' }}>
                <button 
                  className="btn-icon" 
                  title="Chỉnh sửa" 
                  onClick={() => toastEmitter.show('Chỉnh sửa', `Sửa thông tin tài khoản ${u.name}`, 'info')}
                >
                  <i className="fa-solid fa-pen-to-square"></i>
                </button>
                <button 
                  className="btn-icon danger" 
                  title="Khóa/Mở khóa" 
                  onClick={() => toastEmitter.show('Phân quyền', `Cập nhật trạng thái cho ${u.name}`, 'warning')}
                >
                  <i className="fa-solid fa-lock"></i>
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
