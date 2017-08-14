package org.arquillian.smart.testing.report;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class SmartTestExecutionReportMarshaller {

    private final String reportDir;
    private final String fileName;

    public SmartTestExecutionReportMarshaller(String reportDir, String fileName) {
        this.reportDir = reportDir != null ? getAbsoluteReportDir(reportDir) : getDefaultReportDir();
        this.fileName = fileName != null ? getFileName(fileName) : "smart-testing-report.xml";
    }

    public void marshal(Object object) {
        createFileAndDirForReport();
        try {
            JAXBContext context = JAXBContext.newInstance(object.getClass());
            javax.xml.bind.Marshaller m = context.createMarshaller();
            m.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            m.marshal(object, getAbsolutePathForReport());
        } catch (JAXBException e) {
            throw new IllegalStateException("Error during marshaling execution", e);
        }
    }

    private String getUserDir() {
        return System.getProperty("user.dir") + File.separator;
    }

    private String getDefaultReportDir() {
        return getUserDir() + "target" + File.separator;
    }

    private void createFileAndDirForReport() {
        final File file = new File(reportDir);
        if (!file.exists()) {
            if (!file.mkdir()) {
                throw new IllegalStateException("Failed to create directory " + file.getParent());
            }
        }
        try {
            getAbsolutePathForReport().createNewFile();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create file " + file);
        }
    }

    private File getAbsolutePathForReport() {
        File reportFile;
        if (!reportDir.endsWith(File.separator)) {
            reportFile = new File(reportDir + File.separator + fileName);
        } else {
            reportFile = new File(reportDir + fileName);
        }

        return reportFile;
    }

    private String getAbsoluteReportDir(String reportDir) {
        return getUserDir() + reportDir;
    }
    private String getFileName(String fileName) {
       return fileName.endsWith(".xml") ? fileName : fileName + ".xml";
    }
}
