Feature: Trainer workload REST API
  As a gym user
  I want to read trainer workload summaries
  So that I can review monthly training hours

  # ---------- Positive scenarios ----------

  @positive
  Scenario: Authenticated user can read trainer workload summary
    Given trainer "Mike.Brown" has workload for year 2026 month 8 with 90 minutes
    And I have a valid JWT for user "Mike.Brown"
    When I request workload for trainer "Mike.Brown"
    Then the workload response status should be 200
    And the workload summary contains year 2026 month 8 with duration 90

  @positive
  Scenario: Authenticated user can read month hours
    Given trainer "Sarah.Johnson" has workload for year 2026 month 3 with 45 minutes
    And I have a valid JWT for user "Sarah.Johnson"
    When I request month hours for trainer "Sarah.Johnson" year 2026 month 3
    Then the month hours response status should be 200
    And the month hours duration should be 45

  # ---------- Negative scenarios ----------

  @negative
  Scenario: Unauthenticated workload request is rejected
    When I request workload for trainer "Mike.Brown" without authentication
    Then the workload response status should be 401

  @negative
  Scenario: Authenticated request for unknown trainer returns not found
    Given I have a valid JWT for user "Mike.Brown"
    When I request workload for trainer "Unknown.Trainer"
    Then the workload response status should be 404

  @negative
  Scenario: Month hours request with invalid month is rejected
    Given I have a valid JWT for user "Mike.Brown"
    When I request month hours for trainer "Mike.Brown" year 2026 month 13
    Then the month hours response status should be 400
