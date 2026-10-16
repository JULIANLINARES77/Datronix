package uniminuto.datronix.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;

import uniminuto.datronix.exception.CorreoNoDisponibleException;

@Service
public class EmailService {
    private final Resend resend;
    private final String fromAddress;

    public EmailService(
            @Value("${resend.api-key}") String apiKey,
            @Value("${resend.from-address:onboarding@resend.dev}") String fromAddress) {
        this.resend = new Resend(apiKey);
        this.fromAddress = fromAddress;
    }

    public void enviarCorreo(String destinatario, String asunto, String cuerpoHtml) {
        CreateEmailOptions options = CreateEmailOptions.builder()
                .from(fromAddress)
                .to(destinatario)
                .subject(asunto)
                .html(cuerpoHtml)
                .build();
        try {
            resend.emails().send(options);
        } catch (ResendException ex) {
            throw new CorreoNoDisponibleException(ex);
        }
    }
}
