package com.meepleleague.leagueapi.player;

import java.time.OffsetDateTime;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "player", schema = "league")
public class Player {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @NonNull
  @Column(name = "username", length = 50)
  private String username;

  @Setter
  @NonNull
  @Column(name = "email", length = 255)
  private String email;

  @Generated(event = EventType.INSERT) // Posé par le DEFAULT now() de la colonne
  @Column(name = "created_at")
  private OffsetDateTime createdAt;

  @Generated(event = { EventType.INSERT, EventType.UPDATE }) // Valeur générée par le trigger trg_player_updated_at
  @Column(name = "updated_at")
  private OffsetDateTime updatedAt;
}
