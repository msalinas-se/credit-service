package com.nttdata.bank.credit.client;

import com.nttdata.bank.credit.exception.BusinessRuleException;
import com.nttdata.bank.credit.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigurationProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

class CustomerClientTest {

  private final ReactiveResilience4JCircuitBreakerFactory factory =
      new ReactiveResilience4JCircuitBreakerFactory(CircuitBreakerRegistry.ofDefaults(),
          TimeLimiterRegistry.ofDefaults(), new Resilience4JConfigurationProperties());

  private CustomerClient client(HttpStatus status, String body) {
    WebClient webClient = WebClient.builder()
        .exchangeFunction(request -> Mono.just(ClientResponse.create(status)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(body)
            .build()))
        .build();
    return new CustomerClient(webClient, factory);
  }

  @Test
  void findCustomer() {
    client(HttpStatus.OK, "{\"id\":\"1\",\"type\":\"PERSONAL\"}").findById("1").test()
        .awaitDone(2, TimeUnit.SECONDS)
        .assertValue(customer -> customer.id().equals("1"));
  }

  @Test
  void customerNotFound() {
    client(HttpStatus.NOT_FOUND, "{}").findById("1").test()
        .awaitDone(2, TimeUnit.SECONDS)
        .assertError(BusinessRuleException.class);
  }

  @Test
  void customerServiceDown() {
    client(HttpStatus.INTERNAL_SERVER_ERROR, "{}").findById("1").test()
        .awaitDone(2, TimeUnit.SECONDS)
        .assertError(ServiceUnavailableException.class);
  }
}
