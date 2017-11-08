package org.arquillian.smart.testing.ftest.testbed.project;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

class MavenExtensionShadedJarRegisterer {

    void addSmartTestingExtension(String stVersion, String mavenVersion) {
        try {
            Path libExtDir = getMvnLibExtDirectory(mavenVersion);
            File shadedJar = retrieveShadedExtensionJar(stVersion);
            Files.copy(shadedJar.toPath(), libExtDir.resolve(shadedJar.getName()));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private Path getMvnLibExtDirectory(String mavenVersion) throws IOException {
        Path libExtDir = getMvnHome(mavenVersion).resolve("lib").resolve("ext");
        if (!libExtDir.toFile().exists()) {
            Files.createDirectories(libExtDir);
        }
        File[] libExtFiles = libExtDir.toFile().listFiles();
        Arrays.stream(libExtFiles)
            .filter(File::isFile).forEach(file -> {
            try {
                Files.delete(file.toPath());
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
        return libExtDir;
    }

    private Path getMvnHome(String mavenVersion) throws IOException {
        String mvnHomeName = "apache-maven-" + mavenVersion;
        Path resolverMaven = Paths.get("target","resolver-maven").toAbsolutePath();

        Path mvnHome = Files.walk(resolverMaven)
            .filter(path -> path.toFile().isDirectory() && (mvnHomeName).equals(path.toFile().getName()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(String.format(
                "There is no %s directory present inside of the %s directory. The extension cannot be installed.",
                mvnHomeName, resolverMaven)));
        return mvnHome;
    }

    private File retrieveShadedExtensionJar(String stVersion){
        return Maven.configureResolver()
            .withClassPathResolution(false)
            .resolve("org.arquillian.smart.testing:maven-lifecycle-extension:jar:shaded:" + stVersion)
            .withoutTransitivity()
            .asSingleFile();
    }
}
