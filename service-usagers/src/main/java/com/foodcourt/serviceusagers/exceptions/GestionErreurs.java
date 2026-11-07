package com.foodcourt.serviceusagers.exceptions;

import com.foodcourt.serviceusagers.dtos.ErreurReponse;
import com.foodcourt.serviceusagers.dtos.ErreurValidation;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GestionErreurs {

    @ExceptionHandler(RessourceIntrouvableException.class)
    public ResponseEntity<ErreurReponse> introuvable(RessourceIntrouvableException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErreurReponse(exception.getMessage()));
    }

    @ExceptionHandler(EmailDejaUtiliseException.class)
    public ResponseEntity<ErreurReponse> emailDejaUtilise(EmailDejaUtiliseException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErreurReponse(exception.getMessage()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErreurReponse> conflit(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErreurReponse("Un usager avec cet email existe déjà"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurValidation> validation(MethodArgumentNotValidException exception) {
        Map<String, String> erreurs = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> erreurs.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(new ErreurValidation("Données invalides", erreurs));
    }
}
