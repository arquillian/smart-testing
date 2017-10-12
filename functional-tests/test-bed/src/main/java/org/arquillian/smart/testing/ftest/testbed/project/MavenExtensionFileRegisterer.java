package org.arquillian.smart.testing.ftest.testbed.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

class MavenExtensionFileRegisterer {

    public static final String MVN_CONFIG_DIR = ".mvn";
    public static final String EXTENSIONS_XML = "extensions.xml";
    private static final String VERSION_PLACEHOLDER = "${version}";

    private final Path rootPom;

    MavenExtensionFileRegisterer(Path rootPom) {
        this.rootPom = rootPom;
    }

    void addSmartTestingExtension(String version) {
        final Path projectRoot = rootPom.getParent();
        try {
            final Path extensionFolder = Files.createDirectories(projectRoot.resolve(MVN_CONFIG_DIR));
            final InputStream extensionFile =
                Thread.currentThread().getContextClassLoader().getResourceAsStream(EXTENSIONS_XML);

            try (BufferedReader br = new BufferedReader(new InputStreamReader(extensionFile))) {
                final String extensionTemplate = br.lines().collect(Collectors.joining(System.lineSeparator()));
                final String extensionContent = extensionTemplate.replace(VERSION_PLACEHOLDER, version);
                Files.write(extensionFolder.resolve(EXTENSIONS_XML), extensionContent.getBytes());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed registering smart-testing extension locally in .mvn folder", e);
        }
    }
}
