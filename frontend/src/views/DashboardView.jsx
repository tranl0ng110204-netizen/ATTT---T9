import React from 'react';

export default function DashboardView({ setActiveTab }) {
  const metrics = [
    { title: 'Lưu Lượng Yêu Cầu (24h)', value: '142,850', icon: 'fa-solid fa-server', color: '#38bdf8', bg: 'rgba(56, 189, 248, 0.15)' },
    { title: 'Sự Cố Đã Chặn', value: '1,420', icon: 'fa-solid fa-shield-halved', color: '#10b981', bg: 'rgba(16, 185, 129, 0.15)' },
    { title: 'Phiên Đăng Nhập JWT', value: '38', icon: 'fa-solid fa-key', color: '#6366f1', bg: 'rgba(99, 102, 241, 0.15)' },
    { title: 'Thời Gian Phản Hồi TB', value: '42 ms', icon: 'fa-solid fa-gauge-high', color: '#f59e0b', bg: 'rgba(245, 158, 11, 0.15)' },
  ];

  const recentIncidents = [
    { id: 'SEC-892', type: 'SQL Injection Attempt', ip: '192.168.1.105', risk: 'HIGH', time: '5 phút trước', status: 'Đã chặn' },
    { id: 'SEC-891', type: 'Invalid JWT Signature (401)', ip: '10.0.4.12', risk: 'MEDIUM', time: '18 phút trước', status: 'Từ chối' },
    { id: 'SEC-890', type: 'Brute Force Login', ip: '172.16.0.88', risk: 'HIGH', time: '42 phút trước', status: 'Khóa IP' },
    { id: 'SEC-889', type: 'Access Denied (403)', ip: '192.168.1.44', risk: 'LOW', time: '1 giờ trước', status: 'Ghi log' },
  ];

  return (
    <div>
      {/* 4 Thẻ chỉ số chính */}
      <div className="grid-cols-4">
        {metrics.map((m, idx) => (
          <div className="stat-card" key={idx}>
            <div className="stat-icon-wrapper" style={{ background: m.bg, color: m.color }}>
              <i className={m.icon}></i>
            </div>
            <div className="stat-info">
              <div className="stat-label">{m.title}</div>
              <div className="stat-value">{m.value}</div>
            </div>
          </div>
        ))}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '1.5rem' }}>
        {/* Bảng sự cố gần đây */}
        <div className="card-panel" style={{ marginBottom: 0 }}>
          <div className="card-title-row">
            <div className="card-title">
              <i className="fa-solid fa-triangle-exclamation" style={{ color: '#f59e0b' }}></i>
              Cảnh Báo An Ninh Gần Đây
            </div>
            <button 
              className="btn btn-outline" 
              style={{ padding: '0.4rem 0.8rem', fontSize: '0.8rem' }} 
              onClick={() => setActiveTab('logs')}
            >
              Xem Tất Cả Log
            </button>
          </div>
          
          <table className="data-table">
            <thead>
              <tr>
                <th>Mã Sự Cố</th>
                <th>Loại Cảnh Báo</th>
                <th>Địa Chỉ IP</th>
                <th>Mức Độ</th>
                <th>Thời Gian</th>
                <th>Hành Động</th>
              </tr>
            </thead>
            <tbody>
              {recentIncidents.map((row) => (
                <tr key={row.id}>
                  <td style={{ fontWeight: 600, color: '#38bdf8' }}>{row.id}</td>
                  <td>{row.type}</td>
                  <td><code>{row.ip}</code></td>
                  <td>
                    <span className={`badge-status ${row.risk === 'HIGH' ? 'danger' : row.risk === 'MEDIUM' ? 'warning' : 'info'}`}>
                      {row.risk}
                    </span>
                  </td>
                  <td style={{ color: '#94a3b8' }}>{row.time}</td>
                  <td><span className="badge-status success">{row.status}</span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Khung trạng thái dịch vụ */}
        <div className="card-panel" style={{ marginBottom: 0 }}>
          <div className="card-title-row">
            <div className="card-title">
              <i className="fa-solid fa-server" style={{ color: '#10b981' }}></i>
              Trạng Thái Dịch Vụ
            </div>
          </div>
          
          <div className="service-list">
            <div className="service-item">
              <div className="service-info">
                <div className="service-name">Spring Boot API Core</div>
                <div className="service-sub">Port 8080 • RESTful API</div>
              </div>
              <span className="badge-status success">Hoạt động</span>
            </div>

            <div className="service-item">
              <div className="service-info">
                <div className="service-name">Axios Interceptor Filter</div>
                <div className="service-sub">Bắt lỗi tập trung & Token Handler</div>
              </div>
              <span className="badge-status success">Kích hoạt</span>
            </div>

            <div className="service-item">
              <div className="service-info">
                <div className="service-name">JWT Authentication Service</div>
                <div className="service-sub">HMAC-SHA256 Token Provider</div>
              </div>
              <span className="badge-status success">Sẵn sàng</span>
            </div>

            <div className="service-item">
              <div className="service-info">
                <div className="service-name">Cơ Sở Dữ Liệu Quản Trị</div>
                <div className="service-sub">Persistent Storage</div>
              </div>
              <span className="badge-status success">Kết nối tốt</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
