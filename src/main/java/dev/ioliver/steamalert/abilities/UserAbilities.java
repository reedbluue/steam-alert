package dev.ioliver.steamalert.abilities;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.abilitybots.api.objects.ReplyFlow;
import org.telegram.abilitybots.api.util.AbilityExtension;
import org.telegram.abilitybots.api.util.AbilityUtils;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.ioliver.steamalert.dtos.appDetails.AppDetailsDto;
import dev.ioliver.steamalert.dtos.steamProfileData.SteamProfileDataDto;
import dev.ioliver.steamalert.services.SteamService;
import dev.ioliver.steamalert.services.UserService;
import dev.ioliver.steamalert.texts.Texts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class UserAbilities implements AbilityExtension {
  private final AbilityBot BOT;
  private final UserService USER_SERVICE;
  private final SteamService STEAM_SERVICE;

  private void asyncDeleteMessage(Long chatId, Integer messageId) {
    asyncDeleteMessage(chatId, messageId, 10);
  }

  private void asyncDeleteMessage(Long chatId, Integer messageId, int delay) {
    Executors.newSingleThreadScheduledExecutor().schedule(() -> {
      DeleteMessage deleteMessage = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();
      try {
        BOT.execute(deleteMessage);
      } catch (TelegramApiException ignored) {
      }
    }, delay, TimeUnit.SECONDS);
  }


  private void deleteMessage(Long chatId, Integer messageId) {
    DeleteMessage deleteMessage = DeleteMessage.builder().chatId(chatId).messageId(messageId).build();
    try {
      BOT.execute(deleteMessage);
    } catch (TelegramApiException ignored) {
    }
  }

  @SuppressWarnings("unchecked")
  public Ability showMenu() {
    return Ability.builder().name("menu").info("Show the menu").locality(Locality.USER).privacy(Privacy.PUBLIC).action(ctx -> {
      List<InlineKeyboardButton> buttonList0 = List.of(InlineKeyboardButton.builder().text(Texts.MENU_MONITORED_ACCOUNTS).callbackData("monitored-list").build(), InlineKeyboardButton.builder().text(Texts.MENU_UNSUBSCRIBE).callbackData("unsub-question").build());
      List<InlineKeyboardButton> buttonList1 = List.of(InlineKeyboardButton.builder().text(Texts.MENU_ABOUT).url("https://github.com/reedbluue/steam-alert").build(), InlineKeyboardButton.builder().text(Texts.CLOSE_BUTTON).callbackData("none").build());
      InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder().keyboard(List.of(buttonList0, buttonList1)).build();

      SendMessage message = SendMessage.builder().chatId(ctx.update().getMessage().getChatId()).text(Texts.SELECT_AN_OPTION).replyMarkup(keyboardMarkup).build();
      Optional<Message> sendMessage = BOT.silent().execute(message);
      sendMessage.ifPresent(value -> asyncDeleteMessage(value.getChatId(), value.getMessageId()));
      deleteMessage(ctx.update().getMessage().getChatId(), ctx.update().getMessage().getMessageId());
    }).flag(isSubscribed(), isSuspended().negate()).build();
  }

  public Reply onInvalidCommand() {
    BiConsumer<BaseAbilityBot, Update> action = (ability, upd) -> {
      SendMessage message = SendMessage.builder().chatId(upd.getMessage().getChatId()).text(Texts.INVALID_COMMAND).build();
      Optional<Message> execute = BOT.silent().execute(message);
      execute.ifPresent(value -> asyncDeleteMessage(value.getChatId(), value.getMessageId()));
      deleteMessage(upd.getMessage().getChatId(), upd.getMessage().getMessageId());
    };
    return Reply.of(action, isSuspended().negate(), isSubscribed(), isValidCommand().negate());
  }

  public Reply onUnsubscribedUser() {
    BiConsumer<BaseAbilityBot, Update> action = (ability, upd) -> {
      List<InlineKeyboardButton> buttonList = List.of(InlineKeyboardButton.builder().text(Texts.YES).callbackData("sub").build(), InlineKeyboardButton.builder().text(Texts.NO).callbackData("none").build());
      InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder().keyboardRow(buttonList).build();

      SendMessage message = SendMessage.builder().chatId(upd.getMessage().getChatId()).text(Texts.SUBSCRIBE_REQUEST).replyMarkup(keyboardMarkup).build();
      deleteMessage(upd.getMessage().getChatId(), upd.getMessage().getMessageId());
      Optional<Message> sendMessage = ability.silent().execute(message);
      sendMessage.ifPresent(value -> asyncDeleteMessage(value.getChatId(), value.getMessageId()));
    };
    return Reply.of(action, isSuspended().negate(), isSubscribed().negate());
  }

  public Reply onQuery() {
    BiConsumer<BaseAbilityBot, Update> action = (ability, upd) -> {
      switch (upd.getCallbackQuery().getData()) {
        case "sub" -> {
          AnswerCallbackQuery build;
          try {
            USER_SERVICE.setActive(AbilityUtils.getUser(upd).getId(), true);
            SendMessage message = SendMessage.builder().chatId(AbilityUtils.getChatId(upd)).text(Texts.FIRST_MESSAGE).build();
            build = AnswerCallbackQuery.builder().callbackQueryId(upd.getCallbackQuery().getId()).text(Texts.SUBSCRIBE_CONFIRM).build();
            ability.silent().execute(message);
          } catch (Exception e) {
            build = AnswerCallbackQuery.builder().callbackQueryId(upd.getCallbackQuery().getId()).text(Texts.ALREADY_SUBSCRIBED).build();
          }
          ability.silent().execute(build);
          deleteMessage(upd.getCallbackQuery().getMessage().getChatId(), upd.getCallbackQuery().getMessage().getMessageId());
        }
        case "unsub-question" -> {
          List<InlineKeyboardButton> buttonList = List.of(InlineKeyboardButton.builder().text(Texts.YES).callbackData("unsub").build(), InlineKeyboardButton.builder().text(Texts.NO).callbackData("none").build());
          InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder().keyboardRow(buttonList).build();

          SendMessage message = SendMessage.builder().chatId(upd.getCallbackQuery().getMessage().getChatId()).text(Texts.UNSUBSCRIBE_REQUEST).replyMarkup(keyboardMarkup).build();
          deleteMessage(upd.getCallbackQuery().getMessage().getChatId(), upd.getCallbackQuery().getMessage().getMessageId());
          Optional<Message> sendMessage = ability.silent().execute(message);
          sendMessage.ifPresent(value -> asyncDeleteMessage(upd.getCallbackQuery().getMessage().getChatId(), upd.getCallbackQuery().getMessage().getMessageId()));
        }
        case "unsub" -> {
          AnswerCallbackQuery build;
          try {
            USER_SERVICE.setActive(AbilityUtils.getUser(upd).getId(), false);
            build = AnswerCallbackQuery.builder().callbackQueryId(upd.getCallbackQuery().getId()).text(Texts.UNSUBSCRIBE_CONFIRM).build();
          } catch (Exception e) {
            build = AnswerCallbackQuery.builder().callbackQueryId(upd.getCallbackQuery().getId()).text(Texts.ALREADY_UNSUBSCRIBED).build();
          }
          ability.silent().execute(build);
          deleteMessage(upd.getCallbackQuery().getMessage().getChatId(), upd.getCallbackQuery().getMessage().getMessageId());
        }
        case "monitored-list" -> {
          deleteMessage(upd.getCallbackQuery().getMessage().getChatId(), upd.getCallbackQuery().getMessage().getMessageId());
          SendChatAction action1 = SendChatAction.builder().chatId(upd.getCallbackQuery().getMessage().getChatId()).action(ActionType.TYPING.toString()).build();
          BOT.silent().execute(action1);

          dev.ioliver.steamalert.models.User user = USER_SERVICE.findByTelegramId(upd.getCallbackQuery().getFrom().getId());

          if (user.getSteamIds().isEmpty()) {
            List<InlineKeyboardButton> buttonList = List.of(InlineKeyboardButton.builder().text(Texts.ADD).callbackData("add-account").build(), InlineKeyboardButton.builder().text(Texts.CLOSE_BUTTON).callbackData("none").build());
            InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder().keyboardRow(buttonList).build();

            SendMessage message = SendMessage.builder().chatId(upd.getCallbackQuery().getMessage().getChatId()).text(Texts.DONT_HAVE_ANY_ACCOUNT).replyMarkup(keyboardMarkup).build();
            Optional<Message> sendMessage = ability.silent().execute(message);
            sendMessage.ifPresent(value -> asyncDeleteMessage(sendMessage.get().getChatId(), sendMessage.get().getMessageId()));
          } else {
            List<List<InlineKeyboardButton>> keyboardButtons = new java.util.ArrayList<>(user.getSteamIds().stream().map(id -> {
              try {
                SteamProfileDataDto profileData = STEAM_SERVICE.getSteamProfileData(id);
                return List.of(InlineKeyboardButton.builder().text(profileData.personaname()).callbackData("check-" + id).build(), InlineKeyboardButton.builder().text(Texts.DELETE_BUTTON).callbackData("delete-" + id).build());
              } catch (Exception e) {
                return List.of(InlineKeyboardButton.builder().text(id).callbackData("check-" + id).build());
              }
            }).toList());

            List<InlineKeyboardButton> options = List.of(InlineKeyboardButton.builder().text(Texts.ADD).callbackData("add-account").build(), InlineKeyboardButton.builder().text(Texts.CLOSE_BUTTON).callbackData("none").build());

            keyboardButtons.add(options);

            InlineKeyboardMarkup keyboardMarkup = InlineKeyboardMarkup.builder().keyboard(keyboardButtons).build();
            SendMessage message = SendMessage.builder().chatId(upd.getCallbackQuery().getMessage().getChatId()).text(Texts.SELECT_AN_ACCOUNT).replyMarkup(keyboardMarkup).build();
            Optional<Message> sendMessage = ability.silent().execute(message);
            sendMessage.ifPresent(value -> asyncDeleteMessage(sendMessage.get().getChatId(), sendMessage.get().getMessageId()));
          }
        }
        default -> {
          deleteMessage(upd.getCallbackQuery().getMessage().getChatId(), upd.getCallbackQuery().getMessage().getMessageId());
          Matcher checkMatcher = Pattern.compile("check-(.*)").matcher(upd.getCallbackQuery().getData());
          Matcher deleteMatcher = Pattern.compile("delete-(.*)").matcher(upd.getCallbackQuery().getData());
          if (checkMatcher.find()) {
            List<AppDetailsDto> appDetails = STEAM_SERVICE.getAllSaleAppDetails(checkMatcher.group(1));

            if (appDetails.isEmpty()) {
              AnswerCallbackQuery build = AnswerCallbackQuery.builder().callbackQueryId(upd.getCallbackQuery().getId()).text(Texts.WITHOUT_SALES).build();
              BOT.silent().execute(build);
            } else {
              List<SendPhoto> list = appDetails.stream().map(detail -> {
                return SendPhoto.builder().chatId(upd.getCallbackQuery().getMessage().getChatId()).photo(STEAM_SERVICE.getImage(detail.headerImageUrl())).caption(STEAM_SERVICE.getDetailMessage(detail)).parseMode(ParseMode.HTML).build();
              }).toList();
              for (var item : list) {
                try {
                  BOT.execute(item);
                } catch (TelegramApiException e) {
                  log.error(e.getMessage());
                }
              }
            }
          }
          if (deleteMatcher.find()) {
            try {
              USER_SERVICE.removeSteamId(AbilityUtils.getUser(upd).getId(), deleteMatcher.group(1));
              AnswerCallbackQuery build = AnswerCallbackQuery.builder().callbackQueryId(upd.getCallbackQuery().getId()).text(Texts.DELETE_ACCOUNT_CONFIRM).build();
              BOT.silent().execute(build);
            } catch (Exception ignored) {
              AnswerCallbackQuery build = AnswerCallbackQuery.builder().callbackQueryId(upd.getCallbackQuery().getId()).text(Texts.DELETE_ACCOUNT_FAULT).build();
              BOT.silent().execute(build);
            }
          }
        }
      }
    };
    return Reply.of(action, Flag.CALLBACK_QUERY);
  }

  public ReplyFlow flow() {
    AtomicReference<Optional<Message>> firstMessage = new AtomicReference<>();
    AtomicReference<CallbackQuery> callbackQuery = new AtomicReference<>();

    return ReplyFlow.builder(BOT.db()).action((ability, upd) -> {
      callbackQuery.set(upd.getCallbackQuery());
      firstMessage.set(BOT.silent().send(Texts.INSERT_ACCOUNT_ID, AbilityUtils.getChatId(upd)));
      deleteMessage(upd.getCallbackQuery().getMessage().getChatId(), upd.getCallbackQuery().getMessage().getMessageId());
    }).onlyIf(addFlow()).next(Reply.of((ability, upd) -> {
      AnswerCallbackQuery build = AnswerCallbackQuery.builder().callbackQueryId(callbackQuery.get().getId()).text(Texts.ACCOUNT_DONT_EXIST).build();
      try {
        ability.execute(build);
      } catch (TelegramApiException e) {
        SendMessage message = SendMessage.builder().chatId(upd.getMessage().getChatId()).text(Texts.TIMEOUT_ERROR).build();
        Optional<Message> sendMessage = ability.silent().execute(message);
        sendMessage.ifPresent(value -> asyncDeleteMessage(sendMessage.get().getChatId(), sendMessage.get().getMessageId()));
      }
      firstMessage.get().ifPresent(value -> deleteMessage(firstMessage.get().get().getChatId(), firstMessage.get().get().getMessageId()));
    }, isValidCommand().negate(), validSteamId().negate())).next(Reply.of((ability, upd) -> {
      try {
        try {
          dev.ioliver.steamalert.models.User user = USER_SERVICE.findByTelegramId(AbilityUtils.getUser(upd).getId());
          USER_SERVICE.addSteamId(user.getTelegramId(), upd.getMessage().getText());
          AnswerCallbackQuery build = AnswerCallbackQuery.builder().callbackQueryId(callbackQuery.get().getId()).text(Texts.ACCOUNT_ADDED).build();
          ability.silent().execute(build);
        } catch (Exception e) {
          AnswerCallbackQuery build = AnswerCallbackQuery.builder().callbackQueryId(callbackQuery.get().getId()).text(Texts.ADD_ACCOUNT_FAULT).build();
          ability.silent().execute(build);
        }
      } catch (Exception e) {
        SendMessage message = SendMessage.builder().chatId(upd.getMessage().getChatId()).text(Texts.TIMEOUT_ERROR).build();
        Optional<Message> sendMessage = ability.silent().execute(message);
        sendMessage.ifPresent(value -> asyncDeleteMessage(sendMessage.get().getChatId(), sendMessage.get().getMessageId()));
      }
      firstMessage.get().ifPresent(value -> deleteMessage(firstMessage.get().get().getChatId(), firstMessage.get().get().getMessageId()));
    }, isValidCommand().negate(), validSteamId())).build();
  }

  public Reply ifSuspended() {
    BiConsumer<BaseAbilityBot, Update> action = (ability, upd) -> {
      if (USER_SERVICE.findByTelegramId(AbilityUtils.getUser(upd).getId()).getRequests() == 50) {
        SendMessage message = SendMessage.builder().chatId(upd.getMessage().getChatId()).text(Texts.TOO_MANY_REQUESTS).build();
        ability.silent().execute(message);
      }
    };
    return Reply.of(action, isSuspended());
  }

  private Predicate<Update> isSuspended() {
    return upd -> {
      try {
        return USER_SERVICE.findByTelegramId(AbilityUtils.getUser(upd).getId()).getRequests() >= 50L;
      } catch (Exception ignored) {
        return false;
      }
    };
  }

  private Predicate<Update> addFlow() {
    return upd -> upd.hasCallbackQuery() && upd.getCallbackQuery().getData().equals("add-account");
  }

  private Predicate<Update> validSteamId() {
    return upd -> {
      String id = upd.getMessage().getText();
      try {
        STEAM_SERVICE.getSteamProfileData(id);
        return true;
      } catch (Exception e) {
        return false;
      }
    };
  }

  private Predicate<Update> isSubscribed() {
    return upd -> USER_SERVICE.isRegisteredAndActive(AbilityUtils.getUser(upd).getId());
  }

  private Predicate<Update> isValidCommand() {
    return upd -> AbilityUtils.isValidCommand(upd.getMessage().getText()) && BOT.abilities().containsKey(upd.getMessage().getText().trim().split(" ")[0].replace("/", ""));
  }
}