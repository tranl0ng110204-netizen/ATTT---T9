import React, { useState } from 'react';
import Sidebar from './components/Sidebar';
import Header from './components/Header';
import ToastNotification from './components/ToastNotification';
import DashboardView from './views/DashboardView';
import AssessmentsView from './views/AssessmentsView';
import CreateAssessmentView from './views/CreateAssessmentView';
import UsersView from './views/UsersView';
import AuditLogsView from './views/AuditLogsView';
import SecurityConfigView from './views/SecurityConfigView';

export default function App() {
  const [isCollapsed, setIsCollapsed] = useState(false);
  const [activeTab, setActiveTab] = useState('assessments');

  return (
    <div className="app-container">
      {/* Toast thông báo lỗi / thành công tập trung từ Axios Interceptor */}
      <ToastNotification />

      {/* 1. Sidebar bên trái */}
      <Sidebar 
        isCollapsed={isCollapsed} 
        activeTab={activeTab} 
        setActiveTab={setActiveTab} 
      />

      {/* Khung chính chứa Header và Content Area */}
      <div className="main-wrapper">
        {/* 2. Header trên cùng */}
        <Header 
          isCollapsed={isCollapsed} 
          setIsCollapsed={setIsCollapsed} 
          activeTab={activeTab} 
        />

        {/* 3. Content Area hiển thị động */}
        <main className="content-area">
          {activeTab === 'dashboard' && <DashboardView setActiveTab={setActiveTab} />}
          {activeTab === 'assessments' && <AssessmentsView setActiveTab={setActiveTab} />}
          {activeTab === 'create-assessment' && (
            <CreateAssessmentView
              onCreated={() => setActiveTab('assessments')}
              onCancel={() => setActiveTab('assessments')}
            />
          )}
          {activeTab === 'users' && <UsersView />}
          {activeTab === 'logs' && <AuditLogsView />}
          {activeTab === 'security' && <SecurityConfigView />}
        </main>
      </div>
    </div>
  );
}
