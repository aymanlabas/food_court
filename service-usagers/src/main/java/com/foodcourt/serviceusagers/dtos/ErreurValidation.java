package com.foodcourt.serviceusagers.dtos;

import java.util.Map;

public record ErreurValidation(String message, Map<String, String> erreurs) {
}
