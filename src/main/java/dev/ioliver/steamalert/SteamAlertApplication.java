package dev.ioliver.steamalert;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class SteamAlertApplication {
  public static void main(String[] args) {
    new SpringApplicationBuilder(SteamAlertApplication.class).web(WebApplicationType.NONE).run(args);
  }
}
