import React from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import Login from './pages/Login';
import CandidateDashboard from './pages/CandidateDashboard';
import AdminDashboard from './pages/AdminDashboard';

function DashboardRouter() {
  const { user } = useAuth();

  if (!user) {
    return <Login />;
  }

  // Route to dashboard based on User Role (parsed from JWT token)
  if (user.role === 'ADMIN') {
    return <AdminDashboard />;
  }

  return <CandidateDashboard />;
}

export default function App() {
  return (
    <AuthProvider>
      <DashboardRouter />
    </AuthProvider>
  );
}
