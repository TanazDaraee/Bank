package com.tanaz;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionDetails(String accountNumber, String counterAccountNumber, LocalDateTime date, BigDecimal amount, TransactionType transactionType) {
}
