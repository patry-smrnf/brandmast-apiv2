package com.brandmast.api.service;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Optional;



@Service
public class GeoService {

    private final WebClient webClient;

    // Constructor‐injection of a WebClient.Builder
    public GeoService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://nominatim.openstreetmap.org")
                // set a sensible default User‑Agent
                .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36 Edg/138.0.0.0")
                .build();
    }

    /**
     * Fetches coordinates for the given address and postal code.
     * Returns an Optional.empty() if no result is found.
     */
    public Mono<Optional<Coordinates>> getCoordinates(String address, String zipcode) {

        String rawJson = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("street", address)
                        .queryParam("postalcode", zipcode)
                        .queryParam("country", "Poland")
                        .queryParam("format", "json")
                        .queryParam("limit", 1)
                        .build())
                .header(HttpHeaders.USER_AGENT, "Mozilla/5.0")  // Use a common User-Agent
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("Raw JSON response: " + rawJson);
        return webClient.get()
                .uri(uri -> uri
                        .path("/search")
                        .queryParam("street", address)
                        .queryParam("postalcode", zipcode)
                        .queryParam("country", "Poland")
                        .queryParam("format", "json")
                        .queryParam("limit", 1)
                        .build())
                .retrieve()
                .bodyToMono(NominatimResponse[].class)
                // timeout in case the remote hangs
                .timeout(Duration.ofSeconds(5))
                // map to Optional<Coordinates>
                .map(array -> {
                    if (array.length == 0) {
                        return Optional.<Coordinates>empty();
                    }
                    var r = array[0];
                    return Optional.of(new Coordinates(r.lat(), r.lon()));
                })
                // propagate empty or value even on 404/no‑body
                .onErrorResume(ex -> {
                    // log.warn("Geo lookup failed", ex);
                    return Mono.just(Optional.empty());
                });
    }

    // Java 16+ record for the JSON‐mapped response
    private record NominatimResponse(String lat, String lon) {}

    // Java 16+ record for our returned coordinates
    public record Coordinates(String latitude, String longitude) {}
}
