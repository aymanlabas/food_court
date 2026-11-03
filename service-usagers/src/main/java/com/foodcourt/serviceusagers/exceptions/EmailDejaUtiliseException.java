package com.foodcourt.serviceusagers.exceptions;

public class EmailDejaUtiliseException extends RuntimeException {

    public EmailDejaUtiliseException(String email) {
        super("Un usager avec l'email " + email + " existe déjà");
    }
}
