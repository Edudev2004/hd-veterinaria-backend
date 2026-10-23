package com.vethd.backend.services.impl;

import com.vethd.backend.services.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ResendEmailServiceImpl implements EmailService {

    private final RestClient restClient;
    private final String apiKey;
    private final String fromEmail;
    private final String frontendUrl;

    @Autowired
    public ResendEmailServiceImpl(
            @Value("${resend.api-key:}") String apiKey,
            @Value("${resend.from:VetHD <onboarding@resend.dev>}") String fromEmail,
            @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.frontendUrl = (frontendUrl != null && !frontendUrl.isBlank())
                ? (frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl)
                : "http://localhost:3000";
        this.restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public ResendEmailServiceImpl(String apiKey, String fromEmail) {
        this(apiKey, fromEmail, "http://localhost:3000");
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetUrl = frontendUrl + "/restablecer-password?token=" + resetToken + "&email=" + toEmail;

        String htmlContent = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px;">
                    <h2 style="color: #0f172a;">Recuperación de Contraseña - VetHD</h2>
                    <p style="color: #475569; font-size: 15px;">Hola,</p>
                    <p style="color: #475569; font-size: 15px;">Hemos recibido una solicitud para restablecer la contraseña de tu cuenta en VetHD.</p>
                    <p style="color: #475569; font-size: 15px;">Haz clic en el siguiente botón para continuar:</p>
                    <div style="text-align: center; margin: 24px 0;">
                        <a href="%s" style="background-color: #2563eb; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;">Restablecer mi contraseña</a>
                    </div>
                    <p style="color: #64748b; font-size: 13px;">Este enlace es válido únicamente por <strong>30 minutos</strong>.</p>
                    <p style="color: #64748b; font-size: 13px;">Si no solicitaste este cambio, puedes ignorar este correo de forma segura.</p>
                    <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;" />
                    <p style="color: #94a3b8; font-size: 12px;">© 2026 Clínica Veterinaria VetHD. Todos los derechos reservados.</p>
                </div>
                """.formatted(resetUrl);

        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("test_")) {
            log.info("[RESEND] Modo simulación/test: Correo para {} con enlace {}", toEmail, resetUrl);
            return;
        }

        try {
            Map<String, Object> body = Map.of(
                    "from", fromEmail,
                    "to", List.of(toEmail),
                    "subject", "Recuperación de Contraseña - VetHD",
                    "html", htmlContent
            );

            String response = restClient.post()
                    .uri("/emails")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            log.info("[RESEND] Correo de recuperación enviado con éxito a {}: {}", toEmail, response);
        } catch (Exception e) {
            log.warn("[RESEND] No se pudo enviar el correo a {} vía Resend API ({}). Enlace generado: {}", toEmail, e.getMessage(), resetUrl);
        }
    }
}
