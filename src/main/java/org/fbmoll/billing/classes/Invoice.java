package org.fbmoll.billing.classes;

import java.util.Date;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Invoice {
    int id;
    int number;
    Date date;
    int clientId;
    int taxableAmount;
    int vatAmount;
    int totalAmount;
    String hash;
    String qrCode;
    boolean isPaid;
    int paymentMethod;
    Date paymentDate;
    String notes;
}