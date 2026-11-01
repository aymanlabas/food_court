import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  MenuItem,
} from '@mui/material';
import { ROLES, DIETARY_OPTIONS } from '../../utils/constants';

export default function EmployeeForm({ open, onClose, onSubmit, employee = null, loading = false }) {
  const [formData, setFormData] = useState({
    nom: '',
    email: '',
    password: '',
    role: 'EMPLOYEE',
    soldeCantine: '100.00',
    regimeAlimentaire: '',
  });

  const [errors, setErrors] = useState({});

  useEffect(() => {
    if (employee) {
      setFormData({
        nom: employee.nom || '',
        email: employee.email || '',
        password: '',
        role: employee.role || 'EMPLOYEE',
        soldeCantine: employee.soldeCantine !== undefined ? employee.soldeCantine : '100.00',
        regimeAlimentaire: employee.regimeAlimentaire || '',
      });
    } else {
      setFormData({
        nom: '',
        email: '',
        password: '',
        role: 'EMPLOYEE',
        soldeCantine: '100.00',
        regimeAlimentaire: '',
      });
    }
    setErrors({});
  }, [employee, open]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: null }));
    }
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.nom.trim()) newErrors.nom = 'Le nom est obligatoire';
    if (!formData.email.trim() || !/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Un email valide est obligatoire';
    }
    if (!employee && (!formData.password || formData.password.length < 6)) {
      newErrors.password = 'Le mot de passe doit contenir au moins 6 caractères';
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!validate()) return;
    onSubmit(formData);
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <form onSubmit={handleSubmit}>
        <DialogTitle fontWeight={700}>
          {employee ? 'Modifier l’employé' : 'Ajouter un employé'}
        </DialogTitle>
        <DialogContent dividers sx={{ display: 'flex', flexDirection: 'column', gap: 2.5, pt: 2 }}>
          <TextField
            name="nom"
            label="Nom complet"
            value={formData.nom}
            onChange={handleChange}
            error={Boolean(errors.nom)}
            helperText={errors.nom}
            fullWidth
            required
          />

          <TextField
            name="email"
            label="Email professionnel"
            type="email"
            value={formData.email}
            onChange={handleChange}
            error={Boolean(errors.email)}
            helperText={errors.email}
            fullWidth
            required
          />

          {!employee && (
            <TextField
              name="password"
              label="Mot de passe temporaire"
              type="password"
              value={formData.password}
              onChange={handleChange}
              error={Boolean(errors.password)}
              helperText={errors.password}
              fullWidth
              required
            />
          )}

          <TextField
            name="role"
            label="Rôle"
            select
            value={formData.role}
            onChange={handleChange}
            fullWidth
            required
          >
            {Object.keys(ROLES).map((roleKey) => (
              <MenuItem key={roleKey} value={roleKey}>
                {roleKey}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            name="regimeAlimentaire"
            label="Régime alimentaire"
            placeholder="Ex : Végétarien, Sans gluten, etc."
            value={formData.regimeAlimentaire}
            onChange={handleChange}
            fullWidth
          />
        </DialogContent>
        <DialogActions sx={{ px: 3, py: 2 }}>
          <Button onClick={onClose} color="inherit" disabled={loading}>
            Annuler
          </Button>
          <Button type="submit" variant="contained" color="primary" disabled={loading}>
            {loading ? 'Enregistrement...' : employee ? 'Mettre à jour' : 'Créer l’employé'}
          </Button>
        </DialogActions>
      </form>
    </Dialog>
  );
}

