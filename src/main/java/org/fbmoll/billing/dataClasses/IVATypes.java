package org.fbmoll.billing.dataClasses;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC)
public class IVATypes {
    int id;
    double amount;
    String description;
}
