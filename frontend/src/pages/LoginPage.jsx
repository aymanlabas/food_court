import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Card,
  CardContent,
  Typography,
  TextField,
  Button,
  Alert,
  CircularProgress,
  InputAdornment,
  IconButton,
  Chip,
  Paper,
} from '@mui/material';
import RestaurantMenuIcon from '@mui/icons-material/RestaurantMenu';
import EmailOutlinedIcon from '@mui/icons-material/EmailOutlined';
import LockOutlinedIcon from '@mui/icons-material/LockOutlined';
import Visibility from '@mui/icons-material/Visibility';
import VisibilityOff from '@mui/icons-material/VisibilityOff';
import useAuthStore from '../store/authStore';

export default function LoginPage() {
  const navigate = useNavigate();
  const { login, isLoading } = useAuthStore();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      const user = await login(email, password);
      if (user.role === 'ADMIN') {
        navigate('/admin');
      } else if (user.role === 'KITCHEN') {
        navigate('/kitchen');
      } else {
        navigate('/dashboard');
      }
    } catch (err) {
      setError(err.message || 'Identifiants invalides');
    }
  };

  const fillDemoAccount = (demoEmail, demoPass) => {
    setEmail(demoEmail);
    setPassword(demoPass);
    setError(null);
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #f0fdf4 0%, #f8fafc 100%)',
        p: 2,
      }}
    >
      <Box sx={{ maxWidth: 440, width: '100%' }}>
        {/* Logo / Branding */}
        <Box sx={{ textAlign: 'center', mb: 4 }}>
          <Box
            sx={{
              width: 56,
              height: 56,
              borderRadius: '16px',
              bgcolor: '#16a34a',
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#ffffff',
              boxShadow: '0 10px 25px -5px rgba(22, 163, 74, 0.4)',
              mb: 2,
            }}
          >
            <RestaurantMenuIcon fontSize="large" />
          </Box>
          <Typography variant="h4" fontWeight={800} sx={{ letterSpacing: '-0.02em', color: '#0f172a' }}>
            Food<span style={{ color: '#16a34a' }}>Court</span>
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
            Plateforme de restauration d'entreprise et gestion de cantine
          </Typography>
        </Box>

        {/* Login Card */}
        <Card
          sx={{
            borderRadius: 4,
            boxShadow: '0 20px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.01)',
            border: '1px solid #e2e8f0',
            p: 1,
          }}
        >
          <CardContent sx={{ p: 3 }}>
            <Typography variant="h6" fontWeight={700} sx={{ mb: 1, color: '#0f172a' }}>
              Connexion à votre compte
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
              Saisissez vos identifiants professionnels pour accéder à la cantine.
            </Typography>

            {error && (
              <Alert severity="error" sx={{ mb: 3, borderRadius: 2 }}>
                {error}
              </Alert>
            )}

            <form onSubmit={handleSubmit}>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2.5 }}>
                <TextField
                  label="Email professionnel"
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  fullWidth
                  InputProps={{
                    startAdornment: (
                      <InputAdornment position="start">
                        <EmailOutlinedIcon sx={{ color: '#94a3b8' }} />
                      </InputAdornment>
                    ),
                  }}
                />

                <TextField
                  label="Mot de passe"
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  fullWidth
                  InputProps={{
                    startAdornment: (
                      <InputAdornment position="start">
                        <LockOutlinedIcon sx={{ color: '#94a3b8' }} />
                      </InputAdornment>
                    ),
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton
                          onClick={() => setShowPassword(!showPassword)}
                          edge="end"
                          size="small"
                        >
                          {showPassword ? <VisibilityOff fontSize="small" /> : <Visibility fontSize="small" />}
                        </IconButton>
                      </InputAdornment>
                    ),
                  }}
                />

                <Button
                  type="submit"
                  variant="contained"
                  color="primary"
                  size="large"
                  disabled={isLoading}
                  sx={{
                    py: 1.4,
                    fontSize: '1rem',
                    fontWeight: 700,
                    borderRadius: 2.5,
                    mt: 1,
                  }}
                >
                  {isLoading ? <CircularProgress size={24} color="inherit" /> : 'Se connecter'}
                </Button>
              </Box>
            </form>
          </CardContent>
        </Card>

        {/* Quick Demo Accounts */}
        <Paper
          elevation={0}
          sx={{
            mt: 3,
            p: 2.5,
            borderRadius: 3,
            bgcolor: '#ffffff',
            border: '1px dashed #cbd5e1',
          }}
        >
          <Typography variant="caption" fontWeight={700} color="text.secondary" sx={{ textTransform: 'uppercase', display: 'block', mb: 1.5 }}>
            Comptes de test préconfigurés (mot de passe : password123)
          </Typography>
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
            <Chip
              label="Employé (Alice)"
              clickable
              size="small"
              onClick={() => fillDemoAccount('alice.dupont@foodcourt.com', 'password123')}
              sx={{ bgcolor: '#dcfce7', color: '#15803d', fontWeight: 600 }}
            />
            <Chip
              label="Admin"
              clickable
              size="small"
              onClick={() => fillDemoAccount('admin@foodcourt.com', 'password123')}
              sx={{ bgcolor: '#e0e7ff', color: '#4338ca', fontWeight: 600 }}
            />
            <Chip
              label="Cuisine"
              clickable
              size="small"
              onClick={() => fillDemoAccount('kitchen@foodcourt.com', 'password123')}
              sx={{ bgcolor: '#fef3c7', color: '#b45309', fontWeight: 600 }}
            />
          </Box>
        </Paper>
      </Box>
    </Box>
  );
}

