package org.camunda.consulting.testingworkshop.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class MarvelCharacter {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  private String name;
  private String alias;
  private String abilities;

  public MarvelCharacter(String name, String alias, String abilities) {
    this.name = name;
    this.alias = alias;
    this.abilities = abilities;
  }
}

