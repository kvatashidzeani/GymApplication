Feature: Gym CRM authentication
  As a gym user
  I want to register and authenticate via the REST API
  So that I can access protected resources

  # ---------- Positive scenarios ----------

  @positive
  Scenario: Successful trainee registration returns credentials and JWT
    When I register a trainee with first name "Cucumber" and last name "Trainee"
    Then the registration response status should be 200
    And the response should contain a username and password
    And the response should contain a Bearer token

  @positive
  Scenario: Successful login with valid credentials returns JWT
    Given a registered trainee exists
    When I login with the registered trainee credentials
    Then the login response status should be 200
    And the response should contain a Bearer token

  # ---------- Negative scenarios ----------

  @negative
  Scenario: Login with unknown username is rejected
    When I login with username "unknown.user" and password "wrongpass01"
    Then the login response status should be 401

  @negative
  Scenario: Login with wrong password is rejected
    Given a registered trainee exists
    When I login with username of the registered trainee and password "badpassword"
    Then the login response status should be 401

  @negative
  Scenario: Registration without first name is rejected
    When I register a trainee with first name "" and last name "Trainee"
    Then the registration response status should be 400
