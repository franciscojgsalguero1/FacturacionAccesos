package org.fbmoll.billing.dataClasses;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC)
public class Provider {
    int id;
    String name;
    String address;
    int postCode;
    String town;
    String province;
    String country;
    String cif;
    String phone;
    String email;
    String website;
    String notes;
}