package org.fbmoll.billing.data_classes;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PUBLIC)
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