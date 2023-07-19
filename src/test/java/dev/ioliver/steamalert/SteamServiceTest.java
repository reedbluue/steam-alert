package dev.ioliver.steamalert;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import dev.ioliver.steamalert.services.SteamService;

@SpringBootTest
public class SteamServiceTest {
  @Autowired
  private SteamService service;

  @Test
  public void getPrices() {
  }
}
