package acceptanceTest;

import com.intuit.karate.junit5.Karate;

/**
 * Punto de entrada JUnit 5 para las pruebas de aceptación Karate.
 *
 * Ejecutar localmente:
 *   mvn test
 *
 * Ejecutar contra el servidor desplegado en Azure (desde el pipeline):
 *   mvn test -DbaseUrl=https://supermarket-app.azurewebsites.net
 *
 * Ejecutar solo un entorno nombrado:
 *   mvn test -Dkarate.env=staging
 */
class AcceptanceTestRunner {

    @Karate.Test
    Karate testAll() {
        return Karate.run("classpath:acceptanceTest");
    }
}
