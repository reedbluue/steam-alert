package dev.ioliver.steamalert.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.ioliver.steamalert.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
  boolean existsByTelegramIdAndActiveIsTrue(Long telegramId);
}
