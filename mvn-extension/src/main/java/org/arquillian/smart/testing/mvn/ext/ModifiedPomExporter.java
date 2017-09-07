package org.arquillian.smart.testing.mvn.ext;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.arquillian.smart.testing.Logger;

class ModifiedPomExporter {

    static Logger logger = Logger.getLogger(ModifiedPomExporter.class);

    private static String SMART_TESTING_POM_FILE = "smart-testing-pom.xml";

    static void showPom(Model model) {
        try (StringWriter pomOut = new StringWriter()) {
            new MavenXpp3Writer().write(pomOut, model);
            copyModifiedPomInTarget(pomOut, model.getProjectDirectory());
        } catch (IOException e) {
            throw new RuntimeException("Failed writing updated pom file: " + model.getPomFile().getAbsolutePath(), e);
        }
    }

    static void copyModifiedPomInTarget(StringWriter modifiedPom, File project) throws IOException {
        Path path = Paths.get(project.toPath() + File.separator + "target", "smart-testing");
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        final Path target = Paths.get(path + File.separator + SMART_TESTING_POM_FILE);
        Files.write(target, modifiedPom.toString().getBytes());
        logger.debug("Copied modified pom to: " + target);
    }
}



