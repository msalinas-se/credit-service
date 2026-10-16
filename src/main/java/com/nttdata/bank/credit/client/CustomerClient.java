package com.nttdata.bank.credit.client;

import com.nttdata.bank.credit.client.dto.CustomerDto;
import com.nttdata.bank.credit.exception.BusinessRuleException;
import com.nttdata.bank.credit.exception.ServiceUnavailableException;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/** Client of the customer microservice protected by a circuit breaker. */
@Component
@RequiredArgsConstructor
public class CustomerClient {
  private final WebClient customerWebClient;
  private final ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory;

  /** Gets a customer by its identifier. */
  public Single<CustomerDto> findById(String customerId) {
    Mono<CustomerDto> call = customerWebClient.get()
        .uri("/customers/{id}", customerId)
        .retrieve()
        .onStatus(status -> status.value() == 404,
            response -> Mono.error(new BusinessRuleException("CUSTOMER_NOT_FOUND",
                "Customer not found: " + customerId)))
        .bodyToMono(CustomerDto.class);
    return Single.fromPublisher(circuitBreakerFactory.create("customer-service")
        .run(call, error -> error instanceof BusinessRuleException
            ? Mono.error(error)
            : Mono.error(new ServiceUnavailableException("Customer service is not available"))));
  }
}
