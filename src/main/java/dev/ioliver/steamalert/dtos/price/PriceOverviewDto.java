package dev.ioliver.steamalert.dtos.price;

public record PriceOverviewDto(
        String currency,
        int initialPrice,
        int finalPrice,
        int discountPercent,
        String initialFormatted,
        String finalFormatted
) {
}