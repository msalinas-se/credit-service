package com.nttdata.bank.credit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/** Entry point of the credit products microservice. */
@SpringBootApplication
@ConfigurationPropertiesScan
public class CreditServiceApplication {

  /** Starts the credit products microservice. */
  public static void main(String[] args) {
    SpringApplication.run(CreditServiceApplication.class, args);
  }
}
