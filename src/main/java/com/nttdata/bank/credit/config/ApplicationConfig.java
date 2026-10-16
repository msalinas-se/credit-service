package com.nttdata.bank.credit.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/** Beans of the credit microservice. */
@Configuration
public class ApplicationConfig {

  /** Web client builder that resolves service names through Eureka. */
  @Bean
  @LoadBalanced
  public WebClient.Builder webClientBuilder() {
    return WebClient.builder();
  }

  /** Web client for the customer microservice. */
  @Bean
  public WebClient customerWebClient(WebClient.Builder webClientBuilder,
      @Value("${services.customer.url}") String customerUrl) {
    return webClientBuilder.baseUrl(customerUrl).build();
  }

  /** Default circuit breaker configuration with the configured timeout. */
  @Bean
  public Customizer<ReactiveResilience4JCircuitBreakerFactory> circuitBreakerCustomizer(
      @Value("${circuit-breaker.timeout}") Duration timeout) {
    return factory -> factory.configureDefault(id -> new Resilience4JConfigBuilder(id)
        .circuitBreakerConfig(CircuitBreakerConfig.ofDefaults())
        .timeLimiterConfig(TimeLimiterConfig.custom().timeoutDuration(timeout).build())
        .build());
  }
}
