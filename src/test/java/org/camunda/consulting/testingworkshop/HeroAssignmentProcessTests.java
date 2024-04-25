package org.camunda.consulting.testingworkshop;


import static org.camunda.consulting.testingworkshop.worker.ProcessSolutionConstants.*;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivateJobsResponse;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.process.test.assertions.BpmnAssert;
import io.camunda.zeebe.process.test.extension.ZeebeProcessTest;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import lombok.SneakyThrows;
import org.camunda.community.process_test_coverage.junit5.platform8.ProcessEngineCoverageExtension;
import org.camunda.consulting.testingworkshop.model.HeroRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ZeebeProcessTest
@ExtendWith(ProcessEngineCoverageExtension.class)
public class HeroAssignmentProcessTests {
  public static final String ACTIVITY_GET_AVAILABLE_SUPER_HEROS = "Activity_GetAvailableSuperHeros";
  public static final String ACTIVITY_ASSIGN_HERO_TO_MISSION = "Activity_AssignHeroToMission";
  public static final String ACTIVITY_UPDATE_MISSION = "Activity_UpdateMission";
  private static final String HERO_PROCESS = "HeroAssignmentProcess";
  private static final String RESOURCE_FOLDER = "camunda";
  public static final String EVENT_AVENGERS = "Event_Avengers";
  public static final String EVENT_NO_HERO = "Event_NoHero";
  public static final String EVENT_DEADLINE_TIMER = "Event_DeadlineTimer";
  public static final String ACTIVITY_INFORM_FURY = "Activity_InformFury";
  public static final String AVENGERS_ASSEMBLE_SIGNAL = "AVENGERS_ASSEMBLE";
  public static final String MISSION_CANCELED_END = "Event_MissionCanceled";
  public static final String GATEWAY_EVENT_BASED = "Gateway_EventBased";
  public static final String FUTURE_DATE = "2099-04-24";
  public static final String EVALUATE_MISSION_USERTASK = "EvaluateMission";

  private ZeebeClient client;
  private ZeebeTestEngine engine;

  @BeforeEach
  public void deployResources() {

    deployResource(RESOURCE_FOLDER + "/" + "HeroRatingForm" + ".form");
    deployResource(RESOURCE_FOLDER + "/" + "HeroRequestForm" + ".form");
    deployResource(RESOURCE_FOLDER + "/" + "SelectHeroForm" + ".form");
    BpmnAssert
        .assertThat(deployResource(RESOURCE_FOLDER + "/" + HERO_PROCESS + ".bpmn"))
        .containsProcessesByBpmnProcessId(HERO_PROCESS);
  }

  @Test
  public void testHappyPathHeroAssignmentProcess() {
    // given
    String id = String.valueOf(UUID.randomUUID());


    HeroRequest heroRequest = HeroRequest.builder()
        .id(id)
        .startDate("01-01-2022")
        .deadline(FUTURE_DATE)
        .location("New York")
        .request("Save the world")
        .build();

    // when
    ProcessInstanceEvent processInstance = client.newCreateInstanceCommand()
        .bpmnProcessId(HERO_PROCESS)
        .latestVersion()
        .variables(heroRequest)
        .send()
        .join();

    completeServiceTask(GET_HEROS_JOB);

    completeUserTask();

    completeServiceTask(ASSIGN_HERO_JOB);

    publishMessage(MISSION_UPDATE_MESSAGE_NAME, id);

    completeUserTask();

    completeServiceTask(UPDATE_MISSION_JOB);

    // then

    BpmnAssert.assertThat(processInstance)
        .isCompleted()
        .hasPassedElement(ACTIVITY_GET_AVAILABLE_SUPER_HEROS)
        .hasPassedElement(ACTIVITY_ASSIGN_HERO_TO_MISSION)
        .hasPassedElement(ACTIVITY_UPDATE_MISSION)
        .hasNotPassedElement(EVENT_AVENGERS)
        .hasNotPassedElement(EVENT_NO_HERO)
        .hasNotPassedElement(EVENT_DEADLINE_TIMER);

  }

