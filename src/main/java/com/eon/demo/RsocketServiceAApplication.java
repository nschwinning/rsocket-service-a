package com.eon.demo;

import java.time.LocalDateTime;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import io.rsocket.metadata.WellKnownMimeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@SpringBootApplication
public class RsocketServiceAApplication {
	
	private static final MimeType SIMPLE_AUTH = MimeTypeUtils
			.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString());

	public static void main(String[] args) {
		SpringApplication.run(RsocketServiceAApplication.class, args);
	}

	@Value("${rsocket.port}")
	private int port;

	@Bean
	RSocketRequester requester(RSocketRequester.Builder builder) {

        UsernamePasswordMetadata user = new UsernamePasswordMetadata("user", "password");
		
		log.info("Initializing RSocket Requester on port " + port);
		return builder
				.dataMimeType(MimeTypeUtils.APPLICATION_JSON)
				.setupMetadata(user, SIMPLE_AUTH)
                .rsocketStrategies(b -> b.encoder(new SimpleAuthenticationEncoder()))
				.tcp("localhost", port);
	}

}

@Slf4j
@Configuration
class FlywayConfiguration {
	
	private final Environment env;

    public FlywayConfiguration(final Environment env) {
        this.env = env;
    }

    @Bean(initMethod = "migrate")
    public Flyway flyway() {
        var url = env.getRequiredProperty("spring.flyway.url");
        var user = env.getRequiredProperty("spring.flyway.user");
        var password = env.getRequiredProperty("spring.flyway.password");

        log.info("Configuring database with flyway for URL: " + url + ", user: " + user);

        return new Flyway(Flyway.configure().dataSource(url, user, password));
    }
	
}

@Slf4j
@RequiredArgsConstructor
@RestController
class QuoteController {

	private final RSocketRequester requester;
	private final QuoteRepository quoteRepository;

	// Request/Response
	@GetMapping("/quote")
	public Mono<Quote> getQuote() {
		return requester.route("quote")
				.retrieveMono(Quote.class)
				.map(q -> {
					quoteRepository.save(q);
					return q;
				});
	}

	private void handleQuote(Quote c) {
		log.info("Retrieved quote: " + c);
	}

	// Request Stream
	@GetMapping(value = "/quotes/{boundary}")
	public Flux<Quote> getQuotes(@PathVariable int boundary) {
		return requester.route("quotes")
				.data(boundary)
				.retrieveFlux(Quote.class)
				.map(q -> {
					q.setCreatedAt(LocalDateTime.now());
					return q;
				})
				.doOnError(t -> handleError(t))
				.onErrorStop()
				.doOnNext(this::handleQuote)
				.map(quoteRepository::save)
				.flatMap(mono -> mono);
	}
	
	private void handleError(Throwable t) {
		log.error("Error", t);
	}

	@GetMapping("/quotes")
	public Flux<Quote> getAllQuotes() {
		return quoteRepository.findAll();
	}

}

@Slf4j
@RequiredArgsConstructor
@Component
class QuoteHandler {
	
	private final RSocketRequester requester;
	private final QuoteRepository quoteRepository;
	
	@KafkaListener(topics = "quotes", groupId = "service-a")
	public void consume(QuoteDto dto) {
		log.info("Received dto with id " + dto.getId());
		Mono<Quote> qMon = requester.route("quoteById")
			.data(dto.getId())
			.retrieveMono(Quote.class);
		
		qMon.subscribe(q -> {
			log.info("Fetched quote with message " + q.getMessage());
			q.setQuoteId(q.getId());
			q.setId(null);
			quoteRepository.save(q).subscribe();
		});
	}
	
}

interface QuoteRepository extends ReactiveCrudRepository<Quote, Long> {
	
}

@NoArgsConstructor
@AllArgsConstructor
@Data
class Quote {
	
	@Id
	private Integer id;
	private String message;
	private LocalDateTime createdAt;
	private Integer quoteId;

}

@NoArgsConstructor
@AllArgsConstructor
@Data
class QuoteDto {
	
	private Integer id;
	
}
