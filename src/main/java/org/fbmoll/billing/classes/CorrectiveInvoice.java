package org.fbmoll.billing.classes;

import java.sql.Date;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CorrectiveInvoice {
    int id;
    int number;
    Date date;
    int clientId;
    double taxableAmount;
    double vatAmount;
    double totalAmount;
    String hash;
    String qrCode;
    String notes;
}