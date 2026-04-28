Feature: Cuentas API

  Background:
    * def port = karate.properties['server.port'] || 8080
    * url 'http://localhost:' + port

  Scenario: GET cuentas returns valid structure
    Given path 'v1/cuentas'
    And params { cursor: 0, pageSize: 20 }
    When method get
    Then status 200
    And match response contains { content: '#array', page: '#number', pageSize: '#number', totalElements: '#number', totalPages: '#number' }