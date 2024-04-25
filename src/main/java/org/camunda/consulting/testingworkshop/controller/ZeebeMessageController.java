package org.camunda.consulting.testingworkshop.controller;

import static org.camunda.consulting.testingworkshop.worker.ProcessSolutionConstants.*;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.PublishMessageResponse;
import java.util.Map;
import org.camunda.consulting.testingworkshop.model.MissionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ZeebeMessageController {

  @Autowired
  private ZeebeClient zeebeClient;

  @GetMapping("/complete-mission")
  public String completeMission(@RequestParam String correlationKey) {
    return publishMissionStatus(correlationKey, MissionStatus.COMPLETED);
  }

  @GetMapping("/fail-mission")
  public String failMission(@RequestParam String correlationKey) {
    return publishMissionStatus(correlationKey, MissionStatus.FAILED);
  }

  private String publishMissionStatus(String correlationKey, MissionStatus status) {
    try {
      PublishMessageResponse response = zeebeClient.newPublishMessageCommand()
          .messageName(MISSION_UPDATE_MESSAGE_NAME)
          .correlationKey(correlationKey)
          .variables(Map.of(MISSION_RESULT_VARIABLE_NAME, status))
          .send()
          .join();

      return "Mission status updated to " + status + " successfully with message key: " + response.getMessageKey();
    } catch (Exception e) {
      return "Failed to update mission status: " + e.getMessage();
    }
  }
}
