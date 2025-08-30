package org.trakket.client;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.trakket.dto.football.sofascore.SofascoreEventsResponse;
import org.trakket.dto.football.sofascore.SofascoreRoundsResponse;
import reactor.core.publisher.Mono;

@Component
public class SofascoreClient {

    private final WebClient sofascoreWebClient;

    public SofascoreClient(@Qualifier("sofascoreWebClient") WebClient sofascoreWebClient) {
        this.sofascoreWebClient = sofascoreWebClient;
    }

    public Mono<SofascoreEventsResponse> fetchRoundEvents(int uniqueTournamentId, long seasonId, int round) {
        String path = String.format(
                "/api/v1/unique-tournament/%d/season/%d/events/round/%d",
                uniqueTournamentId, seasonId, round
        );
        return sofascoreWebClient
                .get()
                .uri(path)
                .retrieve()
                .bodyToMono(SofascoreEventsResponse.class);
    }

    // Number of rounds in season + current round
    public Mono<SofascoreRoundsResponse> fetchRounds(int uniqueTournamentId, long seasonId) {
        String path = String.format(
                "/api/v1/unique-tournament/%d/season/%d/rounds",
                uniqueTournamentId, seasonId
        );
        return sofascoreWebClient
                .get()
                .uri(path)
                .retrieve()
                .bodyToMono(SofascoreRoundsResponse.class);
    }
}