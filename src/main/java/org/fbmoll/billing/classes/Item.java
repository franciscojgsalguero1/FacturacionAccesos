package org.fbmoll.billing.classes;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Item {
    int id;
    String code;
    String barCode;
    String description;
    int familyId;
    double cost;
    double margin;
    double price;
    int supplier;
    int stock;
    String notes;
}