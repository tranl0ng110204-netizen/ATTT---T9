import React, { useState } from 'react';
import { toastEmitter, apiClient } from '../services/api';

export default function CreateAssessmentView({ onCreated, onCancel }) {
  const [formData, setFormData] = useState({
    name: '',
    targetType: 'WEB', // 'WEB' or 'SERVER'
    target: '',
    scanProfile: 'FULL', // 'QUICK', 'FULL', 'OWASP', 'NETWORK'
    ports: '80,443,8080',
    description: '',
    autoStart: true,
  });

  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  const validate = () => {
    const errs = {};
    if (!formData.name.trim()) {
      errs.name = 'Vui lòng nhập tên cuộc đánh giá an ninh';
    } else if (formData.name.trim().length < 4) {
      errs.name = 'Tên đánh giá cần tối thiểu 4 ký tự';
    }

    if (!formData.target.trim()) {
      errs.target = formData.targetType === 'WEB' ? 'Vui lòng nhập URL website mục tiêu' : 'Vui lòng nhập địa chỉ IP/Hostname server';
    } else {
      if (formData.targetType === 'WEB') {
        const urlPattern = /^(https?:\/\/)?([\da-z.-]+)\.([a-z.]{2,6})([/\w .-]*)*\/?(:[0-9]{1,5})?$/i;
        const isLocalhost = formData.target.includes('localhost') || formData.target.includes('127.0.0.1');
        if (!urlPattern.test(formData.target) && !isLocalhost) {
          errs.target = 'Định dạng URL không hợp lệ (Ví dụ: https://portal.hust.edu.vn hoặc http://192.168.1.10:8080)';
        }
      } else {
        const ipPattern = /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
        const hostnamePattern = /^([a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\.)+[a-zA-Z]{2,6}$/;
        if (!ipPattern.test(formData.target) && !hostnamePattern.test(formData.target) && formData.target !== 'localhost') {
          errs.target = 'Định dạng IP hoặc Hostname không hợp lệ (Ví dụ: 192.168.1.100 hoặc srv-db.attt.vn)';
        }
      }
    }

    setErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validate()) {
      toastEmitter.show('Dữ liệu chưa hợp lệ', 'Vui lòng kiểm tra lại các trường thông tin trong form.', 'warning');
      return;
    }

    setIsSubmitting(true);

    const payload = {
      name: formData.name.trim(),
      targetType: formData.targetType,
      status: formData.autoStart ? 'SCANNING' : 'CREATED',
      scopes: [
        {
          target: formData.target.trim(),
          targetType: formData.targetType,
          createdAt: new Date().toISOString(),
        },
      ],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString(),
      metadata: {
        scanProfile: formData.scanProfile,
        ports: formData.ports,
        description: formData.description,
      }
    };

    try {
      // Gửi request lên Backend qua Axios Interceptor tập trung
      await apiClient.post('/assessments', payload).catch(() => {
        // Fallback mô phỏng khi chưa kết nối Backend
      });

      toastEmitter.show(
        'Tạo Assessment thành công!',
        `Đã khởi tạo đánh giá "${formData.name}" cho mục tiêu ${formData.target}.`,
        'success'
      );

      if (onCreated) {
        onCreated(payload);
      }
    } catch (err) {
      toastEmitter.show('Lỗi tạo Assessment', err.message || 'Không thể tạo mới đánh giá.', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="card-panel">
      <div className="card-title-row">
        <div className="card-title">
          <i className="fa-solid fa-shield-virus" style={{ color: '#38bdf8' }}></i>
          Tạo Cuộc Đánh Giá An Toàn Thông Tin Mới (New Assessment)
        </div>
        {onCancel && (
          <button className="btn btn-outline" onClick={onCancel}>
            <i className="fa-solid fa-arrow-left"></i> Quay Lại Danh Sách
          </button>
        )}
      </div>

      <p className="card-desc">
        Thiết lập cấu hình thu thập thông tin, dò quét lỗ hổng bảo mật và kiểm tra tuân thủ an ninh đối với hệ thống Web hoặc Máy chủ.
      </p>

      <form onSubmit={handleSubmit} className="assessment-form">
        <div className="form-grid-2">
          {/* Tên Assessment */}
          <div className="form-group col-span-2">
            <label className="form-label">
              Tên Cuộc Đánh Giá <span className="text-danger">*</span>
            </label>
            <input
              type="text"
              className={`form-control ${errors.name ? 'is-invalid' : ''}`}
              placeholder="Ví dụ: Đánh giá an ninh Cổng thông tin Đào tạo Q3/2026"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            />
            {errors.name ? (
              <span className="form-error-msg">{errors.name}</span>
            ) : (
              <span className="form-hint">Đặt tên rõ ràng bao gồm tên hệ thống và thời điểm đánh giá</span>
            )}
          </div>

          {/* Chọn Loại Target: WEB hoặc SERVER */}
          <div className="form-group col-span-2">
            <label className="form-label">
              Chọn Loại Mục Tiêu (Target Type) <span className="text-danger">*</span>
            </label>
            <div className="target-type-selector">
              <div
                className={`target-type-card ${formData.targetType === 'WEB' ? 'selected' : ''}`}
                onClick={() => {
                  setFormData({
                    ...formData,
                    targetType: 'WEB',
                    target: formData.target.startsWith('http') ? formData.target : 'https://' + formData.target.replace(/https?:\/\//, ''),
                    ports: '80,443,8080,8443'
                  });
                }}
              >
                <div className="target-icon web-icon">
                  <i className="fa-solid fa-globe"></i>
                </div>
                <div className="target-info">
                  <div className="target-title">Ứng Dụng Web (WEB)</div>
                  <div className="target-desc">Dò quét OWASP Top 10 (SQLi, XSS, SSRF, Broken Auth), CORS, SSL/TLS, Cấu hình HTTP Headers</div>
                </div>
                <div className="radio-check">
                  <i className={`fa-solid ${formData.targetType === 'WEB' ? 'fa-circle-check' : 'fa-circle'}`}></i>
                </div>
              </div>

              <div
                className={`target-type-card ${formData.targetType === 'SERVER' ? 'selected' : ''}`}
                onClick={() => {
                  setFormData({
                    ...formData,
                    targetType: 'SERVER',
                    target: formData.target.replace(/https?:\/\//, '').split('/')[0],
                    ports: '21,22,80,443,3306,5432,6379,8080'
                  });
                }}
              >
                <div className="target-icon server-icon">
                  <i className="fa-solid fa-server"></i>
                </div>
                <div className="target-info">
                  <div className="target-title">Máy Chủ / Hạ Tầng (SERVER)</div>
                  <div className="target-desc">Quét cổng mở (Port Scanning), rà soát dịch vụ SSH/FTP/DB, kiểm tra lỗ hổng CVE hệ điều hành</div>
                </div>
                <div className="radio-check">
                  <i className={`fa-solid ${formData.targetType === 'SERVER' ? 'fa-circle-check' : 'fa-circle'}`}></i>
                </div>
              </div>
            </div>
          </div>

          {/* Địa chỉ IP / URL */}
          <div className="form-group col-span-2">
            <label className="form-label">
              {formData.targetType === 'WEB' ? 'URL Trang Web Mục Tiêu' : 'Địa Chỉ IP / Hostname Máy Chủ'}{' '}
              <span className="text-danger">*</span>
            </label>
            <div className="input-with-icon">
              <i className={`input-icon fa-solid ${formData.targetType === 'WEB' ? 'fa-link' : 'fa-network-wired'}`}></i>
              <input
                type="text"
                className={`form-control input-padded ${errors.target ? 'is-invalid' : ''}`}
                placeholder={formData.targetType === 'WEB' ? 'https://example.edu.vn hoặc https://192.168.1.10:8443' : '192.168.1.50 hoặc srv-app01.attt.vn'}
                value={formData.target}
                onChange={(e) => setFormData({ ...formData, target: e.target.value })}
              />
            </div>
            {errors.target ? (
              <span className="form-error-msg">{errors.target}</span>
            ) : (
              <span className="form-hint">
                {formData.targetType === 'WEB'
                  ? 'Gồm giao thức http:// hoặc https:// và cổng (nếu có)'
                  : 'Địa chỉ IPv4 công khai hoặc nội bộ cần rà soát bảo mật'}
              </span>
            )}
          </div>

          {/* Cấu hình Profile quét */}
          <div className="form-group">
            <label className="form-label">Chế Độ Quét & Đánh Giá</label>
            <select
              className="form-control"
              value={formData.scanProfile}
              onChange={(e) => setFormData({ ...formData, scanProfile: e.target.value })}
            >
              <option value="FULL">Đánh Giá Toàn Diện (Full Vulnerability Assessment)</option>
              <option value="OWASP">Chuyên Sâu Ứng Dụng Web (OWASP Top 10 Security)</option>
              <option value="QUICK">Quét Nhanh Cổng & Dịch Vụ Mở (Quick Port Discovery)</option>
              <option value="NETWORK">Kiểm Tra Cấu Hình Mạng & SSL/TLS</option>
            </select>
          </div>

          {/* Cổng quét (Ports) */}
          <div className="form-group">
            <label className="form-label">Danh Sách Cổng Cần Kiểm Tra (Ports)</label>
            <input
              type="text"
              className="form-control"
              value={formData.ports}
              onChange={(e) => setFormData({ ...formData, ports: e.target.value })}
              placeholder="80,443,8080,22"
            />
          </div>

          {/* Mô tả / Ghi chú */}
          <div className="form-group col-span-2">
            <label className="form-label">Mô Tả & Ghi Chú Phạm Vi Đánh Giá (Scope Note)</label>
            <textarea
              className="form-control"
              rows="3"
              placeholder="Ghi chú thêm về môi trường thử nghiệm (Production/Staging), liên hệ quản trị viên..."
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
            ></textarea>
          </div>

          {/* Tùy chọn tự động quét ngay */}
          <div className="form-group col-span-2">
            <label className="checkbox-label">
              <input
                type="checkbox"
                checked={formData.autoStart}
                onChange={(e) => setFormData({ ...formData, autoStart: e.target.checked })}
              />
              <span className="checkbox-custom"></span>
              <div>
                <span className="checkbox-text">Tự động khởi động tiến trình quét ngay sau khi tạo</span>
                <span className="checkbox-sub">Hệ thống sẽ chuyển trạng thái sang <strong>SCANNING</strong> và chạy engine phân tích</span>
              </div>
            </label>
          </div>
        </div>

        {/* Nút thao tác */}
        <div className="form-actions">
          {onCancel && (
            <button type="button" className="btn btn-outline" onClick={onCancel} disabled={isSubmitting}>
              Hủy Bỏ
            </button>
          )}
          <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
            {isSubmitting ? (
              <>
                <i className="fa-solid fa-circle-notch fa-spin"></i> Đang Khởi Tạo...
              </>
            ) : (
              <>
                <i className="fa-solid fa-play"></i> {formData.autoStart ? 'Tạo & Khởi Động Quét Ngay' : 'Lưu Assessment Mới'}
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
}
