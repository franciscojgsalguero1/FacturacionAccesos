package org.fbmoll.billing.data_classes;

import java.sql.Date;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC)
public class Invoice {
    int id;
    int number;
    Date date;
    int clientId;
    int workerId;
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