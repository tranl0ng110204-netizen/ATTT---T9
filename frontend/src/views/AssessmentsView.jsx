import React, { useState, useEffect } from 'react';
import { toastEmitter, apiClient } from '../services/api';
import CreateAssessmentView from './CreateAssessmentView';

export default function AssessmentsView({ setActiveTab }) {
  const [viewMode, setViewMode] = useState('list'); // 'list' | 'create'
  const [searchTerm, setSearchTerm] = useState('');
  const [filterType, setFilterType] = useState('ALL'); // 'ALL' | 'WEB' | 'SERVER'
  const [filterStatus, setFilterStatus] = useState('ALL'); // 'ALL' | 'CREATED' | 'SCANNING' | 'COMPLETED' | 'FAILED'
  const [selectedAssessment, setSelectedAssessment] = useState(null);

  // Dữ liệu mẫu khởi tạo chuẩn theo Entity Assessment & Scope của Backend
  const [assessments, setAssessments] = useState([
    {
      id: 1,
      name: 'Rà soát an ninh Cổng thông tin Sinh viên',
      targetType: 'WEB',
      status: 'COMPLETED',
      createdAt: '2026-09-21 08:30:00',
      updatedAt: '2026-09-21 09:15:20',
      scopes: [
        { id: 101, target: 'https://sinhvien.attt.edu.vn', targetType: 'WEB', createdAt: '2026-09-21 08:30:00' }
      ],
      findings: { critical: 1, high: 2, medium: 4, low: 7 },
      scanProfile: 'OWASP Top 10 + SSL/TLS Audit'
    },
    {
      id: 2,
      name: 'Kiểm toán hệ thống Máy chủ Cơ sở dữ liệu Oracle',
      targetType: 'SERVER',
      status: 'SCANNING',
      createdAt: '2026-09-23 08:15:00',
      updatedAt: '2026-09-23 08:20:00',
      scopes: [
        { id: 102, target: '192.168.10.55', targetType: 'SERVER', createdAt: '2026-09-23 08:15:00' }
      ],
      findings: { critical: 0, high: 1, medium: 3, low: 2 },
      scanProfile: 'Full Port Scan & CVE OS Audit'
    },
    {
      id: 3,
      name: 'Đánh giá ứng dụng Quản lý Điểm thi Trực tuyến',
      targetType: 'WEB',
      status: 'CREATED',
      createdAt: '2026-09-23 09:00:00',
      updatedAt: '2026-09-23 09:00:00',
      scopes: [
        { id: 103, target: 'https://diemthi.attt.edu.vn:8443', targetType: 'WEB', createdAt: '2026-09-23 09:00:00' }
      ],
      findings: { critical: 0, high: 0, medium: 0, low: 0 },
      scanProfile: 'Full Vulnerability Assessment'
    },
    {
      id: 4,
      name: 'Kiểm tra hạ tầng Tường lửa & Gateway DMZ',
      targetType: 'SERVER',
      status: 'COMPLETED',
      createdAt: '2026-09-20 14:00:00',
      updatedAt: '2026-09-20 14:45:00',
      scopes: [
        { id: 104, target: '10.0.1.1', targetType: 'SERVER', createdAt: '2026-09-20 14:00:00' }
      ],
      findings: { critical: 0, high: 0, medium: 2, low: 5 },
      scanProfile: 'Network Gateway & Port Audit'
    },
    {
      id: 5,
      name: 'Dò quét ứng dụng Thư viện Điện tử',
      targetType: 'WEB',
      status: 'FAILED',
      createdAt: '2026-09-19 11:20:00',
      updatedAt: '2026-09-19 11:25:00',
      scopes: [
        { id: 105, target: 'https://lib.attt.edu.vn', targetType: 'WEB', createdAt: '2026-09-19 11:20:00' }
      ],
      findings: { critical: 0, high: 0, medium: 0, low: 0 },
      scanProfile: 'OWASP Security Scan (Mất kết nối tới máy chủ đích)'
    }
  ]);

  // Tải dữ liệu từ Backend nếu có
  useEffect(() => {
    apiClient.get('/assessments')
      .then((data) => {
        if (Array.isArray(data) && data.length > 0) {
          setAssessments(data);
        }
      })
      .catch(() => {
        // Giữ mock data nếu backend chưa có controller
      });
  }, []);

  // Xử lý khi tạo xong Assessment mới
  const handleCreated = (newAssessment) => {
    const newItem = {
      ...newAssessment,
      id: Date.now(),
      findings: { critical: 0, high: 0, medium: 0, low: 0 }
    };

    setAssessments([newItem, ...assessments]);
    setViewMode('list');

    // Nếu autoStart SCANNING, mô phỏng hoàn thành sau 4.5s
    if (newItem.status === 'SCANNING') {
      setTimeout(() => {
        setAssessments((prev) =>
          prev.map((item) =>
            item.id === newItem.id
              ? {
                  ...item,
                  status: 'COMPLETED',
                  updatedAt: new Date().toLocaleString(),
                  findings: { critical: 1, high: 2, medium: 3, low: 5 }
                }
              : item
          )
        );
        toastEmitter.show(
          'Hoàn tất quét lỗ hổng!',
          `Đánh giá "${newItem.name}" đã hoàn thành tiến trình phân tích.`,
          'success'
        );
      }, 5000);
    }
  };

  // Kích hoạt quét / quét lại
  const handleTriggerScan = (id) => {
    setAssessments((prev) =>
      prev.map((item) =>
        item.id === id ? { ...item, status: 'SCANNING', updatedAt: new Date().toLocaleString() } : item
      )
    );

    toastEmitter.show('Đang thực hiện quét...', `Tiến trình kiểm tra an ninh cho Assessment #${id} đã bắt đầu.`, 'info');

    setTimeout(() => {
      setAssessments((prev) =>
        prev.map((item) =>
          item.id === id
            ? {
                ...item,
                status: 'COMPLETED',
                updatedAt: new Date().toLocaleString(),
                findings: { critical: 0, high: 1, medium: 2, low: 4 }
              }
            : item
        )
      );
      toastEmitter.show('Quét hoàn tất!', `Assessment #${id} đã cập nhật báo cáo lỗ hổng an ninh mới nhất.`, 'success');
    }, 4500);
  };

  // Xóa Assessment
  const handleDelete = (id) => {
    if (window.confirm(`Bạn có chắc chắn muốn xóa Assessment #${id} không?`)) {
      setAssessments(assessments.filter((a) => a.id !== id));
      toastEmitter.show('Đã xóa', `Đã loại bỏ Assessment #${id} khỏi hệ thống.`, 'info');
      if (selectedAssessment?.id === id) {
        setSelectedAssessment(null);
      }
    }
  };

  // Thống kê nhanh
  const stats = {
    total: assessments.length,
    web: assessments.filter((a) => a.targetType === 'WEB').length,
    server: assessments.filter((a) => a.targetType === 'SERVER').length,
    scanning: assessments.filter((a) => a.status === 'SCANNING').length,
    completed: assessments.filter((a) => a.status === 'COMPLETED').length,
  };

  // Lọc dữ liệu
  const filteredAssessments = assessments.filter((item) => {
    const matchSearch =
      item.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      item.scopes?.some((s) => s.target.toLowerCase().includes(searchTerm.toLowerCase()));
    const matchType = filterType === 'ALL' || item.targetType === filterType;
    const matchStatus = filterStatus === 'ALL' || item.status === filterStatus;
    return matchSearch && matchType && matchStatus;
  });

  const getStatusBadge = (status) => {
    switch (status) {
      case 'SCANNING':
        return (
          <span className="badge-status info status-scanning">
            <i className="fa-solid fa-circle-notch fa-spin"></i> Đang Quét (SCANNING)
          </span>
        );
      case 'COMPLETED':
        return (
          <span className="badge-status success">
            <i className="fa-solid fa-circle-check"></i> Hoàn Thành (COMPLETED)
          </span>
        );
      case 'CREATED':
        return (
          <span className="badge-status" style={{ background: '#334155', color: '#cbd5e1' }}>
            <i className="fa-solid fa-clock"></i> Mới Tạo (CREATED)
          </span>
        );
      case 'FAILED':
        return (
          <span className="badge-status danger">
            <i className="fa-solid fa-triangle-exclamation"></i> Lỗi (FAILED)
          </span>
        );
      default:
        return <span className="badge-status">{status}</span>;
    }
  };

  if (viewMode === 'create') {
    return (
      <CreateAssessmentView
        onCreated={handleCreated}
        onCancel={() => setViewMode('list')}
      />
    );
  }

  return (
    <div className="assessments-container">
      {/* 1. THẺ THỐNG KÊ TỔNG QUAN */}
      <div className="grid-cols-4">
        <div className="stat-card">
          <div className="stat-icon-wrapper" style={{ background: 'rgba(56, 189, 248, 0.15)', color: '#38bdf8' }}>
            <i className="fa-solid fa-shield-virus"></i>
          </div>
          <div className="stat-info">
            <span className="stat-label">Tổng Số Assessment</span>
            <span className="stat-value">{stats.total}</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrapper" style={{ background: 'rgba(99, 102, 241, 0.15)', color: '#818cf8' }}>
            <i className="fa-solid fa-circle-notch fa-spin"></i>
          </div>
          <div className="stat-info">
            <span className="stat-label">Đang Quét An Ninh</span>
            <span className="stat-value">{stats.scanning}</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrapper" style={{ background: 'rgba(16, 185, 129, 0.15)', color: '#34d399' }}>
            <i className="fa-solid fa-circle-check"></i>
          </div>
          <div className="stat-info">
            <span className="stat-label">Đã Hoàn Thành</span>
            <span className="stat-value">{stats.completed}</span>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon-wrapper" style={{ background: 'rgba(245, 158, 11, 0.15)', color: '#fbbf24' }}>
            <i className="fa-solid fa-layer-group"></i>
          </div>
          <div className="stat-info">
            <span className="stat-label">Web / Server</span>
            <span className="stat-value">{stats.web} <span style={{ fontSize: '0.9rem', color: '#94a3b8' }}>Web</span> / {stats.server} <span style={{ fontSize: '0.9rem', color: '#94a3b8' }}>Server</span></span>
          </div>
        </div>
      </div>

      {/* 2. BẢNG DANH SÁCH ASSESSMENT */}
      <div className="card-panel">
        <div className="card-title-row">
          <div className="card-title">
            <i className="fa-solid fa-table-list" style={{ color: '#38bdf8' }}></i>
            Danh Sách Cuộc Đánh Giá An Ninh (Assessments Table)
          </div>
          <button className="btn btn-primary" onClick={() => setViewMode('create')}>
            <i className="fa-solid fa-plus"></i> Tạo Assessment Mới
          </button>
        </div>

        {/* Thanh công cụ tìm kiếm và lọc */}
        <div className="table-toolbar">
          <div className="search-box">
            <i className="fa-solid fa-magnifying-glass"></i>
            <input
              type="text"
              placeholder="Tìm theo tên, IP, URL mục tiêu..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          <div className="filter-group">
            <span className="filter-label">Loại:</span>
            <select
              className="form-control filter-select"
              value={filterType}
              onChange={(e) => setFilterType(e.target.value)}
            >
              <option value="ALL">Tất Cả Loại</option>
              <option value="WEB">Ứng Dụng Web (WEB)</option>
              <option value="SERVER">Máy Chủ / Hạ Tầng (SERVER)</option>
            </select>

            <span className="filter-label">Trạng thái:</span>
            <select
              className="form-control filter-select"
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
            >
              <option value="ALL">Tất Cả Trạng Thái</option>
              <option value="SCANNING">Đang Quét (SCANNING)</option>
              <option value="COMPLETED">Hoàn Thành (COMPLETED)</option>
              <option value="CREATED">Mới Tạo (CREATED)</option>
              <option value="FAILED">Lỗi (FAILED)</option>
            </select>
          </div>
        </div>

        {/* Bảng dữ liệu Assessment */}
        <div className="table-responsive">
          <table className="data-table">
            <thead>
              <tr>
                <th style={{ width: '60px' }}>ID</th>
                <th>Tên Đánh Giá</th>
                <th>Loại Mục Tiêu</th>
                <th>Mục Tiêu (Scope IP/URL)</th>
                <th>Trạng Thái</th>
                <th>Lỗ Hổng Phát Hiện</th>
                <th>Thời Gian Cập Nhật</th>
                <th style={{ textAlign: 'right' }}>Thao Tác</th>
              </tr>
            </thead>
            <tbody>
              {filteredAssessments.length === 0 ? (
                <tr>
                  <td colSpan="8" style={{ textAlign: 'center', padding: '3rem', color: '#64748b' }}>
                    <i className="fa-solid fa-folder-open" style={{ fontSize: '2rem', marginBottom: '0.5rem', display: 'block' }}></i>
                    Không tìm thấy cuộc đánh giá nào phù hợp với bộ lọc.
                  </td>
                </tr>
              ) : (
                filteredAssessments.map((item) => (
                  <tr key={item.id}>
                    <td style={{ color: '#64748b', fontWeight: 600 }}>#{item.id}</td>
                    <td>
                      <div className="assessment-name-cell" onClick={() => setSelectedAssessment(item)}>
                        <strong>{item.name}</strong>
                      </div>
                    </td>
                    <td>
                      {item.targetType === 'WEB' ? (
                        <span className="badge-target web">
                          <i className="fa-solid fa-globe"></i> WEB
                        </span>
                      ) : (
                        <span className="badge-target server">
                          <i className="fa-solid fa-server"></i> SERVER
                        </span>
                      )}
                    </td>
                    <td>
                      <div className="scope-target-cell">
                        <code>{item.scopes?.[0]?.target || 'N/A'}</code>
                        {item.scopes?.length > 1 && (
                          <span className="badge-more-scopes">+{item.scopes.length - 1}</span>
                        )}
                      </div>
                    </td>
                    <td>{getStatusBadge(item.status)}</td>
                    <td>
                      {item.findings && (item.findings.critical > 0 || item.findings.high > 0 || item.findings.medium > 0) ? (
                        <div className="findings-pills">
                          {item.findings.critical > 0 && <span className="pill-vuln critical">{item.findings.critical} Crit</span>}
                          {item.findings.high > 0 && <span className="pill-vuln high">{item.findings.high} High</span>}
                          {item.findings.medium > 0 && <span className="pill-vuln med">{item.findings.medium} Med</span>}
                        </div>
                      ) : item.status === 'COMPLETED' ? (
                        <span style={{ color: '#10b981', fontSize: '0.8rem', fontWeight: 600 }}>
                          <i className="fa-solid fa-shield-check"></i> An toàn
                        </span>
                      ) : (
                        <span style={{ color: '#64748b', fontSize: '0.8rem' }}>Chưa có kết quả</span>
                      )}
                    </td>
                    <td style={{ fontSize: '0.82rem', color: '#94a3b8' }}>
                      {item.updatedAt || item.createdAt}
                    </td>
                    <td style={{ textAlign: 'right', whiteSpace: 'nowrap' }}>
                      <button
                        className="btn-icon"
                        title="Xem chi tiết Assessment"
                        onClick={() => setSelectedAssessment(item)}
                      >
                        <i className="fa-solid fa-eye"></i>
                      </button>

                      {item.status !== 'SCANNING' && (
                        <button
                          className="btn-icon"
                          style={{ color: '#38bdf8' }}
                          title="Bắt đầu / Quét lại"
                          onClick={() => handleTriggerScan(item.id)}
                        >
                          <i className="fa-solid fa-play"></i>
                        </button>
                      )}

                      <button
                        className="btn-icon danger"
                        title="Xóa cuộc đánh giá"
                        onClick={() => handleDelete(item.id)}
                      >
                        <i className="fa-solid fa-trash"></i>
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* 3. MODAL XEM CHI TIẾT ASSESSMENT */}
      {selectedAssessment && (
        <div className="modal-overlay" onClick={() => setSelectedAssessment(null)}>
          <div className="modal-card" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <div className="modal-title">
                <i className="fa-solid fa-shield-halved" style={{ color: '#38bdf8' }}></i>
                Chi Tiết Cuộc Đánh Giá #{selectedAssessment.id}
              </div>
              <button className="modal-close-btn" onClick={() => setSelectedAssessment(null)}>
                <i className="fa-solid fa-xmark"></i>
              </button>
            </div>

            <div className="modal-body">
              <div className="detail-section">
                <div className="detail-row">
                  <span className="detail-label">Tên Đánh Giá:</span>
                  <span className="detail-value font-bold">{selectedAssessment.name}</span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">Loại Mục Tiêu:</span>
                  <span className="detail-value">
                    {selectedAssessment.targetType === 'WEB' ? 'Ứng Dụng Web (WEB)' : 'Máy Chủ / Hạ Tầng (SERVER)'}
                  </span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">Trạng Thái:</span>
                  <span className="detail-value">{getStatusBadge(selectedAssessment.status)}</span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">Chế Độ Quét:</span>
                  <span className="detail-value">{selectedAssessment.scanProfile || 'Đánh Giá Toàn Diện (Full)'}</span>
                </div>
                <div className="detail-row">
                  <span className="detail-label">Thời Gian Tạo:</span>
                  <span className="detail-value">{selectedAssessment.createdAt}</span>
                </div>
              </div>

              <div className="detail-scopes-section">
                <h4 style={{ fontSize: '0.95rem', marginBottom: '0.75rem', color: '#f8fafc' }}>
                  <i className="fa-solid fa-crosshairs" style={{ color: '#6366f1', marginRight: '0.5rem' }}></i>
                  Phạm Vi Đánh Giá (Target Scopes)
                </h4>
                <div className="scope-list">
                  {selectedAssessment.scopes?.map((sc, idx) => (
                    <div key={idx} className="scope-item">
                      <div className="scope-item-left">
                        <i className={`fa-solid ${sc.targetType === 'WEB' ? 'fa-globe' : 'fa-server'}`} style={{ color: '#38bdf8' }}></i>
                        <code>{sc.target}</code>
                      </div>
                      <span className="badge-role security">{sc.targetType}</span>
                    </div>
                  ))}
                </div>
              </div>

              {selectedAssessment.status === 'COMPLETED' && (
                <div className="detail-findings-section">
                  <h4 style={{ fontSize: '0.95rem', marginBottom: '0.75rem', color: '#f8fafc' }}>
                    <i className="fa-solid fa-bug" style={{ color: '#f43f5e', marginRight: '0.5rem' }}></i>
                    Tóm Tắt Lỗ Hổng Bảo Mật (Vulnerability Findings)
                  </h4>
                  <div className="findings-summary-grid">
                    <div className="finding-box critical">
                      <span className="finding-num">{selectedAssessment.findings?.critical || 0}</span>
                      <span className="finding-lbl">Nghiêm Trọng (Critical)</span>
                    </div>
                    <div className="finding-box high">
                      <span className="finding-num">{selectedAssessment.findings?.high || 0}</span>
                      <span className="finding-lbl">Mức Cao (High)</span>
                    </div>
                    <div className="finding-box medium">
                      <span className="finding-num">{selectedAssessment.findings?.medium || 0}</span>
                      <span className="finding-lbl">Mức Trung Bình (Medium)</span>
                    </div>
                    <div className="finding-box low">
                      <span className="finding-num">{selectedAssessment.findings?.low || 0}</span>
                      <span className="finding-lbl">Mức Thấp (Low)</span>
                    </div>
                  </div>
                </div>
              )}
            </div>

            <div className="modal-footer">
              {selectedAssessment.status !== 'SCANNING' && (
                <button
                  className="btn btn-primary"
                  onClick={() => {
                    handleTriggerScan(selectedAssessment.id);
                    setSelectedAssessment(null);
                  }}
                >
                  <i className="fa-solid fa-play"></i> Chạy Quét Lại
                </button>
              )}
              <button className="btn btn-outline" onClick={() => setSelectedAssessment(null)}>
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
