package com.eon.demo;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.http.MediaType;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

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

	public static void main(String[] args) {
		SpringApplication.run(RsocketServiceAApplication.class, args);
	}

	@Value("${rsocket.port}")
	private int port;

	@Bean
	RSocketRequester requester(RSocketRequester.Builder builder) {
		log.info("Initializing RSocket Requester on port " + port);
		return builder.dataMimeType(MimeTypeUtils.APPLICATION_JSON).tcp("localhost", port);
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