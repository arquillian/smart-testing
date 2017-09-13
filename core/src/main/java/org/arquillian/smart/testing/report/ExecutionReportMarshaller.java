package org.arquillian.smart.testing.report;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

class ExecutionReportMarshaller {

    private final String reportDir;
    private final String fileName;
    private final String baseDir;

    ExecutionReportMarshaller(String baseDir, String reportDir, String fileName) {
        this.baseDir = baseDir;
        this.reportDir = getReportDir(reportDir);
        this.fileName = getFileName(fileName);
    }

    void marshal(Object object) {
        createDirForReport();
        try {
            JAXBContext context = JAXBContext.newInstance(object.getClass());
            javax.xml.bind.Marshaller m = context.createMarshaller();
            m.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            m.marshal(object, getPathForReportFile().toFile());
        } catch (JAXBException e) {
            throw new IllegalStateException("Error during marshaling execution", e);
        }
    }

    private void createDirForReport() {
        try {
            Files.createDirectories(Paths.get(reportDir));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path getPathForReportFile() {
        return Paths.get(reportDir, fileName);
    }

    private String getReportDir(String reportDir) {
        return String.join(File.separator, baseDir, reportDir);
    }

    private String getFileName(String fileName) {
       return fileName.endsWith(".xml") ? fileName : fileName + ".xml";
    }
}
