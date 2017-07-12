package org.arquillian.smart.testing.hub.storage;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

public class LocalTestPlanPersister implements TestPlanPersister {

    private static final String SMART_TESTING_PLAN = ".smart-testing-plan";

    @Override
    public void storeTestPlan(Set<String> testPlan) {
        try (BufferedWriter smartTestingFile = Files.newBufferedWriter(Paths.get(".", SMART_TESTING_PLAN))){
            testPlan.forEach(testClassName -> {
                try {
                    smartTestingFile.write(testClassName);
                    smartTestingFile.newLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Cannot create "+ SMART_TESTING_PLAN + " file", e);
        }
    }
}
