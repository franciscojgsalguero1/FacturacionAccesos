package org.fbmoll.billing.dataClasses;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC)
public class Client {
    int id;
    String name;
    String address;
    int postCode;
    String town;
    String province;
    String country;
    String cif;
    String number;
    String email;
    String iban;
    double risk;
    double discount;
    String description;
}