package com.nttdata.bank.credit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Credit settings loaded from the config server. */
@ConfigurationProperties("credit")
public record CreditProperties(int personalMaxCredits) {
}
