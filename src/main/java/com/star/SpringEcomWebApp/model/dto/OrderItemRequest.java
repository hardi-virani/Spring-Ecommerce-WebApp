package com.star.SpringEcomWebApp.model.dto;

public record OrderItemRequest(
        int productId,
        int quantity

) {
}
