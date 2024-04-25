package org.camunda.consulting.testingworkshop.service;

import org.camunda.consulting.testingworkshop.model.MarvelCharacter;
import org.camunda.consulting.testingworkshop.model.Mission;
import org.camunda.consulting.testingworkshop.model.MissionStatus;
import org.camunda.consulting.testingworkshop.repository.MarvelCharacterRepository;
import org.camunda.consulting.testingworkshop.repository.MissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MarvelCharacterService {
  @Autowired
  private MarvelCharacterRepository marvelCharacterRepository;

  @Autowired
  private MissionRepository missionRepository;

  public MarvelCharacter saveCharacter(MarvelCharacter character) {
    return marvelCharacterRepository.save(character);
  }

  public List<MarvelCharacter> getAllCharacters() {
    return marvelCharacterRepository.findAll();
  }

  public MarvelCharacter getCharacterById(Long id) {
    return marvelCharacterRepository.findById(id).orElse(null);
  }

  public void deleteCharacter(Long id) {
    marvelCharacterRepository.deleteById(id);
  }

  public Mission addMissionToCharacter(Long characterId, Mission mission) {
    MarvelCharacter character = marvelCharacterRepository.findById(characterId)
        .orElseThrow(() -> new RuntimeException("Character not found"));
    mission.setCharacter(character);
    return missionRepository.save(mission);
  }

  public Mission updateMission(Long missionId, MissionStatus newStatus, int rating) {
    Mission mission = missionRepository.findById(missionId)
        .orElseThrow(() -> new RuntimeException("Mission not found"));
    mission.setStatus(newStatus);
    mission.setRating(rating);
    return missionRepository.save(mission);
  }


  public List<Mission> getMissionsByCharacterId(Long characterId) {
    return missionRepository.findByCharacterId(characterId);
  }

  public List<MarvelCharacter> getHeroesWithoutActiveMissions() {
    return marvelCharacterRepository.findCharactersWithoutActiveMissions();
  }
}
