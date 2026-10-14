package com.nttdata.bank.credit.client.dto;

import com.nttdata.bank.credit.model.CustomerType;

/** Customer data obtained from the customer microservice. */
public record CustomerDto(String id, CustomerType type, String documentNumber, String name) {
}
