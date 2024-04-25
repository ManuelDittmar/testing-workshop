package org.camunda.consulting.testingworkshop.repository;

import java.util.List;
import org.camunda.consulting.testingworkshop.model.Mission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, Long> {
  List<Mission> findByCharacterId(Long characterId);
}

