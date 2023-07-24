package dev.ioliver.steamalert.services;

import org.openqa.selenium.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

import dev.ioliver.steamalert.models.User;
import dev.ioliver.steamalert.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository USER_REPOSITORY;

  public User create(@NonNull Long telegramId, @NonNull Long chatId) {
    try {
      findByTelegramId(telegramId);
      throw new RuntimeException("User already exists.");
    } catch (NotFoundException ignored) {
    }
    User user = User.builder().telegramId(telegramId).chatId(chatId).requests(0L).active(false).build();
    return USER_REPOSITORY.save(user);
  }

  public List<User> getAll() {
    return USER_REPOSITORY.findAll();
  }

  public User findByTelegramId(@NonNull Long telegramId) {
    return USER_REPOSITORY.findById(telegramId).orElseThrow(() -> new NotFoundException("User not found."));
  }

  public boolean isRegistered(@NonNull Long telegramId) {
    return USER_REPOSITORY.existsByTelegramIdAndActiveIsTrue(telegramId);
  }

  public void deleteByTelegramId(@NonNull Long telegramId) {
    User user = findByTelegramId(telegramId);
    USER_REPOSITORY.delete(user);
  }

  @Transactional
  public User addSteamId(@NonNull Long telegramId, @NotNull String steamId) {
    User user = findByTelegramId(telegramId);
    user.getSteamIds().add(steamId);
    return USER_REPOSITORY.save(user);
  }

  @Transactional
  public void addRequest(@NonNull Long telegramId) {
    User user = findByTelegramId(telegramId);
    user.setRequests(user.getRequests() + 1);
    USER_REPOSITORY.save(user);
  }

  @Transactional
  public void resetRequests(@NonNull Long telegramId) {
    User user = findByTelegramId(telegramId);
    user.setRequests(0L);
    USER_REPOSITORY.save(user);
  }

  @Transactional
  public void removeSteamId(@NonNull Long telegramId, @NotNull String steamId) {
    User user = findByTelegramId(telegramId);
    user.getSteamIds().remove(steamId);
    USER_REPOSITORY.save(user);
  }

  @Transactional
  public void setActive(@NonNull Long telegramId, boolean value) {
    User user = findByTelegramId(telegramId);
    user.setActive(value);
    USER_REPOSITORY.save(user);
  }
}
