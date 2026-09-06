Feature: Gym CRM actuator health
  As an operator
  I want to check service health
  So that I can verify the application is running

  @positive
  Scenario: Health endpoint is publicly accessible
    When I request the health endpoint
    Then the health response status should be 200
