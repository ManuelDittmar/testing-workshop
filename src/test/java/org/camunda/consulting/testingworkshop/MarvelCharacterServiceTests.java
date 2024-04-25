package org.camunda.consulting.testingworkshop;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.camunda.consulting.testingworkshop.model.MarvelCharacter;
import org.camunda.consulting.testingworkshop.model.Mission;
import org.camunda.consulting.testingworkshop.model.MissionStatus;
import org.camunda.consulting.testingworkshop.repository.MarvelCharacterRepository;
import org.camunda.consulting.testingworkshop.repository.MissionRepository;
import org.camunda.consulting.testingworkshop.service.MarvelCharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
public class MarvelCharacterServiceTests {
  @Autowired
  private MarvelCharacterService marvelCharacterService;

  @MockBean
  private MarvelCharacterRepository marvelCharacterRepository;

  @MockBean
  private MissionRepository missionRepository;

  @Test
  public void testSaveCharacter() {
    MarvelCharacter character = new MarvelCharacter();
    character.setName("Tony Stark");
    character.setAlias("Iron Man");
    character.setAbilities("Genius level intellect, multiple powered armor suits");

    when(marvelCharacterRepository.save(any(MarvelCharacter.class))).thenReturn(character);

    MarvelCharacter savedCharacter = marvelCharacterService.saveCharacter(character);
    assertNotNull(savedCharacter);
    assertEquals("Iron Man", savedCharacter.getAlias());
  }

  @Test
  public void testGetAllCharacters() {
    MarvelCharacter character1 = new MarvelCharacter();
    character1.setName("Steve Rogers");
    character1.setAlias("Captain America");
    character1.setAbilities("Superhuman strength, agility, stamina and healing");

    MarvelCharacter character2 = new MarvelCharacter();
    character2.setName("Peter Parker");
    character2.setAlias("Spider-Man");
    character2.setAbilities("Wall-crawling, superhuman strength, speed, reflexes, durability, and agility");

    when(marvelCharacterRepository.findAll()).thenReturn(Arrays.asList(character1, character2));

    List<MarvelCharacter> characters = marvelCharacterService.getAllCharacters();
    assertEquals(2, characters.size());
    assertEquals("Spider-Man", characters.get(1).getAlias());
  }

  @Test
  public void testGetCharacterById() {
    MarvelCharacter character = new MarvelCharacter();
    character.setId(1L);
    character.setName("Bruce Banner");
    character.setAlias("Hulk");
    character.setAbilities("Immense strength, durability, endurance, and regenerative ability");

    when(marvelCharacterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));

    MarvelCharacter foundCharacter = marvelCharacterService.getCharacterById(1L);
    assertNotNull(foundCharacter);
    assertEquals("Hulk", foundCharacter.getAlias());
  }

  @Test
  public void testDeleteCharacter() {
    Long characterId = 1L;
    doNothing().when(marvelCharacterRepository).deleteById(characterId);

    marvelCharacterService.deleteCharacter(characterId);
    verify(marvelCharacterRepository, times(1)).deleteById(characterId);
  }

  @Test
  public void testAddMissionToCharacter() {
    MarvelCharacter character = new MarvelCharacter();
    character.setId(1L);
    character.setName("Tony Stark");
    character.setAlias("Iron Man");
    character.setAbilities("Genius level intellect, multiple powered armor suits");

    Mission mission = new Mission();
    mission.setDescription("Defend New York");
    mission.setStatus(MissionStatus.ASSIGNED);

    when(marvelCharacterRepository.findById(1L)).thenReturn(java.util.Optional.of(character));
    when(missionRepository.save(any(Mission.class))).thenReturn(mission);

    Mission addedMission = marvelCharacterService.addMissionToCharacter(1L, mission);
    assertNotNull(addedMission);
    assertEquals("Defend New York", addedMission.getDescription());
    assertEquals(MissionStatus.ASSIGNED, addedMission.getStatus());
    assertEquals(character.getId(), addedMission.getCharacter().getId());
  }

  @Test
  public void testUpdateMissionStatus() {
    Mission mission = new Mission();
    mission.setId(1L);
    mission.setDescription("Defend New York");
    mission.setStatus(MissionStatus.ASSIGNED);

    when(missionRepository.findById(1L)).thenReturn(java.util.Optional.of(mission));
    when(missionRepository.save(any(Mission.class))).thenReturn(mission);

    Mission updatedMission = marvelCharacterService.updateMission(1L, MissionStatus.COMPLETED, 5);
    assertNotNull(updatedMission);
    assertEquals(MissionStatus.COMPLETED, updatedMission.getStatus());
  }

  @Test
  public void testGetMissionsByCharacterId() {
    Mission mission1 = new Mission();
    mission1.setDescription("Save the universe");
    mission1.setStatus(MissionStatus.COMPLETED);

    Mission mission2 = new Mission();
    mission2.setDescription("Rescue allies");
    mission2.setStatus(MissionStatus.COMPLETED);

    when(missionRepository.findByCharacterId(1L)).thenReturn(Arrays.asList(mission1, mission2));

    List<Mission> missions = marvelCharacterService.getMissionsByCharacterId(1L);
    assertEquals(2, missions.size());
    assertEquals("Save the universe", missions.get(0).getDescription());
  }

}
