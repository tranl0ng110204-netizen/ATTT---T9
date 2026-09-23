import React from 'react';
import { toastEmitter } from '../services/api';

export default function AuditLogsView() {
  const logs = [
    { id: 'LOG-4091', method: 'GET', endpoint: '/api/v1/users', status: 200, user: 'admin@attt.edu.vn', ip: '192.168.1.10', time: '10:45:12' },
    { id: 'LOG-4090', method: 'POST', endpoint: '/api/v1/auth/login', status: 401, user: 'unknown', ip: '10.0.4.12', time: '10:44:50' },
    { id: 'LOG-4089', method: 'DELETE', endpoint: '/api/v1/roles/2', status: 403, user: 'user.c@attt.edu.vn', ip: '192.168.1.44', time: '10:42:15' },
    { id: 'LOG-4088', method: 'GET', endpoint: '/api/v1/reports/summary', status: 200, user: 'binh.tran@attt.edu.vn', ip: '192.168.1.15', time: '10:39:08' },
    { id: 'LOG-4087', method: 'PUT', endpoint: '/api/v1/system/config', status: 500, user: 'admin@attt.edu.vn', ip: '192.168.1.10', time: '10:35:40' },
    { id: 'LOG-4086', method: 'GET', endpoint: '/api/v1/files/secret.pdf', status: 404, user: 'cuong.le@attt.edu.vn', ip: '192.168.1.22', time: '10:30:19' },
  ];

  return (
    <div className="card-panel">
      <div className="card-title-row">
        <div className="card-title">
          <i className="fa-solid fa-list-check" style={{ color: '#6366f1' }}></i>
          Nhật Ký Yêu Cầu & Giám Sát HTTP
        </div>
        <button 
          className="btn btn-outline" 
          onClick={() => toastEmitter.show('Làm mới', 'Đã cập nhật nhật ký kiểm toán mới nhất', 'success')}
        >
          <i className="fa-solid fa-arrows-rotate"></i> Làm Mới Log
        </button>
      </div>
      <p className="card-desc">
        Toàn bộ yêu cầu đi qua hệ thống đều được giám sát và xử lý mã lỗi tự động qua <strong>Axios Interceptor</strong>.
      </p>

      <table className="data-table">
        <thead>
          <tr>
            <th>Mã Log</th>
            <th>Phương Thức</th>
            <th>Endpoint API</th>
            <th>Mã HTTP</th>
            <th>Người Thực Hiện</th>
            <th>IP Nguồn</th>
            <th>Thời Gian</th>
          </tr>
        </thead>
        <tbody>
          {logs.map((log) => (
            <tr key={log.id}>
              <td style={{ color: '#64748b' }}>{log.id}</td>
              <td>
                <span className={`badge-method ${log.method.toLowerCase()}`}>
                  {log.method}
                </span>
              </td>
              <td><code>{log.endpoint}</code></td>
              <td>
                <span className={`badge-status ${log.status === 200 ? 'success' : log.status === 401 || log.status === 403 ? 'warning' : 'danger'}`}>
                  {log.status} {log.status === 200 ? 'OK' : log.status === 401 ? 'Unauthorized' : log.status === 403 ? 'Forbidden' : log.status === 404 ? 'Not Found' : 'Error'}
                </span>
              </td>
              <td style={{ color: '#f8fafc' }}>{log.user}</td>
              <td><code>{log.ip}</code></td>
              <td style={{ color: '#94a3b8' }}>{log.time}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
