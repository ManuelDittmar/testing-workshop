package org.camunda.consulting.testingworkshop.repository;

import java.util.List;
import org.camunda.consulting.testingworkshop.model.MarvelCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MarvelCharacterRepository extends JpaRepository<MarvelCharacter, Long> {

  @Query("SELECT mc FROM MarvelCharacter mc WHERE NOT EXISTS (" +
      "SELECT m FROM Mission m WHERE m.character = mc AND m.status = 'ASSIGNED')")
  List<MarvelCharacter> findCharactersWithoutActiveMissions();
}

