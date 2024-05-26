package com.rocha.aws.app.client;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.rocha.aws.app.domain.MovieInfo;
import com.rocha.aws.app.exceptions.MoviesInfoServerException;

import lombok.extern.slf4j.Slf4j;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@Component
@Slf4j
public class MovieInfoRestClient {
	
	@Autowired
	private WebClient webCliente;
	
	@Value("${restClient.movies-info-url}")
	private String MOVIE_URL;

	public Mono<MovieInfo> getMovieById(String id){
		
		log.info("ROCHA getMovieById"+ MOVIE_URL+"/"+id);
		
		var retrySpec = Retry.backoff(3, Duration.ofSeconds(1)).onRetryExhaustedThrow( (retryBackoffSpec, retrySignal) ->{
			return Exceptions.propagate(retrySignal.failure());
		});
		
		return webCliente.get()
		.uri( MOVIE_URL+"/{id}", id)
		.retrieve()
		.onStatus(HttpStatusCode::is4xxClientError, clientResponse->{
			log.info("status code , {}", clientResponse.statusCode().value());
			if(clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {
				return Mono.error(new Exception("There are not movie info"));
			}else {
				return clientResponse.bodyToMono(String.class).
						flatMap(responseMessage -> {
							return Mono.error(new Exception("Generic error"));
						});
			}
		})
		.onStatus(HttpStatusCode::is5xxServerError, clientResponse ->{
			log.info("Status code is __Entro__ : {}", clientResponse.statusCode().value());
            return clientResponse.bodyToMono(String.class)
            		
                    .flatMap(responseMessage -> {
                    	log.info("Rocha info exploto : {}", responseMessage);
                    	return Mono.error( new MoviesInfoServerException( "Server Exception in MoviesInfoService "  +responseMessage));});
        })
		
		.bodyToMono(MovieInfo.class)
		//.retry(3)
		//.retryWhen(retrySpec)
		.log();
	}

}
