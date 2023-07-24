package dev.ioliver.steamalert.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Map;

import dev.ioliver.steamalert.dtos.appDetails.AppDetailsDto;
import dev.ioliver.steamalert.dtos.price.PriceOverviewDto;

@Mapper(componentModel = "spring")
public interface AppDetailsMapper {
  AppDetailsMapper INSTANCE = Mappers.getMapper(AppDetailsMapper.class);
  PriceOverviewMapper PRICE_OVERVIEW_MAPPER = Mappers.getMapper(PriceOverviewMapper.class);

  @Mapping(source = "steam_appid", target = "appId")
  @Mapping(source = "short_description", target = "shortDescription")
  @Mapping(source = "header_image", target = "headerImageUrl")
  @Mapping(source = "price_overview", target = "priceOverview")
  AppDetailsDto mapToDto(Map<String, Object> map);

  default String mapToString(Object o) {
    return o != null ? o.toString() : null;
  }

  default PriceOverviewDto mapToPriceOverviewDto(Object value) {
    return PRICE_OVERVIEW_MAPPER.mapToDto((Map<String, Object>) value);
  }
}
