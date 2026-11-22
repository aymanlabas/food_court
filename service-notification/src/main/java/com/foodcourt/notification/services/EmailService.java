package com.foodcourt.notification.services;

import com.foodcourt.notification.dtos.NotificationRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendNotificationEmail(NotificationRequest request) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom("no-reply@foodcourt.ma");
        mail.setTo(request.recipient());
        mail.setSubject("FoodCourt - Notification (" + request.type() + ")");
        mail.setText(
                "Bonjour,\n\n" +
                        request.message() + "\n\n" +
                        "Commande ID : #" + request.orderId() + "\n" +
                        "Merci d'utiliser FoodCourt !"
        );
        mailSender.send(mail);
        log.info("Email envoyé via Mailpit à {} pour la commande #{}", request.recipient(), request.orderId());
    }
}