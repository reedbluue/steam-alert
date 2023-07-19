package dev.ioliver.steamalert.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.ioliver.steamalert.dtos.appDetails.AppDetailsDto;
import dev.ioliver.steamalert.dtos.price.PriceOverviewDto;
import dev.ioliver.steamalert.dtos.wishlistItem.WishlistItemResponseDto;
import dev.ioliver.steamalert.mappers.AppDetailsMapper;
import dev.ioliver.steamalert.mappers.PriceOverviewMapper;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SteamService {
  private final ObjectMapper objectMapper;
  private final RestTemplate client = new RestTemplate();
  private final PriceOverviewMapper priceOverviewMapper = PriceOverviewMapper.INSTANCE;
  private final AppDetailsMapper appDetailsMapper = AppDetailsMapper.INSTANCE;
  private WebDriver driver;

  @PostConstruct
  private void init() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless");

    WebDriverManager.chromedriver().setup();
    driver = new ChromeDriver(options);

    Runtime.getRuntime().addShutdownHook(new Thread(() -> driver.quit()));
  }

  private AppDetailsDto getDetails(Integer appId) {
    UriComponents uriComponents = UriComponentsBuilder.fromUriString("https://store.steampowered.com/api/appdetails").queryParam("filters", "basic,price_overview").queryParam("appids", appId).build();

    String json = client.getForObject(uriComponents.toString(), String.class);

    Map<Integer, Object> resMap;

    try {
      resMap = objectMapper.readValue(json, new TypeReference<>() {
      });
    } catch (Exception e) {
      log.error("Returning a default value because an exception has occurred: " + e.getMessage());
      return null;
    }

    for (Map.Entry<Integer, Object> entry : resMap.entrySet()) {
      Map<String, Object> value = (Map<String, Object>) entry.getValue();
      if (value.containsKey("data")) {
        return appDetailsMapper.mapToDto((Map<String, Object>) value.get("data"));
      }
    }
    log.error("Returning a default value because the game data is empty: " + appId);
    return null;
  }

  private Map<Integer, PriceOverviewDto> getPrices(List<Integer> steamGameIds) {
    UriComponents uriComponents = UriComponentsBuilder
            .fromUriString("https://store.steampowered.com/api/appdetails")
            .queryParam("filters", "price_overview")
            .queryParam("appids", String.join(",", steamGameIds.stream().map(Object::toString)
                    .toList())).build();

    String json = client.getForObject(uriComponents.toString(), String.class);

    Map<Integer, Object> resMap;

    try {
      resMap = objectMapper.readValue(json, new TypeReference<>() {
      });
    } catch (Exception e) {
      log.error("Returning a default value because an exception has occurred: " + e.getMessage());
      return new HashMap<>();
    }

    HashMap<Integer, PriceOverviewDto> overviewDtoHashMap = new HashMap<>();

    for (Map.Entry<Integer, Object> entry : resMap.entrySet()) {
      Map<String, Object> value = (Map<String, Object>) entry.getValue();
      if (value.containsKey("data")) {
        Object data = value.get("data");
        if (data instanceof List<?>) continue;
        Map<String, Object> dataMap = (Map<String, Object>) data;
        if (dataMap.containsKey("price_overview"))
          overviewDtoHashMap.put(entry.getKey(), priceOverviewMapper.mapToDto((Map<String, Object>) dataMap.get("price_overview")));
      }
    }
    return overviewDtoHashMap;
  }

  private List<Integer> getWishlistAppIds(String steamId) {
    driver.get("https://store.steampowered.com/wishlist/profiles/" + steamId + "/");
    JavascriptExecutor js = (JavascriptExecutor) driver;
    List<Object> wishlistData = (List<Object>) js.executeScript("return g_rgWishlistData;");
    List<WishlistItemResponseDto> wishlistItems = objectMapper.convertValue(wishlistData, new TypeReference<>() {
    });
    return wishlistItems.stream().map(WishlistItemResponseDto::appid).toList();
  }

  private List<Integer> getOnlyOnSaleAppIds(List<Integer> appIds) {
    Map<Integer, PriceOverviewDto> prices = getPrices(appIds);
    return prices.entrySet().stream().filter(e -> e.getValue().discountPercent() != 0).map(e -> e.getKey()).collect(Collectors.toList());
  }

  public List<AppDetailsDto> getAllSaleAppDetails(String steamId) {
    List<Integer> appIds = getOnlyOnSaleAppIds(getWishlistAppIds(steamId));
    return appIds.stream().map(this::getDetails).collect(Collectors.toList());
  }

  public List<AppDetailsDto> getAllAppDetails(String steamId) {
    return getWishlistAppIds(steamId).stream().map(this::getDetails).collect(Collectors.toList());
  }
}
