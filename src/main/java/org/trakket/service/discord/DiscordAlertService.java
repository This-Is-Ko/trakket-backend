package org.trakket.service.discord;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Service
public class DiscordAlertService {

    private final WebClient discordWebClient;

    @Value("${discord.webhook.url}")
    private String webhookUrl;

    @Value("${discord.webhook.username:SofaScore Sync Bot}")
    private String username;

    public DiscordAlertService(@Qualifier("discordWebClient") WebClient discordWebClient) {
        this.discordWebClient = discordWebClient;
    }

    public void sendAlert(String message) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.error("Discord webhook URL is not set. Skipping notification.");
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> payload = Map.of(
                "username", username,
                "content", message
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            discordWebClient.post()
                    .uri(webhookUrl)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .doOnError(e -> log.error("Failed to send Discord notification", e))
                    .subscribe();
        } catch (Exception e) {
            log.error("Failed to send Discord notification: " + e.getMessage());
        }
    }
}
