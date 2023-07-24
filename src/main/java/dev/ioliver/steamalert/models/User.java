package dev.ioliver.steamalert.models;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "custom_user")
public class User {
  @Id
  private Long telegramId;
  private Long chatId;
  private Long requests = 0L;
  private Boolean active = false;
  @ElementCollection(fetch = FetchType.EAGER)
  private List<String> steamIds = new ArrayList<>();
}
