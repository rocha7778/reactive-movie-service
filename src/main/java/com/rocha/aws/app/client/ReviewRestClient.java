package com.rocha.aws.app.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.rocha.aws.app.domain.Review;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ReviewRestClient {

	@Autowired
	private WebClient webCliente;

	@Value("${restClient.reviews-info-url}")
	private String REVIEW_URL;

	public Flux<Review> getMovieById(String id) {

		log.info("Rocha Review id , {}", id);
		log.info("Rocha Review URL , {}", REVIEW_URL);
		
		var url = UriComponentsBuilder
				.fromHttpUrl(REVIEW_URL)
				.queryParam("movieInfoId", id)
				.buildAndExpand()
				.toString();

		return webCliente.get().uri(url)
				.retrieve()
				.onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
			log.info("status code , {}", clientResponse.statusCode().value());
			if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
				return Mono.empty();
			}

			return clientResponse.bodyToMono(String.class)
					.flatMap(responseMessage -> Mono.error(new Exception("Generic error")));

		}).onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
			log.info("Status code is : {}", clientResponse.statusCode().value());
			log.info("Rocha review clientResponse.toString() : {}", clientResponse.toString());
			return clientResponse.bodyToMono(String.class)
					.flatMap(responseMessage -> Mono.error(new Exception("Generic error")));
		}).bodyToFlux(Review.class).log();

	}

}
