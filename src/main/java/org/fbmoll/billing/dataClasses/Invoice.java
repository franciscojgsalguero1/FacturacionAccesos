package org.fbmoll.billing.dataClasses;

import java.sql.Date;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC)
public class Invoice {
    int id;
    int number;
    Date date;
    int clientId;
    double taxableAmount;
    double vatAmount;
    double totalAmount;
    String hash;
    String qrCode;
    boolean isPaid;
    int paymentMethod;
    Date paymentDate;
    String notes;
}