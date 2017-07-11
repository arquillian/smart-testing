package org.arquillian.smart.testing.ftest.testbed.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

class MavenExtensionRegisterer {

    private static final String EXTENSIONS_XML = "extensions.xml";
    private static final String VERSION_PLACEHOLDER = "${version}";

    private final ProjectConfigurator projectConfigurator;
    private final Path rootPom;

    MavenExtensionRegisterer(Path rootPom, ProjectConfigurator projectConfigurator) {
        this.rootPom = rootPom;
        this.projectConfigurator = projectConfigurator;
    }

    void addSmartTestingExtension() {
        final Path projectRoot = rootPom.getParent();
        try {
            final Path extensionFolder = Files.createDirectories(projectRoot.resolve(".mvn"));
            final InputStream extensionFile =
                Thread.currentThread().getContextClassLoader().getResourceAsStream(EXTENSIONS_XML);

            try (BufferedReader br = new BufferedReader(new InputStreamReader(extensionFile))) {
                final String extensionTemplate = br.lines().collect(Collectors.joining(System.lineSeparator()));
                final String extensionContent = extensionTemplate.replace(VERSION_PLACEHOLDER, Project.SMART_TESTING_VERSION);
                Files.write(extensionFolder.resolve(EXTENSIONS_XML), extensionContent.getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed registering smart-testing extension locally in .mvn folder", e);
        }
    }

}
