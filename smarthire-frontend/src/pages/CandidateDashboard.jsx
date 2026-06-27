import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { LogOut, FileText, Send, User, Award, ShieldAlert, CheckCircle, RefreshCw } from 'lucide-react';

export default function CandidateDashboard() {
  const { logout } = useAuth();
  const [profile, setProfile] = useState(null);
  const [scorecard, setScorecard] = useState(null);
  const [fullName, setFullName] = useState('');
  const [phone, setPhone] = useState('');
  const [branch, setBranch] = useState('');
  const [percentage, setPercentage] = useState('');
  const [skills, setSkills] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  
  const [profileLoading, setProfileLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [analyzing, setAnalyzing] = useState(false);
  const [statusMessage, setStatusMessage] = useState({ text: '', type: '' });

  const loadData = async () => {
    try {
      setProfileLoading(true);
      const profileData = await api.getProfile();
      setProfile(profileData);
      setFullName(profileData.fullName || '');
      setPhone(profileData.phone || '');
      setBranch(profileData.branch || '');
      setPercentage(profileData.percentage || '');
      setSkills(profileData.skills || '');

      if (profileData.analysisCompleted) {
        const scorecardData = await api.getScorecard();
        setScorecard(scorecardData);
      } else {
        setScorecard(null);
      }
    } catch (error) {
      console.error('Failed to load profile details', error);
    } finally {
      setProfileLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleUpdateProfile = async (e) => {
    e.preventDefault();
    setStatusMessage({ text: '', type: '' });
    try {
      const updated = await api.updateProfile({
        fullName,
        phone,
        branch,
        percentage: parseFloat(percentage),
        skills
      });
      setProfile(updated);
      setStatusMessage({ text: 'Profile updated successfully!', type: 'success' });
    } catch (error) {
      setStatusMessage({ text: error.message, type: 'error' });
    }
  };

  const handleFileChange = (e) => {
    if (e.target.files && e.target.files[0]) {
      setSelectedFile(e.target.files[0]);
    }
  };

  const handleUploadResume = async (e) => {
    e.preventDefault();
    if (!selectedFile) return;
    setStatusMessage({ text: '', type: '' });
    setUploading(true);

    try {
      const updated = await api.uploadResume(selectedFile);
      setProfile(updated);
      setSelectedFile(null);
      setStatusMessage({ text: 'Resume uploaded and text extracted!', type: 'success' });
      // Reload profile
      loadData();
    } catch (error) {
      setStatusMessage({ text: error.message, type: 'error' });
    } finally {
      setUploading(false);
    }
  };

  const handleAnalyze = async () => {
    setStatusMessage({ text: '', type: '' });
    setAnalyzing(true);
    try {
      const result = await api.analyzeResume();
      setScorecard(result);
      setStatusMessage({ text: 'AI analysis scorecard generated!', type: 'success' });
      loadData();
    } catch (error) {
      setStatusMessage({ text: error.message, type: 'error' });
    } finally {
      setAnalyzing(false);
    }
  };

  if (profileLoading) {
    return (
      <div style={{ display: 'flex', minHeight: '100vh', alignItems: 'center', justifyContent: 'center' }}>
        <p style={{ color: 'var(--text-secondary)' }}>Loading Workspace...</p>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '40px 20px' }}>
      
      {/* Navbar Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '40px' }}>
        <div>
          <h1 style={{ fontSize: '32px', fontWeight: '800' }}><span className="gradient-text">Candidate Dashboard</span></h1>
          <p style={{ color: 'var(--text-secondary)' }}>Configure credentials, upload resume, and get AI metrics</p>
        </div>
        <button onClick={logout} className="btn btn-secondary" style={{ padding: '10px 20px' }}>
          <LogOut size={16} /> Sign Out
        </button>
      </div>

      {statusMessage.text && (
        <div style={{ background: statusMessage.type === 'success' ? 'var(--secondary-glow)' : 'rgba(239, 68, 68, 0.12)', border: `1px solid ${statusMessage.type === 'success' ? 'var(--secondary)' : 'var(--danger)'}`, borderRadius: '8px', padding: '16px', marginBottom: '24px', textAlign: 'center' }}>
          {statusMessage.text}
        </div>
      )}

      {/* Main Grid content split */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '32px', alignItems: 'start' }}>
        
        {/* Left Side: Setup Forms */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '32px' }}>
          
          {/* Profile Details Card */}
          <div className="glass-card" style={{ padding: '32px' }}>
            <h3 style={{ fontSize: '20px', fontWeight: '700', marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <User size={20} color="#818cf8" /> Personal & Academic Info
            </h3>
            <form onSubmit={handleUpdateProfile} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div>
                <label style={{ fontSize: '13px', color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>Full Name</label>
                <input type="text" value={fullName} onChange={(e) => setFullName(e.target.value)} required />
              </div>
              
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div>
                  <label style={{ fontSize: '13px', color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>Phone</label>
                  <input type="text" value={phone} onChange={(e) => setPhone(e.target.value)} />
                </div>
                <div>
                  <label style={{ fontSize: '13px', color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>Academic Branch</label>
                  <input type="text" value={branch} onChange={(e) => setBranch(e.target.value)} placeholder="e.g. Computer Science" />
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                <div>
                  <label style={{ fontSize: '13px', color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>Academic Percentage (%)</label>
                  <input type="number" step="0.01" value={percentage} onChange={(e) => setPercentage(e.target.value)} required />
                </div>
                <div>
                  <label style={{ fontSize: '13px', color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>Skills (comma-separated)</label>
                  <input type="text" value={skills} onChange={(e) => setSkills(e.target.value)} placeholder="e.g. Java, SQL, React" />
                </div>
              </div>

              <button type="submit" className="btn btn-primary" style={{ marginTop: '8px' }}>Save Profile</button>
            </form>
          </div>

          {/* Resume Upload Card */}
          <div className="glass-card" style={{ padding: '32px' }}>
            <h3 style={{ fontSize: '20px', fontWeight: '700', marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <FileText size={20} color="#10b981" /> Resume Upload (PDF)
            </h3>
            
            {profile?.resumeUploaded ? (
              <div style={{ background: 'rgba(16, 185, 129, 0.1)', border: '1px dashed var(--secondary)', borderRadius: '8px', padding: '16px', marginBottom: '20px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                  <CheckCircle color="var(--secondary)" size={24} />
                  <div>
                    <p style={{ fontWeight: '600', fontSize: '14px' }}>Resume is Uploaded</p>
                    <p style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>{profile.resumeFilename || 'resume.pdf'}</p>
                  </div>
                </div>
              </div>
            ) : (
              <div style={{ background: 'rgba(239, 68, 68, 0.08)', border: '1px dashed var(--danger)', borderRadius: '8px', padding: '16px', marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '12px' }}>
                <ShieldAlert color="var(--danger)" size={24} />
                <div>
                  <p style={{ fontWeight: '600', fontSize: '14px' }}>No Resume Uploaded</p>
                  <p style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>Upload your PDF resume to trigger AI analysis scorecard</p>
                </div>
              </div>
            )}

            <form onSubmit={handleUploadResume} style={{ display: 'flex', gap: '12px' }}>
              <input type="file" accept=".pdf" onChange={handleFileChange} required style={{ flex: '1', padding: '8px 12px' }} />
              <button type="submit" className="btn btn-secondary" disabled={uploading}>
                {uploading ? 'Uploading...' : 'Upload'}
              </button>
            </form>

            {profile?.resumeUploaded && (
              <button 
                onClick={handleAnalyze} 
                className="btn btn-success" 
                style={{ width: '100%', marginTop: '20px', height: '48px' }} 
                disabled={analyzing}
              >
                {analyzing ? (
                  <>
                    <RefreshCw className="animate-spin" size={18} /> Generating scorecard...
                  </>
                ) : (
                  <>
                    <Send size={16} /> Analyze My Resume
                  </>
                )}
              </button>
            )}
          </div>

        </div>

        {/* Right Side: Scorecard / Results */}
        <div>
          {scorecard ? (
            <div className="glass-card animated-entry" style={{ padding: '32px', border: '1px solid rgba(16, 185, 129, 0.2)' }}>
              
              {/* Scorecard Header */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '28px', borderBottom: '1px solid var(--border-color)', paddingBottom: '20px' }}>
                <div>
                  <h3 style={{ fontSize: '22px', fontWeight: '800' }}>AI Evaluation Scorecard</h3>
                  <p style={{ color: 'var(--text-secondary)', fontSize: '13px' }}>Candidate: {scorecard.candidateName}</p>
                </div>
                
                {/* Visual Circle Gauge */}
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', width: '80px', height: '80px', borderRadius: '50%', background: 'linear-gradient(135deg, #10b981 0%, #059669 100%)', boxShadow: '0 0 20px var(--secondary-glow)' }}>
                  <span style={{ fontSize: '28px', fontWeight: '900', color: '#fff' }}>{scorecard.score}</span>
                  <span style={{ fontSize: '10px', color: 'rgba(255,255,255,0.8)', textTransform: 'uppercase', letterSpacing: '0.5px' }}>Score</span>
                </div>
              </div>

              {/* Scorecard contents */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
                  <div style={{ background: 'rgba(255,255,255,0.02)', padding: '16px', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                    <p style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>Recommended Role</p>
                    <p style={{ fontWeight: '700', fontSize: '16px', marginTop: '4px', color: '#818cf8' }}>{scorecard.recommendedRole}</p>
                  </div>
                  <div style={{ background: 'rgba(255,255,255,0.02)', padding: '16px', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                    <p style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>Readiness Level</p>
                    <p style={{ fontWeight: '700', fontSize: '16px', marginTop: '4px', color: scorecard.readinessLevel === 'Ready' ? 'var(--secondary)' : 'var(--warning)' }}>
                      {scorecard.readinessLevel}
                    </p>
                  </div>
                </div>

                <div>
                  <h4 style={{ fontSize: '14px', fontWeight: '700', marginBottom: '8px' }}>Executive Summary</h4>
                  <p style={{ fontSize: '14px', color: 'var(--text-secondary)', lineHeight: '1.6' }}>{scorecard.summary}</p>
                </div>

                <div>
                  <h4 style={{ fontSize: '14px', fontWeight: '700', color: 'var(--secondary)', marginBottom: '8px' }}>Key Strengths</h4>
                  <div style={{ fontSize: '13px', color: 'var(--text-secondary)', lineHeight: '1.6', background: 'rgba(16, 185, 129, 0.05)', padding: '12px', borderRadius: '8px', border: '1px solid rgba(16, 185, 129, 0.1)' }}>
                    {scorecard.strengths}
                  </div>
                </div>

                <div>
                  <h4 style={{ fontSize: '14px', fontWeight: '700', color: 'var(--warning)', marginBottom: '8px' }}>Areas for Improvement</h4>
                  <div style={{ fontSize: '13px', color: 'var(--text-secondary)', lineHeight: '1.6', background: 'rgba(245, 158, 11, 0.05)', padding: '12px', borderRadius: '8px', border: '1px solid rgba(245, 158, 11, 0.1)' }}>
                    {scorecard.weaknesses}
                  </div>
                </div>

              </div>

            </div>
          ) : (
            <div className="glass-card" style={{ padding: '40px', textAlign: 'center', minHeight: '300px', display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: '16px' }}>
              <Award size={48} color="var(--text-secondary)" />
              <div>
                <h3 style={{ fontSize: '18px', fontWeight: '700' }}>AI Scorecard Pending</h3>
                <p style={{ color: 'var(--text-secondary)', fontSize: '14px', marginTop: '4px', maxWidth: '300px', margin: '8px auto 0 auto' }}>
                  Fill in your profile details and upload a PDF resume, then trigger evaluation to generate metrics.
                </p>
              </div>
            </div>
          )}
        </div>

      </div>

    </div>
  );
}
