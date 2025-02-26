package org.fbmoll.billing.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.sql.Date;

@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoicePaymentDTO {
    final int number;
    final double taxableAmount;
    final double vatAmount;
    final double totalAmount;
    final boolean isPaid;
    final boolean corrected;
    final String paymentMethod;
    final Date paymentDate;
}