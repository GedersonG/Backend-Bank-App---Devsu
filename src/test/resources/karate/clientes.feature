Feature: Clientes API

  Background:
    * def port = karate.properties['server.port'] || 8080
    * url 'http://localhost:' + port

  Scenario: GET clientes returns valid structure
    Given path 'v1/clientes'
    And params { page: 0, pageSize: 5 }
    When method get
    Then status 200
    And match response contains { content: '#array', page: 0, pageSize: '#number', totalElements: '#number', totalPages: '#number' }