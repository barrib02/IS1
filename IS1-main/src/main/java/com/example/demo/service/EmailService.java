
package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String remitente;
    private final String baseUrl;

    // Inyectamos el servicio de correo y la configuración
    // definida en application.properties.
    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String remitente,
            @Value("${app.verification.base-url}") String baseUrl) {

        this.mailSender = mailSender;
        this.remitente = remitente;
        this.baseUrl = baseUrl;
    }

    // H6.1 - ENVIAR CORREO DE VERIFICACIÓN
    public void enviarCorreoVerificacion(
            String destinatario,
            String token) {

        // Construimos el enlace de activación de la cuenta.
        String enlace = baseUrl
                + "/api/auth/verificar?token="
                + token;

        // Creamos el mensaje de correo electrónico.
        SimpleMailMessage mensaje = new SimpleMailMessage();

        mensaje.setFrom(remitente);
        mensaje.setTo(destinatario);
        mensaje.setSubject("Shelfie - Verifica tu correo electrónico");

        mensaje.setText(
                "¡Bienvenido a Shelfie!\n\n"
                + "Gracias por registrarte.\n\n"
                + "Para verificar tu correo electrónico, "
                + "abre el siguiente enlace:\n\n"
                + enlace
                + "\n\n"
                + "Este enlace caduca en 24 horas.\n\n"
                + "Si no has creado una cuenta en Shelfie, "
                + "puedes ignorar este mensaje.\n\n"
                + "El equipo de Shelfie"
        );

        // Enviamos el correo mediante Gmail.
        mailSender.send(mensaje);
    }
}
