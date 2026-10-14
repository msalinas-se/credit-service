package com.nttdata.bank.credit.client;

import com.nttdata.bank.credit.client.dto.CustomerDto;
import com.nttdata.bank.credit.exception.BusinessRuleException;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/** Client of the customer microservice. */
@Component
@RequiredArgsConstructor
public class CustomerClient {
  private final WebClient customerWebClient;

  /** Gets a customer by its identifier. */
  public Single<CustomerDto> findById(String customerId) {
    return Single.fromPublisher(customerWebClient.get()
        .uri("/customers/{id}", customerId)
        .retrieve()
        .onStatus(status -> status.value() == 404,
            response -> Mono.error(new BusinessRuleException("CUSTOMER_NOT_FOUND",
                "Customer not found: " + customerId)))
        .bodyToMono(CustomerDto.class));
  }
}
