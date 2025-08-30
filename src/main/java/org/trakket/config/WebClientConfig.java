package org.trakket.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${webclient.max-memory-bytes:4194304}") // 4MB default
    private int maxMemoryBytes;

    @Value("${webclient.connect-timeout-ms:5000}")
    private int connectTimeoutMs;

    @Value("${webclient.response-timeout-seconds:20}")
    private int responseTimeoutSeconds;

    @Value("${webclient.read-timeout-seconds:30}")
    private int readTimeoutSeconds;

    @Value("${webclient.max-connections:50}")
    private int maxConnections;

    /**
     * Shared connection provider so all WebClients created here reuse a single connection pool.
     * This avoids creating one pool per WebClient while keeping per-source WebClient config.
     */
    @Bean
    public ConnectionProvider sharedConnectionProvider() {
        return ConnectionProvider.builder("trakket-connection-pool")
                .maxConnections(maxConnections)
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .build();
    }

    private ReactorClientHttpConnector createClientConnector(ConnectionProvider provider) {
        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
                .responseTimeout(Duration.ofSeconds(responseTimeoutSeconds))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(readTimeoutSeconds))
                        .addHandlerLast(new WriteTimeoutHandler(readTimeoutSeconds))
                );

        return new ReactorClientHttpConnector(httpClient);
    }

    /**
     * Preconfigured builder used to create named WebClient beans.
     * We intentionally return a new builder instance each call (not a shared Builder bean)
     * to avoid accidental stateful reuse of a builder.
     */
    private WebClient.Builder preconfiguredBuilder() {
        return WebClient.builder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(maxMemoryBytes))
                        .build());
    }

    /**
     * Generic default WebClient (primary). Useful for internal or fallback uses.
     */
    @Bean
    @Primary
    public WebClient webClient(ConnectionProvider sharedConnectionProvider) {
        return preconfiguredBuilder()
                .clientConnector(createClientConnector(sharedConnectionProvider))
                .build();
    }

    /**
     * SofaScore WebClient (named). Use @Qualifier("sofascoreWebClient") when injecting.
     */
    @Bean
    public WebClient sofascoreWebClient(ConnectionProvider sharedConnectionProvider) {
        return preconfiguredBuilder()
                .clientConnector(createClientConnector(sharedConnectionProvider))
                .baseUrl("https://www.sofascore.com")
                .build();
    }

    /**
     * Fantasy Premier League WebClient (named). Use @Qualifier("fplWebClient") when injecting.
     */
    @Bean
    public WebClient fplWebClient(ConnectionProvider sharedConnectionProvider) {
        return preconfiguredBuilder()
                .clientConnector(createClientConnector(sharedConnectionProvider))
                .baseUrl("https://fantasy.premierleague.com/api")
                .build();
    }

    // Add more per-source beans here when needed (e.g., @Bean public WebClient anotherSourceWebClient(...))
}
