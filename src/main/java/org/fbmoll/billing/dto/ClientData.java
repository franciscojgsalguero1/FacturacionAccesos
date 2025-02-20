package org.fbmoll.billing.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientData {
    String name;
    String cif;
    String number;
    String email;
    String iban;
}
