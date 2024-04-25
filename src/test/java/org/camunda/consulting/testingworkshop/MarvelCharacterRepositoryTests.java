package org.camunda.consulting.testingworkshop;

import org.camunda.consulting.testingworkshop.model.MarvelCharacter;
import org.camunda.consulting.testingworkshop.model.Mission;
import org.camunda.consulting.testingworkshop.model.MissionStatus;
import org.camunda.consulting.testingworkshop.repository.MarvelCharacterRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class MarvelCharacterRepositoryTests {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private MarvelCharacterRepository marvelCharacterRepository;

  @Test
  public void testFindHeroesWithoutActiveMissions() {
    MarvelCharacter activeHero = new MarvelCharacter("Tony Stark", "Iron Man", "Genius level intellect");
    MarvelCharacter completedHero = new MarvelCharacter("Steve Rogers", "Captain America", "Super strength");
    MarvelCharacter idleHero = new MarvelCharacter("Bruce Banner", "Hulk", "Super strength");

    entityManager.persist(activeHero);
    entityManager.persist(completedHero);
    entityManager.persist(idleHero);

    Mission activeMission = new Mission("Save the world", MissionStatus.ASSIGNED, activeHero);
    Mission completedMission = new Mission("Save the city", MissionStatus.COMPLETED, completedHero);

    entityManager.persist(activeMission);
    entityManager.persist(completedMission);
    entityManager.flush();

    List<MarvelCharacter> result = marvelCharacterRepository.findCharactersWithoutActiveMissions();

    assertEquals(2, result.size(), "Should return two heroes");
    assertTrue(result.contains(completedHero), "Should include hero with completed missions");
    assertTrue(result.contains(idleHero), "Should include hero without any missions");
  }
}
