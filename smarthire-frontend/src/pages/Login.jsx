import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { api } from '../services/api';
import { Briefcase, Mail, Lock, User } from 'lucide-react';

export default function Login() {
  const { login } = useAuth();
  const [isRegister, setIsRegister] = useState(false);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (isRegister) {
        await api.register(fullName, email, password);
        setIsRegister(false);
        setError('Registration successful! Please login.');
      } else {
        const response = await api.login(email, password);
        login(response.token, response.email, response.role);
      }
    } catch (err) {
      setError(err.message || 'Authentication failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh', alignItems: 'center', justifyContent: 'center', padding: '20px' }}>
      <div className="glass-card animated-entry" style={{ width: '100%', maxWidth: '440px', padding: '40px' }}>
        
        {/* Logo/Header */}
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <div style={{ background: 'var(--primary-glow)', width: '64px', height: '64px', borderRadius: '16px', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px auto', border: '1px solid rgba(79, 70, 229, 0.3)' }}>
            <Briefcase size={32} color="#818cf8" />
          </div>
          <h2 style={{ fontSize: '28px', fontWeight: '800' }}>
            <span className="gradient-text">SmartHire AI</span>
          </h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '14px', marginTop: '4px' }}>
            {isRegister ? 'Create your recruitment profile' : 'Sign in to access your dashboard'}
          </p>
        </div>

        {error && (
          <div style={{ background: error.includes('successful') ? 'var(--secondary-glow)' : 'rgba(239, 68, 68, 0.15)', border: `1px solid ${error.includes('successful') ? 'var(--secondary)' : 'var(--danger)'}`, borderRadius: '8px', padding: '12px', fontSize: '13px', marginBottom: '20px', textAlign: 'center' }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {isRegister && (
            <div style={{ position: 'relative' }}>
              <User size={18} color="var(--text-secondary)" style={{ position: 'absolute', left: '16px', top: '15px' }} />
              <input 
                type="text" 
                placeholder="Full Name" 
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                required
                style={{ paddingLeft: '48px' }}
              />
            </div>
          )}

          <div style={{ position: 'relative' }}>
            <Mail size={18} color="var(--text-secondary)" style={{ position: 'absolute', left: '16px', top: '15px' }} />
            <input 
              type="email" 
              placeholder="Email Address" 
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              style={{ paddingLeft: '48px' }}
            />
          </div>

          <div style={{ position: 'relative' }}>
            <Lock size={18} color="var(--text-secondary)" style={{ position: 'absolute', left: '16px', top: '15px' }} />
            <input 
              type="password" 
              placeholder="Password" 
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              style={{ paddingLeft: '48px' }}
            />
          </div>

          <button type="submit" className="btn btn-primary" style={{ width: '100%', height: '48px' }} disabled={loading}>
            {loading ? 'Processing...' : (isRegister ? 'Register' : 'Sign In')}
          </button>
        </form>

        <div style={{ textAlign: 'center', marginTop: '24px', fontSize: '14px', color: 'var(--text-secondary)' }}>
          {isRegister ? 'Already have an account? ' : 'First time applying? '}
          <button 
            type="button"
            onClick={() => { setIsRegister(!isRegister); setError(''); }}
            style={{ background: 'none', border: 'none', color: '#818cf8', fontWeight: '600', cursor: 'pointer' }}
          >
            {isRegister ? 'Sign In' : 'Create Account'}
          </button>
        </div>

      </div>
    </div>
  );
}
