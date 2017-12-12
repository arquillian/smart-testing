package org.arquillian.smart.testing.mvn.ext.dependencies;

import java.util.Arrays;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;

class MavenCoordinatesResolver {

    static Dependency createDependencyFromCoordinates(String coordinatesString, boolean excludeTransitive) {
        final String[] coordinates = coordinatesString.split(":");
        int amountOfCoordinates = coordinates.length;
        if (amountOfCoordinates < 2) {
            throw new IllegalArgumentException(
                "Coordinates of the specified strategy [" + coordinatesString + "] doesn't have the correct format.");
        }
        final Dependency dependency = new Dependency();
        dependency.setGroupId(coordinates[0]);
        dependency.setArtifactId(coordinates[1]);
        if (amountOfCoordinates == 3) {
            dependency.setVersion(coordinates[2]);
        } else if (amountOfCoordinates >= 4) {
            dependency.setType(coordinates[2].isEmpty() ? "jar" : coordinates[2]);
            dependency.setClassifier(coordinates[3]);
        }
        if (amountOfCoordinates >= 5) {
            dependency.setVersion(coordinates[4]);
        }
        if (amountOfCoordinates == 6) {
            dependency.setScope(coordinates[5]);
        }
        if (dependency.getVersion() == null || dependency.getVersion().isEmpty()) {
            dependency.setVersion(ExtensionVersion.version().toString());
        }
        if (excludeTransitive) {
            Exclusion exclusion = new Exclusion();
            exclusion.setGroupId("*");
            exclusion.setArtifactId("*");
            dependency.setExclusions(Arrays.asList(exclusion));
        }
        return dependency;
    }
}
