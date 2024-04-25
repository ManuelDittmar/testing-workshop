package org.camunda.consulting.testingworkshop.model;

import java.util.Date;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class HeroRequest {
  String id;
  String startDate;
  String deadline;
  String location;
  String request;

}
