Feature: Movimientos API

  Background:
    * def port = karate.properties['server.port'] || 8080
    * url 'http://localhost:' + port

  Scenario: GET movimientos returns valid structure
    Given path 'v1/movimientos'
    And params { page: 0, pageSize: 20, startDate: '2026-01-01T00:00:00', endDate: '2026-12-31T23:59:59' }
    When method get
    Then status 200
    And match response contains { content: '#array', page: '#number', pageSize: '#number', totalElements: '#number', totalPages: '#number' }