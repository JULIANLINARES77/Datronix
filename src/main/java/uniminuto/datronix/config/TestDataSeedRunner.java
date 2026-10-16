package uniminuto.datronix.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import uniminuto.datronix.service.TestDataSeeder;

@Component
@ConditionalOnProperty(name = "datronix.test-data.enabled", havingValue = "true")
public class TestDataSeedRunner implements CommandLineRunner {
    private final TestDataSeeder testDataSeeder;

    public TestDataSeedRunner(TestDataSeeder testDataSeeder) {
        this.testDataSeeder = testDataSeeder;
    }

    @Override
    public void run(String... args) {
        testDataSeeder.seed();
    }
}
