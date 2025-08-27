package org.sportstracker.service.football.fantasypremierleague;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sportstracker.dto.football.fantasypremierleague.FixtureDto;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class FantasyPremierLeagueClient {

    private final WebClient webClient;
    private static final String FIXTURES_URL = "https://fantasy.premierleague.com/api/fixtures/";

    public Mono<List<FixtureDto>> fetchFixtures() {
        return webClient.get()
                .uri(FIXTURES_URL)
                .retrieve()
                .bodyToMono(FixtureDto[].class)
                .map(Arrays::asList)
                .onErrorResume(e -> {
                    // log the error and return empty list
                    log.error("Failed to fetch fixtures", e);
                    return Mono.just(Collections.emptyList());
                });
    }

}
