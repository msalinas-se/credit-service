package com.nttdata.bank.credit.exception;

/** Error body returned by the API. */
public record ErrorResponse(String code, String message) {
}
