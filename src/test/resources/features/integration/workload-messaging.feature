Feature: Microservice workload integration
  As a gym user
  I want training changes in Gym CRM to reach the workload microservice through ActiveMQ
  So that trainer monthly hours stay consistent across services

  Background:
    Given a registered trainer with specialization "Cardio"
    And a registered trainee exists

  # ---------- Positive scenarios ----------

  @positive @integration
  Scenario: Adding a training updates trainer workload via ActiveMQ
    Given I am authenticated as the registered trainee
    When I add a training with the registered trainer on "2026-06-15" for 90 minutes
    Then the training response status should be 200
    And the trainer workload for year 2026 month 6 should be 90 minutes

  @positive @integration
  Scenario: Adding multiple trainings accumulates workload duration
    Given I am authenticated as the registered trainee
    When I add a training with the registered trainer on "2026-07-10" for 45 minutes
    And I add a training with the registered trainer on "2026-07-20" for 30 minutes
    Then the training response status should be 200
    And the trainer workload for year 2026 month 7 should be 75 minutes

  # ---------- Negative scenarios ----------

  @negative @integration
  Scenario: Unauthenticated training creation is rejected and workload stays unchanged
    When I add a training with the registered trainer on "2026-08-01" for 60 minutes without authentication
    Then the training response status should be 401
    And the trainer workload for year 2026 month 8 should be 0 minutes

  @negative @integration
  Scenario: Training with unknown trainer is rejected and workload stays unchanged
    Given I am authenticated as the registered trainee
    When I add a training with trainer "Unknown.Coach" on "2026-09-01" for 60 minutes
    Then the training response status should be 404
    And the trainer workload for year 2026 month 9 should be 0 minutes
