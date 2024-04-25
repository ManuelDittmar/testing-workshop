package org.camunda.consulting.testingworkshop.worker;

import static org.camunda.consulting.testingworkshop.worker.ProcessSolutionConstants.*;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import java.util.List;
import java.util.Map;
import org.camunda.consulting.testingworkshop.model.MarvelCharacter;
import org.camunda.consulting.testingworkshop.model.Mission;
import org.camunda.consulting.testingworkshop.model.MissionStatus;
import org.camunda.consulting.testingworkshop.service.MarvelCharacterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HeroAssignmentWorker {

  private static final Logger logger = LoggerFactory.getLogger(HeroAssignmentWorker.class);

  @Autowired
  MarvelCharacterService marvelCharacterService;

  @JobWorker(type = GET_HEROS_JOB)
  public Map<String, List<MarvelCharacter>> getAvailableHeros(JobClient client, ActivatedJob job) {
    logger.info("Fetching available heroes for job: {}", job.getKey());
    List<MarvelCharacter> availableHeros = marvelCharacterService.getHeroesWithoutActiveMissions();

    if(availableHeros.isEmpty()) {
      logger.warn("No available heroes found; throwing error for job: {}", job.getKey());
      client.newThrowErrorCommand(job)
          .errorCode(NO_HERO_ERROR_CODE)
          .send()
          .join();
    } else {
      logger.info("Found {} available heroes", availableHeros.size());
    }

    return Map.of("availableHeros",availableHeros);
  }

  @JobWorker(type = ASSIGN_HERO_JOB)
  public Map<String, Long> assignHeroToMission(ActivatedJob job) {
    logger.info("Assigning hero to mission for job: {}", job.getKey());
    String request = (String) job.getVariable(REQUEST_VARIABLE_NAME);
    long selectedHero = Long.parseLong(String.valueOf(job.getVariable(SELECTED_HERO_VARIABLE_NAME)));

    Mission mission = new Mission();
    mission.setDescription(request);
    mission.setStatus(MissionStatus.ASSIGNED);

    Mission savedMission = marvelCharacterService.addMissionToCharacter(selectedHero, mission);
    logger.info("Assigned mission (ID: {}) to hero (ID: {})", savedMission.getId(), selectedHero);
    return Map.of(MISSION_ID_VARIABLE_NAME,savedMission.getId());
  }

  @JobWorker(type = UPDATE_MISSION_JOB)
  public void updateMission(ActivatedJob job) {
    long missionId = Long.parseLong(String.valueOf(job.getVariable(MISSION_ID_VARIABLE_NAME)));
    MissionStatus status = MissionStatus.valueOf((String) job.getVariable(MISSION_RESULT_VARIABLE_NAME));
    int rating = (int) job.getVariable(RATING_VARIABLE_NAME);
    logger.info("Updating mission status for mission ID: {} to status: {}", missionId, status);
    marvelCharacterService.updateMission(missionId, status, rating);
  }
}
