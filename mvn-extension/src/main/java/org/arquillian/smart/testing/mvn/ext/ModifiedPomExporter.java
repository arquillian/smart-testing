package org.arquillian.smart.testing.mvn.ext;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.arquillian.smart.testing.logger.Logger;
import org.arquillian.smart.testing.logger.Log;

class ModifiedPomExporter {

    private static final String SMART_TESTING_POM_FILE = "smart-testing-effective-pom.xml";

    private static Logger logger = Log.getLogger();

    static void exportModifiedPom(Model model) {
        try (StringWriter pomOut = new StringWriter()) {
            new MavenXpp3Writer().write(pomOut, model);
            writeModifiedPomToTarget(pomOut, model.getProjectDirectory());
        } catch (IOException e) {
            throw new RuntimeException("Failed writing updated pom file: " + model.getPomFile().getAbsolutePath(), e);
        }
    }

    private static void writeModifiedPomToTarget(StringWriter modifiedPom, File project) throws IOException {
        Path path = Paths.get(project.toPath() + File.separator + "target", "smart-testing");
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }
        final Path target = Paths.get(path + File.separator + SMART_TESTING_POM_FILE);
        Files.write(target, modifiedPom.toString().getBytes());
        logger.debug("Modified pom stored at: " + target);
    }
}



