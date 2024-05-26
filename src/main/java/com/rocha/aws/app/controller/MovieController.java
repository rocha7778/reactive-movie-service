package com.rocha.aws.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rocha.aws.app.client.MovieInfoRestClient;
import com.rocha.aws.app.client.ReviewRestClient;
import com.rocha.aws.app.domain.Movie;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/movies")
public class MovieController {
	

	@Autowired
	private MovieInfoRestClient movieInfoClient;
	
	@Autowired
	private ReviewRestClient reviewRestClient;
	

	@GetMapping("/{id}")
	public Mono<Movie> getMovieById(@PathVariable String id){
		return movieInfoClient.getMovieById(id)
				.flatMap(movieInfo -> reviewRestClient.getMovieById(movieInfo.getId())
						.collectList()
						.map(reviewList -> new Movie(movieInfo, reviewList)))
				.switchIfEmpty(Mono.empty());
	}
}
