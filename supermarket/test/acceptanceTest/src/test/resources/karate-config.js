function fn() {
    // karate.env se activa con -Dkarate.env=staging|prod
    var env = karate.env;
    if (!env) env = 'local';

    var config = {};

    // 1. Prioridad máxima: propiedad explícita -DbaseUrl=https://...
    //    (usada por el pipeline de Azure DevOps)
    var explicitBaseUrl = karate.properties['baseUrl'];

    if (explicitBaseUrl && explicitBaseUrl !== 'http://localhost:8080') {
        config.baseUrl = explicitBaseUrl;
    } else if (env === 'staging') {
        config.baseUrl = 'https://supermarket-staging.azurewebsites.net';
    } else if (env === 'prod') {
        config.baseUrl = 'https://supermarket.azurewebsites.net';
    } else {
        // local (default)
        config.baseUrl = 'http://localhost:8080';
    }

    karate.log('[karate-config] env:', env, '| baseUrl:', config.baseUrl);
    return config;
}
