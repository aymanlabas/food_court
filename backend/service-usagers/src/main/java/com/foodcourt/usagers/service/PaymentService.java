package com.foodcourt.usagers.service;

import com.foodcourt.usagers.dto.DebitRequest;
import com.foodcourt.usagers.dto.DebitResponse;
import com.foodcourt.usagers.dto.PaymentResponse;
import com.foodcourt.usagers.dto.RefundRequest;
import com.foodcourt.usagers.entity.Payment;
import com.foodcourt.usagers.entity.PaymentStatus;
import com.foodcourt.usagers.entity.User;
import com.foodcourt.usagers.exception.DuplicateRefundException;
import com.foodcourt.usagers.exception.InsufficientBalanceException;
import com.foodcourt.usagers.exception.ResourceNotFoundException;
import com.foodcourt.usagers.mapper.PaymentMapper;
import com.foodcourt.usagers.repository.PaymentRepository;
import com.foodcourt.usagers.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        log.info("Fetching payments page: {}", pageable);
        return paymentRepository.findAll(pageable).map(paymentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé : " + id));
        return paymentMapper.toResponse(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentsByUserId(Long userId, Pageable pageable) {
        return paymentRepository.findByUserId(userId, pageable).map(paymentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByMealOrderId(Long mealOrderId) {
        Payment payment = paymentRepository.findByMealOrderId(mealOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé pour la commande : " + mealOrderId));
        return paymentMapper.toResponse(payment);
    }

    /**
     * Débit sécurisé et idempotent du solde badge cantine.
     */
    @Transactional
    public DebitResponse debitBadge(DebitRequest request) {
        log.info("Processing debit for user {} and order {} with key {}",
                request.getUserId(), request.getMealOrderId(), request.getIdempotencyKey());

        // Règle 8 : Idempotence - Ne jamais débiter deux fois pour la même requête
        Optional<Payment> existingPayment = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existingPayment.isPresent()) {
            log.info("Payment with idempotency key {} already processed, returning existing result", request.getIdempotencyKey());
            Payment p = existingPayment.get();
            return DebitResponse.builder()
                    .paymentId(p.getId())
                    .statut(p.getStatut())
                    .build();
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + request.getUserId()));

        // Règle 3 : Vérifier le solde
        if (user.getSoldeCantine().compareTo(request.getMontant()) < 0) {
            log.warn("Insufficient badge balance for user {}. Balance: {}, Required: {}",
                    user.getId(), user.getSoldeCantine(), request.getMontant());

            Payment failedPayment = Payment.builder()
                    .userId(user.getId())
                    .mealOrderId(request.getMealOrderId())
                    .montant(request.getMontant())
                    .moyen("BADGE")
                    .statut(PaymentStatus.ECHOUE)
                    .idempotencyKey(request.getIdempotencyKey())
                    .build();
            Payment saved = paymentRepository.save(failedPayment);

            throw new InsufficientBalanceException("Solde insuffisant sur le badge pour effectuer ce paiement");
        }

        // Débit du solde
        user.setSoldeCantine(user.getSoldeCantine().subtract(request.getMontant()));
        userRepository.save(user);

        // Enregistrement du paiement confirmé
        Payment payment = Payment.builder()
                .userId(user.getId())
                .mealOrderId(request.getMealOrderId())
                .montant(request.getMontant())
                .moyen("BADGE")
                .statut(PaymentStatus.CONFIRME)
                .idempotencyKey(request.getIdempotencyKey())
                .build();
        Payment saved = paymentRepository.save(payment);

        log.info("Payment confirmed with ID: {}. New user balance: {}", saved.getId(), user.getSoldeCantine());
        return DebitResponse.builder()
                .paymentId(saved.getId())
                .statut(saved.getStatut())
                .build();
    }

    /**
     * Remboursement sécurisé et idempotent du solde badge suite à une annulation.
     */
    @Transactional
    public PaymentResponse refundPayment(RefundRequest request) {
        log.info("Processing refund for order: {} with key: {}", request.getMealOrderId(), request.getIdempotencyKey());

        Payment payment = paymentRepository.findByMealOrderId(request.getMealOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Aucun paiement trouvé pour la commande : " + request.getMealOrderId()));

        // Règle 9 : Ne jamais rembourser deux fois
        if (payment.getStatut() == PaymentStatus.REMBOURSE) {
            log.info("Payment for order {} is already refunded", request.getMealOrderId());
            return paymentMapper.toResponse(payment);
        }

        if (payment.getStatut() != PaymentStatus.CONFIRME) {
            throw new DuplicateRefundException("Impossible de rembourser un paiement qui n'a pas été confirmé");
        }

        User user = userRepository.findById(payment.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + payment.getUserId()));

        // Recréditer le solde
        user.setSoldeCantine(user.getSoldeCantine().add(payment.getMontant()));
        userRepository.save(user);

        // Mettre à jour le statut du paiement
        payment.setStatut(PaymentStatus.REMBOURSE);
        Payment updatedPayment = paymentRepository.save(payment);

        log.info("Payment refunded successfully for order {}. Restored balance: {}",
                request.getMealOrderId(), user.getSoldeCantine());

        return paymentMapper.toResponse(updatedPayment);
    }
}

