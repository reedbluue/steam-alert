package dev.ioliver.steamalert.bots;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.toggle.BareboneToggle;
import org.telegram.abilitybots.api.util.AbilityUtils;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.List;

import dev.ioliver.steamalert.abilities.UserAbilities;
import dev.ioliver.steamalert.services.SteamService;
import dev.ioliver.steamalert.services.UserService;
import dev.ioliver.steamalert.texts.Texts;

public class SteamAlertBot extends AbilityBot {
  private final Long creatorId;
  private final UserService USER_SERVICE;

  public SteamAlertBot(String token, String botName, Long creatorId, UserService USER_SERVICE, SteamService STEAM_SERVICE) {
    super(token, botName, new BareboneToggle());
    addExtension(new UserAbilities(this, USER_SERVICE, STEAM_SERVICE));
    this.creatorId = creatorId;
    this.USER_SERVICE = USER_SERVICE;
  }

  @Override
  public long creatorId() {
    return this.creatorId;
  }

  @Override
  public void onRegister() {
    List<BotCommand> commands = List.of(BotCommand.builder().command("menu").description(Texts.MENU_DESCRIPTION).build());
    SetMyCommands setMyCommands = SetMyCommands.builder().commands(commands).build();
    silent().execute(setMyCommands);
    super.onRegister();
  }

  @Override
  public void onUpdateReceived(Update update) {
    try {
      registerUser(AbilityUtils.getUser(update).getId(), AbilityUtils.getChatId(update));
      USER_SERVICE.addRequest(AbilityUtils.getUser(update).getId());
    } catch (Exception ignored) {
    }
    super.onUpdateReceived(update);
  }

  private void registerUser(Long telegramId, Long chatId) {
    try {
      USER_SERVICE.create(telegramId, chatId);
    } catch (Exception ignored) {
    }
  }
}