  @Test
  public void testErrorThrowWhenNoHerosAvailable() {
    // given
    ProcessInstanceEvent instance = startLatestVersionBeforeElement(HERO_PROCESS, ACTIVITY_GET_AVAILABLE_SUPER_HEROS);

    // when
    ActivateJobsResponse jobs = client.newActivateJobsCommand()
        .jobType(GET_HEROS_JOB)
        .maxJobsToActivate(1)
        .send()
        .join();

    Long jobKey = jobs.getJobs().get(0).getKey();
    client.newThrowErrorCommand(jobKey)
        .errorCode(NO_HERO_ERROR_CODE)
        .send()
        .join();

    completeRestConnector();

    // then
    BpmnAssert.assertThat(instance)
        .isCompleted()
        .hasPassedElement(ACTIVITY_INFORM_FURY)
        .hasNotPassedElement(ACTIVITY_GET_AVAILABLE_SUPER_HEROS)
        .hasNotPassedElement(ACTIVITY_ASSIGN_HERO_TO_MISSION);
  }

  @Test
  public void testAvengersAssembleSignal() {
    // given
    ProcessInstanceEvent instance = startLatestVersionBeforeElement(HERO_PROCESS, ACTIVITY_ASSIGN_HERO_TO_MISSION);

    // when
    client.newBroadcastSignalCommand()
        .signalName(AVENGERS_ASSEMBLE_SIGNAL)
        .send()
        .join();

    // then
    BpmnAssert.assertThat(instance)
        .isCompleted()
        .hasPassedElement(MISSION_CANCELED_END);
  }

  @SneakyThrows
  @Test
  public void testDeadlineExceeded() {
    // given
    ProcessInstanceEvent instance = client.newCreateInstanceCommand()
        .bpmnProcessId(HERO_PROCESS)
        .latestVersion()
        .startBeforeElement(GATEWAY_EVENT_BASED)
        .variables(Map.of("id","123","deadline", FUTURE_DATE ))
        .send()
        .join();
    BpmnAssert.assertThat(instance)
        .isWaitingAtElements(GATEWAY_EVENT_BASED)
        .isWaitingForMessages(MISSION_UPDATE_MESSAGE_NAME);
    // when
    engine.increaseTime(Duration.ofDays(365).multipliedBy(100));
    engine.waitForIdleState(Duration.ofSeconds(1));
    completeServiceTask(UPDATE_MISSION_JOB);
    // then
    BpmnAssert.assertThat(instance)
        .hasPassedElement(EVENT_DEADLINE_TIMER)
        .hasNotPassedElement(EVALUATE_MISSION_USERTASK)
        .isCompleted();
  }

  private ProcessInstanceEvent startLatestVersionBeforeElement(String processId, String Element) {
    ProcessInstanceEvent instance = client.newCreateInstanceCommand()
        .bpmnProcessId(processId)
        .latestVersion()
        .startBeforeElement(Element)
        .send()
        .join();
    return instance;
  }

  @SneakyThrows
  private void publishMessage(String messageName, String correlationKey) {
    client.newPublishMessageCommand()
        .messageName(messageName)
        .correlationKey(correlationKey)
        .timeToLive(Duration.ofSeconds(1))
        .send()
        .join();

    engine.waitForIdleState(Duration.ofSeconds(1));
  }

  @SneakyThrows
  private void completeServiceTask(String jobType) {

    ActivateJobsResponse jobs = client.newActivateJobsCommand()
        .jobType(jobType)
        .maxJobsToActivate(1)
        .send()
        .join();

    client.newCompleteCommand(jobs.getJobs().get(0).getKey())
        .send()
        .join();

    engine.waitForIdleState(Duration.ofSeconds(1));
  }

  public void completeUserTask() {
    completeServiceTask("io.camunda.zeebe:userTask");
  }

  public void completeRestConnector() {
    completeServiceTask("io.camunda:http-json:1");
  }

  public DeploymentEvent deployResource(String resourcePath) {

    return client.newDeployCommand()
        .addResourceFromClasspath(resourcePath)
        .send()
        .join();
  }

}
