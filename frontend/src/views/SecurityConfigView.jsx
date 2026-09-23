import React, { useState } from 'react';
import { toastEmitter } from '../services/api';

export default function SecurityConfigView() {
  const [jwtExp, setJwtExp] = useState('86400');
  const [corsOrigins, setCorsOrigins] = useState('http://localhost:3000, http://127.0.0.1:5500');
  const [maxLoginAttempts, setMaxLoginAttempts] = useState('5');
  const [rateLimit, setRateLimit] = useState('100');

  const handleSave = (e) => {
    e.preventDefault();
    toastEmitter.show('Lưu chính sách', 'Đã cập nhật cấu hình bảo mật thành công!', 'success');
  };

  return (
    <div className="card-panel">
      <div className="card-title-row">
        <div className="card-title">
          <i className="fa-solid fa-sliders" style={{ color: '#38bdf8' }}></i>
          Cấu Hình Chính Sách An Toàn Thông Tin
        </div>
      </div>
      <p className="card-desc">Thiết lập các tham số bảo vệ hệ thống, giới hạn truy cập và chính sách xác thực JWT.</p>

      <form onSubmit={handleSave} style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1.5rem', marginTop: '1rem' }}>
        <div className="form-group">
          <label className="form-label">Thời Hạn Sống JWT Token (Giây)</label>
          <input 
            type="number" 
            className="form-control" 
            value={jwtExp} 
            onChange={(e) => setJwtExp(e.target.value)} 
          />
          <span className="form-hint">Mặc định: 86400s (24 giờ). Sau thời gian này Interceptor sẽ bắt lỗi 401.</span>
        </div>

        <div className="form-group">
          <label className="form-label">Giới Hạn Tần Suất (Rate Limiting - req/phút)</label>
          <input 
            type="number" 
            className="form-control" 
            value={rateLimit} 
            onChange={(e) => setRateLimit(e.target.value)} 
          />
          <span className="form-hint">Số lượng yêu cầu tối đa cho phép trên mỗi IP trong 1 phút.</span>
        </div>

        <div className="form-group" style={{ gridColumn: 'span 2' }}>
          <label className="form-label">Danh Sách Nguồn CORS Được Phép (Allowed Origins)</label>
          <input 
            type="text" 
            className="form-control" 
            value={corsOrigins} 
            onChange={(e) => setCorsOrigins(e.target.value)} 
          />
          <span className="form-hint">Các domain frontend được phép gọi API tới Backend Spring Boot.</span>
        </div>

        <div className="form-group">
          <label className="form-label">Số Lần Đăng Nhập Sai Tối Đa Trước Khi Khóa</label>
          <input 
            type="number" 
            className="form-control" 
            value={maxLoginAttempts} 
            onChange={(e) => setMaxLoginAttempts(e.target.value)} 
          />
          <span className="form-hint">Tự động tạm khóa tài khoản sau các lần thử sai liên tiếp.</span>
        </div>

        <div className="form-group" style={{ display: 'flex', alignItems: 'flex-end' }}>
          <button type="submit" className="btn btn-primary" style={{ width: '100%', height: '42px' }}>
            <i className="fa-solid fa-floppy-disk"></i> Lưu & Áp Dụng Chính Sách
          </button>
        </div>
      </form>
    </div>
  );
}
