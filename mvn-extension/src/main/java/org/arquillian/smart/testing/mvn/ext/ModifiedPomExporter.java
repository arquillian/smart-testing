package org.arquillian.smart.testing.mvn.ext;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.arquillian.smart.testing.hub.storage.local.LocalStorage;
import org.arquillian.smart.testing.hub.storage.local.LocalStorageFileAction;
import org.arquillian.smart.testing.logger.Log;
import org.arquillian.smart.testing.logger.Logger;

public class ModifiedPomExporter {

    public static final String SMART_TESTING_POM_FILE = "smart-testing-effective-pom.xml";

    private static Logger logger = Log.getLogger();

    static void exportModifiedPom(Model model) {
        try (StringWriter pomOut = new StringWriter()) {
            new MavenXpp3Writer().write(pomOut, model);
            writeModifiedPomToTarget(pomOut, model.getProjectDirectory());
        } catch (IOException e) {
            throw new RuntimeException("Failed writing updated pom file: " + model.getPomFile().getAbsolutePath(), e);
        }
    }

    private static void writeModifiedPomToTarget(StringWriter modifiedPom, File projectDir) throws IOException {

        final LocalStorageFileAction modifiedPomAction = new LocalStorage(projectDir)
            .afterExecution()
            .toReporting()
            .file(SMART_TESTING_POM_FILE);

        modifiedPomAction.create(modifiedPom.toString().getBytes());
        logger.debug("Modified pom stored at: " + modifiedPomAction.getPath());
    }
}



