package dev.ioliver.steamalert.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Map;

import dev.ioliver.steamalert.dtos.price.PriceOverviewDto;

@Mapper(componentModel = "spring")
public interface PriceOverviewMapper {
  PriceOverviewMapper INSTANCE = Mappers.getMapper(PriceOverviewMapper.class);
  @Mapping(source = "currency", target = "currency")
  @Mapping(source = "initial", target = "initialPrice")
  @Mapping(source = "final", target = "finalPrice")
  @Mapping(source = "discount_percent", target = "discountPercent")
  @Mapping(source = "initial_formatted", target = "initialFormatted")
  @Mapping(source = "final_formatted", target = "finalFormatted")
  PriceOverviewDto mapToDto(Map<String, Object> map);

  default String mapToString(Object o) {
    return o != null ? o.toString() : null;
  }
}
