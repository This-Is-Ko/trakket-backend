package org.trakket.service.discord;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Service
public class DiscordAlertService {
    private static final String BOT_USERNAME = "SofaScore Sync Bot";

    private final WebClient discordWebClient;

    @Value("${discord.webhook.url}")
    private String webhookUrl;

    public DiscordAlertService(@Qualifier("discordWebClient") WebClient discordWebClient) {
        this.discordWebClient = discordWebClient;
    }

    public void sendAlert(String message) {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.error("Discord webhook URL is not set. Skipping notification.");
            return;
        }

        Map<String, Object> payload = Map.of(
                "username", BOT_USERNAME,
                "content", message
        );

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

    public void sendCompetitionSyncFailedAlert(String name, Throwable ex) {
        sendAlert("Sync failed for competition: " + name +
                "\nError: " + ex.getMessage());
    }
}
