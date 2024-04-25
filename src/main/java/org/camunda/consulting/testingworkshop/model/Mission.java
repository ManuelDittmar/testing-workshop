package org.camunda.consulting.testingworkshop.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import jakarta.validation.constraints.*;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class Mission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String description;
  @Enumerated(EnumType.STRING)
  private MissionStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  private MarvelCharacter character;

  @Min(0)
  @Max(5)
  private int rating = 0; // Rating from 1 to 5 stars

  public Mission(String description, MissionStatus status, MarvelCharacter hero) {
    this.description = description;
    this.status = status;
    this.character = hero;
  }
}

