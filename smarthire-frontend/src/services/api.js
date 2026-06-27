const API_BASE_URL = 'http://localhost:8080/api';

/**
 * Standard fetch wrapper that automatically injects the JWT token
 * from localStorage into the Authorization headers.
 */
async function apiRequest(endpoint, options = {}) {
  const token = localStorage.getItem('token');
  
  const headers = {
    ...options.headers,
  };

  // If payload is not MultipartFormData, default content-type is JSON
  if (!(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const config = {
    ...options,
    headers,
  };

  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
    
    // Check for HTTP errors
    if (!response.ok) {
      // PDF export returns raw blob, handle JSON parsing error boundary
      if (endpoint === '/admin/shortlist/export') {
        throw new Error('Failed to export PDF shortlist');
      }
      const errData = await response.json().catch(() => ({ message: 'Something went wrong' }));
      throw new Error(errData.message || `Request failed: ${response.status}`);
    }

    // PDF export returns a binary stream (blob)
    if (endpoint === '/admin/shortlist/export') {
      return await response.blob();
    }

    return await response.json();
  } catch (error) {
    console.error(`API Error on ${endpoint}:`, error.message);
    throw error;
  }
}

export const api = {
  // Auth API
  register: (fullName, email, password) => 
    apiRequest('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ fullName, email, password })
    }),
    
  login: (email, password) =>
    apiRequest('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password })
    }),

  // Candidate API
  getProfile: () => apiRequest('/candidate/profile'),

  updateProfile: (profileData) =>
    apiRequest('/candidate/profile', {
      method: 'PUT',
      body: JSON.stringify(profileData)
    }),

  uploadResume: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return apiRequest('/candidate/resume/upload', {
      method: 'POST',
      body: formData
    });
  },

  analyzeResume: () =>
    apiRequest('/candidate/analyze', {
      method: 'POST'
    }),

  getScorecard: () => apiRequest('/candidate/scorecard'),

  // Admin API
  getCandidates: (filters = {}) => {
    const queryParams = new URLSearchParams();
    Object.entries(filters).forEach(([key, val]) => {
      if (val !== undefined && val !== null && val !== '') {
        queryParams.append(key, val);
      }
    });
    const queryString = queryParams.toString();
    return apiRequest(`/admin/candidates${queryString ? `?${queryString}` : ''}`);
  },

  shortlistCandidate: (profileId) =>
    apiRequest(`/admin/candidates/${profileId}/shortlist`, {
      method: 'PUT'
    }),

  exportShortlistPdf: () => apiRequest('/admin/shortlist/export')
};
