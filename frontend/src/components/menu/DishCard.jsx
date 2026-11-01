import React from 'react';
import {
  Card,
  CardMedia,
  CardContent,
  CardActions,
  Typography,
  Button,
  Box,
  Chip,
} from '@mui/material';
import AddShoppingCartIcon from '@mui/icons-material/AddShoppingCart';
import BlockIcon from '@mui/icons-material/Block';
import { formatPrice, formatCategory } from '../../utils/formatters';
import { CATEGORY_EMOJI } from '../../utils/constants';
import useCartStore from '../../store/cartStore';

export default function DishCard({ dish, onAddToCart }) {
  const { addItem } = useCartStore();

  const handleAdd = () => {
    if (!dish.disponible) return;
    if (onAddToCart) {
      onAddToCart(dish);
    } else {
      addItem(dish);
    }
  };

  const allergensList = dish.allergenes
    ? dish.allergenes.split(',').map((a) => a.trim())
    : [];

  return (
    <Card
      sx={{
        display: 'flex',
        flexDirection: 'column',
        height: '100%',
        borderRadius: 4,
        overflow: 'hidden',
        border: '1px solid #f1f5f9',
        boxShadow: '0 1px 3px 0 rgba(0,0,0,0.04)',
        opacity: dish.disponible ? 1 : 0.75,
        transition: 'transform 0.2s, box-shadow 0.2s',
        '&:hover': dish.disponible
          ? {
              transform: 'translateY(-4px)',
              boxShadow: '0 10px 25px -5px rgba(0,0,0,0.08)',
            }
          : {},
      }}
    >
      <Box sx={{ position: 'relative', height: 180, bgcolor: '#f1f5f9' }}>
        {dish.imageUrl ? (
          <CardMedia
            component="img"
            height="180"
            image={dish.imageUrl}
            alt={dish.nom}
            sx={{ objectFit: 'cover' }}
          />
        ) : (
          <Box
            sx={{
              height: '100%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: '4rem',
              bgcolor: '#f8fafc',
            }}
          >
            {CATEGORY_EMOJI[dish.categorie] || '🍽️'}
          </Box>
        )}

        {/* Category tag */}
        <Chip
          label={formatCategory(dish.categorie)}
          size="small"
          sx={{
            position: 'absolute',
            top: 12,
            left: 12,
            bgcolor: 'rgba(255, 255, 255, 0.95)',
            backdropFilter: 'blur(4px)',
            fontWeight: 700,
            fontSize: '0.75rem',
          }}
        />

        {/* Status overlay if unavailable */}
        {!dish.disponible && (
          <Box
            sx={{
              position: 'absolute',
              inset: 0,
              bgcolor: 'rgba(15, 23, 42, 0.65)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#ffffff',
              gap: 1,
              fontWeight: 700,
              backdropFilter: 'blur(2px)',
            }}
          >
            <BlockIcon fontSize="small" />
            Épuisé / Indisponible
          </Box>
        )}
      </Box>

      <CardContent sx={{ flexGrow: 1, p: 2.5 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', mb: 1 }}>
          <Typography variant="h6" fontWeight={700} sx={{ fontSize: '1.05rem', color: '#0f172a' }}>
            {dish.nom}
          </Typography>
          <Typography variant="h6" fontWeight={800} sx={{ color: '#16a34a' }}>
            {formatPrice(dish.prix)}
          </Typography>
        </Box>

        <Typography
          variant="body2"
          color="text.secondary"
          sx={{
            mb: 2,
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            display: '-webkit-box',
            WebkitLineClamp: 2,
            WebkitBoxOrient: 'vertical',
            minHeight: '2.8em',
          }}
        >
          {dish.description || 'Plat préparé quotidiennement par l’équipe de cuisine.'}
        </Typography>

        {allergensList.length > 0 && (
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, mt: 'auto' }}>
            {allergensList.map((allergen, idx) => (
              <Chip
                key={idx}
                label={allergen}
                size="small"
                variant="outlined"
                sx={{ fontSize: '0.68rem', height: 20, color: '#64748b' }}
              />
            ))}
          </Box>
        )}
      </CardContent>

      <CardActions sx={{ p: 2, pt: 0 }}>
        <Button
          fullWidth
          variant="contained"
          color="primary"
          disabled={!dish.disponible}
          onClick={handleAdd}
          startIcon={<AddShoppingCartIcon />}
          sx={{ borderRadius: 2.5, py: 1 }}
        >
          {dish.disponible ? 'Ajouter au panier' : 'Indisponible'}
        </Button>
      </CardActions>
    </Card>
  );
}

