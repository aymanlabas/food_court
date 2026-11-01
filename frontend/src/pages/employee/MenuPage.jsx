import React, { useState, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link as RouterLink } from 'react-router-dom';
import {
  Box,
  Typography,
  Grid,
  Fab,
  Badge,
  Snackbar,
  Alert,
} from '@mui/material';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import * as menuApi from '../../api/menuApi';
import DishCard from '../../components/menu/DishCard';
import CategoryFilter from '../../components/menu/CategoryFilter';
import SearchBar from '../../components/menu/SearchBar';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import useCartStore from '../../store/cartStore';

export default function MenuPage() {
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [toastMessage, setToastMessage] = useState(null);

  const { items, addItem } = useCartStore();
  const cartItemCount = items.reduce((acc, item) => acc + item.quantity, 0);

  const { data: dishes = [], isLoading } = useQuery({
    queryKey: ['dishes'],
    queryFn: () => menuApi.getDishes(),
  });

  const filteredDishes = useMemo(() => {
    return dishes.filter((dish) => {
      const matchCategory =
        selectedCategory === 'ALL' || dish.categorie === selectedCategory;
      const matchSearch =
        !searchQuery ||
        dish.nom.toLowerCase().includes(searchQuery.toLowerCase()) ||
        (dish.description && dish.description.toLowerCase().includes(searchQuery.toLowerCase())) ||
        (dish.allergenes && dish.allergenes.toLowerCase().includes(searchQuery.toLowerCase()));
      return matchCategory && matchSearch;
    });
  }, [dishes, selectedCategory, searchQuery]);

  const handleAddToCart = (dish) => {
    addItem(dish);
    setToastMessage(`"${dish.nom}" a été ajouté à votre panier`);
  };

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3.5, pb: 8 }}>
      {/* Page Header */}
      <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, justifyContent: 'space-between', alignItems: { xs: 'flex-start', md: 'center' }, gap: 2 }}>
        <Box>
          <Typography variant="h4" fontWeight={800} color="text.primary" sx={{ letterSpacing: '-0.02em' }}>
            Menu de la Cafétéria 🥗
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Découvrez nos plats du jour préparés chaque matin avec des ingrédients de qualité.
          </Typography>
        </Box>
        <SearchBar value={searchQuery} onChange={setSearchQuery} />
      </Box>

      {/* Category filters */}
      <CategoryFilter
        selectedCategory={selectedCategory}
        onSelectCategory={setSelectedCategory}
      />

      {/* Dishes Grid */}
      {isLoading ? (
        <LoadingSpinner message="Chargement des plats de la cantine..." />
      ) : filteredDishes.length === 0 ? (
        <Box sx={{ textAlign: 'center', py: 8, bgcolor: '#ffffff', borderRadius: 4, border: '1px solid #f1f5f9' }}>
          <Typography variant="h6" fontWeight={700} color="text.primary" sx={{ mb: 1 }}>
            Aucun plat trouvé
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Essayez de modifier votre recherche ou de changer de catégorie.
          </Typography>
        </Box>
      ) : (
        <Grid container spacing={3}>
          {filteredDishes.map((dish) => (
            <Grid item xs={12} sm={6} md={4} lg={3} key={dish.id}>
              <DishCard dish={dish} onAddToCart={handleAddToCart} />
            </Grid>
          ))}
        </Grid>
      )}

      {/* Floating cart action button */}
      <Fab
        component={RouterLink}
        to="/cart"
        color="primary"
        sx={{
          position: 'fixed',
          bottom: 28,
          right: 28,
          boxShadow: '0 10px 25px -5px rgba(22, 163, 74, 0.4)',
        }}
      >
        <Badge badgeContent={cartItemCount} color="error">
          <ShoppingCartIcon />
        </Badge>
      </Fab>

      {/* Toast notification */}
      <Snackbar
        open={Boolean(toastMessage)}
        autoHideDuration={3000}
        onClose={() => setToastMessage(null)}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert onClose={() => setToastMessage(null)} severity="success" sx={{ width: '100%', borderRadius: 2 }}>
          {toastMessage}
        </Alert>
      </Snackbar>
    </Box>
  );
}

