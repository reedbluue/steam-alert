package dev.ioliver.steamalert.dtos.appDetails;

import dev.ioliver.steamalert.dtos.price.PriceOverviewDto;

public record AppDetailsDto(
        String name,
        int appId,
        String shortDescription,
        String headerImageUrl,
        PriceOverviewDto priceOverview
) {
}
