package dev.ioliver.steamalert.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;

import dev.ioliver.steamalert.bots.SteamAlertBot;
import dev.ioliver.steamalert.dtos.appDetails.AppDetailsDto;
import dev.ioliver.steamalert.models.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class BotService {
  private final UserService USER_SERVICE;
  private final SteamService STEAM_SERVICE;

  @Value("${bot.token}")
  private String TOKEN;
  @Value("${bot.name}")
  private String BOT_NAME;
  @Value("${bot.creator_id}")
  private Long BOT_CREATOR_ID;

  private SteamAlertBot bot;

  @PostConstruct
  public void init() throws TelegramApiException {
    if (TOKEN == null || TOKEN.isBlank() || BOT_CREATOR_ID == null || BOT_CREATOR_ID <= 0) {
      throw new RuntimeException("Bot token or bot creator id is invalid");
    }
    TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
    bot = new SteamAlertBot(TOKEN, BOT_NAME, BOT_CREATOR_ID, USER_SERVICE, STEAM_SERVICE);
    telegramBotsApi.registerBot(bot);
  }

  @Scheduled(cron = "0 0 15 * * *")
  private void alertAllUsers() {
    List<User> all = USER_SERVICE.getAll();
    for (var user : all) {
      for (var steamId : user.getSteamIds()) {
        List<AppDetailsDto> appDetails = STEAM_SERVICE.getAllSaleAppDetails(steamId);
        List<SendPhoto> list = appDetails.stream().map(detail -> SendPhoto.builder().chatId(user.getChatId())
                .photo(STEAM_SERVICE.getImage(detail.headerImageUrl())).caption(STEAM_SERVICE.getDetailMessage(detail))
                .parseMode(ParseMode.HTML).build()).toList();
        list.forEach(item -> {
          try {
            bot.execute(item);
          } catch (TelegramApiException ignored) {
          }
        });
      }
    }
  }

  @Scheduled(fixedRate = 1_800_000L)
  private void resetRequests() {
    List<User> all = USER_SERVICE.getAll();
    for (var user : all) {
      USER_SERVICE.resetRequests(user.getTelegramId());
    }
  }
}
