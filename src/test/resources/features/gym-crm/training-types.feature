Feature: Training types catalog
  As a gym user
  I want to list available training types
  So that I can choose workouts

  # ---------- Positive scenarios ----------

  @positive
  Scenario: Authenticated user can list training types
    Given a registered trainee exists
    And I am authenticated as the registered trainee
    When I request the training types catalog
    Then the training types response status should be 200
    And the training types list should not be empty

  # ---------- Negative scenarios ----------

  @negative
  Scenario: Unauthenticated user cannot list training types
    When I request the training types catalog without authentication
    Then the training types response status should be 401
