Feature: Health Check - Verify API is running

  Background:
    * url 'http://localhost:' + (karate.properties['server.port'] || 8080)

  Scenario: Check if API is alive
    Given path 'actuator/health'
    When method get
    Then status 200
    And print 'Health response:', response