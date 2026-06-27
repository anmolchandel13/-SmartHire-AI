import React, { useState, useEffect } from 'react';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { LogOut, Filter, Download, UserCheck, Check, Search, Award } from 'lucide-react';

export default function AdminDashboard() {
  const { logout } = useAuth();
  const [candidates, setCandidates] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Filtering States
  const [branch, setBranch] = useState('');
  const [minPercentage, setMinPercentage] = useState('');
  const [minScore, setMinScore] = useState('');
  const [skill, setSkill] = useState('');
  const [sortBy, setSortBy] = useState('createdAt');
  const [direction, setDirection] = useState('desc');

  const [statusMsg, setStatusMsg] = useState('');

  const fetchCandidates = async () => {
    try {
      setLoading(true);
      const data = await api.getCandidates({
        branch,
        minPercentage,
        minScore,
        skill,
        sortBy,
        direction
      });
      setCandidates(data);
    } catch (error) {
      console.error('Failed to load candidate summaries', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCandidates();
  }, [branch, minPercentage, minScore, skill, sortBy, direction]);

  const handleShortlist = async (profileId) => {
    setStatusMsg('');
    try {
      await api.shortlistCandidate(profileId);
      setStatusMsg('Candidate shortlisted and email notification triggered!');
      fetchCandidates();
    } catch (error) {
      setStatusMsg(`Shortlist failed: ${error.message}`);
    }
  };

  const handleExportPDF = async () => {
    try {
      const blob = await api.exportShortlistPdf();
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'smarthire_shortlist_report.pdf');
      document.body.appendChild(link);
      link.click();
      link.parentNode.removeChild(link);
    } catch (error) {
      console.error('Failed to export PDF shortlist report', error);
    }
  };

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '40px 20px' }}>
      
      {/* Navbar Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '40px' }}>
        <div>
          <h1 style={{ fontSize: '32px', fontWeight: '800' }}><span className="gradient-text">Recruiter Dashboard</span></h1>
          <p style={{ color: 'var(--text-secondary)' }}>Review registered candidate scorecards, filter profiles, and shortlist applications</p>
        </div>
        <div style={{ display: 'flex', gap: '12px' }}>
          <button onClick={handleExportPDF} className="btn btn-success" style={{ padding: '10px 20px' }}>
            <Download size={16} /> Export Shortlist PDF
          </button>
          <button onClick={logout} className="btn btn-secondary" style={{ padding: '10px 20px' }}>
            <LogOut size={16} /> Sign Out
          </button>
        </div>
      </div>

      {statusMsg && (
        <div style={{ background: 'var(--secondary-glow)', border: '1px solid var(--secondary)', borderRadius: '8px', padding: '12px', marginBottom: '24px', textAlign: 'center' }}>
          {statusMsg}
        </div>
      )}

      {/* Dynamic Filters Bar */}
      <div className="glass-card" style={{ padding: '24px', marginBottom: '32px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
        <h3 style={{ fontSize: '16px', fontWeight: '700', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Filter size={16} color="#818cf8" /> Filter Candidates
        </h3>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr 1fr', gap: '16px' }}>
          <div>
            <label style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>Branch</label>
            <input type="text" placeholder="e.g. Computer Science" value={branch} onChange={(e) => setBranch(e.target.value)} />
          </div>
          <div>
            <label style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>Min CGPA / Percentage</label>
            <input type="number" placeholder="e.g. 75" value={minPercentage} onChange={(e) => setMinPercentage(e.target.value)} />
          </div>
          <div>
            <label style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>Min AI Score</label>
            <input type="number" placeholder="e.g. 80" value={minScore} onChange={(e) => setMinScore(e.target.value)} />
          </div>
          <div>
            <label style={{ fontSize: '12px', color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>Skills Keyword</label>
            <input type="text" placeholder="e.g. Java, Docker" value={skill} onChange={(e) => setSkill(e.target.value)} />
          </div>
        </div>

        {/* Sorting Controls */}
        <div style={{ display: 'flex', gap: '16px', borderTop: '1px solid var(--border-color)', paddingTop: '16px', fontSize: '13px', alignItems: 'center' }}>
          <span style={{ color: 'var(--text-secondary)' }}>Sort By:</span>
          <select value={sortBy} onChange={(e) => setSortBy(e.target.value)} style={{ width: 'auto', padding: '6px 12px' }}>
            <option value="createdAt">Date Registered</option>
            <option value="score">AI Evaluation Score</option>
            <option value="percentage">Academic Percentage</option>
          </select>
          <select value={direction} onChange={(e) => setDirection(e.target.value)} style={{ width: 'auto', padding: '6px 12px' }}>
            <option value="desc">Descending</option>
            <option value="asc">Ascending</option>
          </select>
        </div>
      </div>

      {/* Candidate List Data Table */}
      <div className="glass-card" style={{ overflowX: 'auto' }}>
        {loading ? (
          <div style={{ padding: '40px', textAlign: 'center', color: 'var(--text-secondary)' }}>Loading Candidates...</div>
        ) : candidates.length > 0 ? (
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '14px' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)', textTransform: 'uppercase', fontSize: '11px', letterSpacing: '0.5px' }}>
                <th style={{ padding: '16px 24px' }}>Candidate</th>
                <th style={{ padding: '16px 24px' }}>Branch</th>
                <th style={{ padding: '16px 24px' }}>Percentage</th>
                <th style={{ padding: '16px 24px' }}>AI Score</th>
                <th style={{ padding: '16px 24px' }}>Recommended Role</th>
                <th style={{ padding: '16px 24px' }}>Readiness</th>
                <th style={{ padding: '16px 24px', textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {candidates.map((c) => (
                <tr key={c.profileId} style={{ borderBottom: '1px solid var(--border-color)', transition: 'background 0.2s' }} onMouseEnter={(e) => e.currentTarget.style.background = 'rgba(255, 255, 255, 0.01)'} onMouseLeave={(e) => e.currentTarget.style.background = 'transparent'}>
                  <td style={{ padding: '16px 24px' }}>
                    <div style={{ fontWeight: '600' }}>{c.fullName}</div>
                    <div style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>{c.email}</div>
                  </td>
                  <td style={{ padding: '16px 24px', color: 'var(--text-secondary)' }}>{c.branch || 'N/A'}</td>
                  <td style={{ padding: '16px 24px' }}>{c.percentage ? `${c.percentage}%` : 'N/A'}</td>
                  <td style={{ padding: '16px 24px' }}>
                    {c.aiScore !== null ? (
                      <span style={{ background: c.aiScore >= 80 ? 'var(--secondary-glow)' : 'rgba(255,255,255,0.05)', color: c.aiScore >= 80 ? 'var(--secondary)' : 'var(--text-primary)', padding: '4px 8px', borderRadius: '6px', fontWeight: 'bold' }}>
                        {c.aiScore}/100
                      </span>
                    ) : (
                      <span style={{ color: 'var(--text-secondary)', fontSize: '12px' }}>Not Analyzed</span>
                    )}
                  </td>
                  <td style={{ padding: '16px 24px', color: '#818cf8', fontWeight: '600' }}>{c.recommendedRole || 'N/A'}</td>
                  <td style={{ padding: '16px 24px' }}>
                    {c.readinessLevel ? (
                      <span style={{ color: c.readinessLevel === 'Ready' ? 'var(--secondary)' : 'var(--warning)', fontWeight: '600', fontSize: '13px' }}>
                        {c.readinessLevel}
                      </span>
                    ) : 'N/A'}
                  </td>
                  <td style={{ padding: '16px 24px', textAlign: 'right' }}>
                    {c.isShortlisted ? (
                      <span style={{ color: 'var(--secondary)', display: 'inline-flex', alignItems: 'center', gap: '4px', fontSize: '13px', fontWeight: '600' }}>
                        <Check size={14} /> Shortlisted
                      </span>
                    ) : (
                      <button onClick={() => handleShortlist(c.profileId)} className="btn btn-secondary" style={{ padding: '6px 12px', fontSize: '12px' }}>
                        <UserCheck size={13} /> Shortlist
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <div style={{ padding: '60px', textAlign: 'center', color: 'var(--text-secondary)', display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '12px' }}>
            <Award size={40} />
            <div>No candidates matched the criteria</div>
          </div>
        )}
      </div>

    </div>
  );
}
