function fn() {
    var config = {
        baseUrl: 'http://localhost:' + (karate.properties['server.port'] || 8080)
    };
    return config;
}