package com.thehecklers.conditionsservice;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@SpringBootApplication
@EnableDiscoveryClient
public class ConditionsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConditionsServiceApplication.class, args);
    }

}

@RestController
class ConditionsController {
    private final WebClient apClient = WebClient.create("http://airport-service");
    private final WebClient wxClient = WebClient.create("http://weather-service");

    @GetMapping
    String greeting() {
        return "Greetings DEVDAY!!!";
    }

    @GetMapping("/summary")
    Flux<METAR> getSummaryForAirports() {
        return apClient.get()
                .retrieve()
                .bodyToFlux(Airport.class)
                .flatMap(ap -> wxClient.get()
                        .uri("/metar/{icao}", ap.getIcao())
                        .retrieve()
                        .bodyToMono(METAR.class))
                .log();
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class Airport {
    private String icao;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class METAR {
    private String raw;
    private Time time;
    private String flight_rules;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Time {
    private ZonedDateTime dt;
    private String repr;
}
