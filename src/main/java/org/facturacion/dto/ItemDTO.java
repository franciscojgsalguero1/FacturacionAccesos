package org.facturacion.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDTO {
    final String code;
    final String barCode;
    final double cost;
    final double margin;
    final double price;
    final String supplier;
    final int stock;
}
