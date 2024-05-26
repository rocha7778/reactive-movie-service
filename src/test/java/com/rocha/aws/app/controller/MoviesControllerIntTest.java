package com.rocha.aws.app.controller;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.rocha.aws.app.domain.Movie;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWireMock(port = 8084)
@TestPropertySource(properties = { "restClient.movies-info-url=http://localhost:8084/v1/movies",
		"restClient.reviews-info-url=http://localhost:8084/v1/reviews", })
public class MoviesControllerIntTest {

	@Autowired
	WebTestClient webTestClient;

	@Test
	void getMovieInfoById() {
		
		// given
		
		var id = "abc";
		
		stubFor(get(urlEqualTo("/v1/movies/" + id))
				.willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBody(getMovieInfoJson()))
				);
		
		stubFor(get(urlPathEqualTo("/v1/reviews"))
				.willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBody(getReviewInfoJson()))
				);
		// when
		
		webTestClient.get().uri("/v1/movies/{id}",id)
		.exchange()
		.expectStatus()
		.isOk()
		.expectBody(Movie.class);
		
		// then
		
	}
	
	
	@Test
	void getMovieInfoById_404() {
		
		// given
		
		var id = "abc";
		
		stubFor(get(urlEqualTo("/v1/movies/" + id))
				.willReturn(
						aResponse()
						.withStatus(404)
						)
				);
		
		stubFor(get(urlPathEqualTo("/v1/reviews"))
				.willReturn(aResponse()
						.withStatus(404))
				);
		// when
		
		webTestClient.get().uri("/v1/movies/{id}",id)
		.exchange()
		.expectStatus()
		.isBadRequest()
		.expectBody(String.class)
		.consumeWith(e ->{
			 var responseBody = e.getResponseBody();
			 assertEquals("There are not movie info", responseBody);
		});
		
		// then
		
	}
	
	@Test
	void getMovieInfoById_500() {
		// given
		var id = "abc";
		
		stubFor(get(urlEqualTo("/v1/movies/" + id))
				.willReturn(
						aResponse()
						.withStatus(500)
						.withStatusMessage("Connection refused: no further information: localhost/127.0.0.1:8080")
						)
				);
		
	
		// when
		webTestClient.get().uri("/v1/movies/{id}",id)
		.exchange()
		.expectStatus()
		.is5xxServerError();
		// then
		
	}

	String getMovieInfoJson() {
		return """
				{
				  "id": "1",
				  "name": "Batman Begins",
				  "year": 2005,
				  "cast": [
				    "Christian Bale",
				    "Michael Cane"
				  ],
				  "release_date": "2005-06-15"
				}
		""";
	}
	
	String getReviewInfoJson() {
		return """
				
[
  {
    "id": "1",
    "movieInfoId": 1,
    "comment": "Awesome Movie",
    "rating": 9.0
  },
  {
    "id": "2",
    "movieInfoId": 1,
    "comment": "Excellent Movie",
    "rating": 8.0
  }
]
				
				""";
	}
}
