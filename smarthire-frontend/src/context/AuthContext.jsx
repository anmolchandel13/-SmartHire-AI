import React, { createContext, useState, useEffect, useContext } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (token) {
      // Decode JWT roughly to extract role & email (since it's a stateless JWT)
      try {
        const payloadBase64 = token.split('.')[1];
        const decodedClaims = JSON.parse(atob(payloadBase64));
        
        // Spring Security roles come as a string "ROLE_CANDIDATE" or "ROLE_ADMIN"
        let role = 'CANDIDATE';
        if (decodedClaims.roles && decodedClaims.roles.includes('ROLE_ADMIN')) {
          role = 'ADMIN';
        }

        setUser({
          email: decodedClaims.sub,
          role: role
        });
      } catch (error) {
        console.error('Failed to parse JWT token', error);
        logout();
      }
    } else {
      setUser(null);
    }
    setLoading(false);
  }, [token]);

  const login = (jwtToken, email, role) => {
    localStorage.setItem('token', jwtToken);
    setToken(jwtToken);
    setUser({ email, role });
  };

  const logout = () => {
    localStorage.removeItem('token');
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, token, loading, login, logout }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
