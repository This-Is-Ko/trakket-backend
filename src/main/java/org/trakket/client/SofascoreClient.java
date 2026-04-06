package org.trakket.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.trakket.dto.football.sofascore.SofascoreEventsResponse;
import org.trakket.dto.football.sofascore.SofascoreRoundsResponse;
import org.trakket.dto.football.sofascore.SofascoreSeasonsResponse;
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

    public Mono<SofascoreEventsResponse> fetchRoundEventsWithSlug(int uniqueTournamentId, long seasonId, int round, String roundSlug) {
        String path = String.format(
                "/api/v1/unique-tournament/%d/season/%d/events/round/%d/slug/%s",
                uniqueTournamentId, seasonId, round, roundSlug
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

    public Mono<SofascoreSeasonsResponse> fetchSeasons(int uniqueTournamentId) {
        String path = String.format(
                "/api/v1/unique-tournament/%d/seasons",
                uniqueTournamentId
        );
        return sofascoreWebClient
                .get()
                .uri(path)
                .retrieve()
                .bodyToMono(SofascoreSeasonsResponse.class);
    }
}