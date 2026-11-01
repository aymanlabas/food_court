package com.foodcourt.usagers.service;

import com.foodcourt.usagers.dto.BalanceUpdateRequest;
import com.foodcourt.usagers.dto.UserResponse;
import com.foodcourt.usagers.dto.UserUpdateRequest;
import com.foodcourt.usagers.entity.User;
import com.foodcourt.usagers.exception.ResourceNotFoundException;
import com.foodcourt.usagers.mapper.UserMapper;
import com.foodcourt.usagers.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");
        return userMapper.toResponseList(userRepository.findAll());
    }

    public UserResponse getUserById(Long id) {
        log.info("Fetching user by ID: {}", id);
        User user = findUserOrThrow(id);
        return userMapper.toResponse(user);
    }

    public UserResponse getUserByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'email : " + email));
        return userMapper.toResponse(user);
    }

    public BigDecimal getUserBalance(Long id) {
        log.info("Fetching balance for user ID: {}", id);
        User user = findUserOrThrow(id);
        return user.getSoldeCantine();
    }

    @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        log.info("Updating user ID: {}", id);
        User user = findUserOrThrow(id);

        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Cet email est déjà utilisé par un autre compte");
            }
        }

        userMapper.updateEntityFromRequest(request, user);
        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user ID: {}", id);
        User user = findUserOrThrow(id);
        userRepository.delete(user);
    }

    @Transactional
    public UserResponse updateBalance(Long id, BalanceUpdateRequest request) {
        log.info("Crediting balance for user ID: {} with amount: {}", id, request.getMontant());
        User user = findUserOrThrow(id);
        user.setSoldeCantine(user.getSoldeCantine().add(request.getMontant()));
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'identifiant : " + id));
    }
}

