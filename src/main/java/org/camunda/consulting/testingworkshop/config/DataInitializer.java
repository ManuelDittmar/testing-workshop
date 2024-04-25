package org.camunda.consulting.testingworkshop.config;

import org.camunda.consulting.testingworkshop.model.MarvelCharacter;
import org.camunda.consulting.testingworkshop.service.MarvelCharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

  @Autowired
  MarvelCharacterService marvelCharacterService;

  @Bean
  public CommandLineRunner initData() {
    return args -> {
      createHero("Marc Spector", "Moon Knight", "Expert detective, martial arts, gains strength from the moon");
      createHero("Doreen Green", "Squirrel Girl", "Superhuman agility, can communicate with squirrels");
      createHero("Adam Warlock", "Adam Warlock", "Superhuman abilities, energy manipulation");
      createHero("Kamala Khan", "Ms. Marvel", "Shape-shifting, size alteration, healing factor");
      createHero("SD", "El Señor", "TAM-Magic");
    };
  }

  private void createHero(String name, String alias, String abilities) {
    MarvelCharacter hero = new MarvelCharacter();
    hero.setName(name);
    hero.setAlias(alias);
    hero.setAbilities(abilities);
    marvelCharacterService.saveCharacter(hero);
  }
}

