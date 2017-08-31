package org.arquillian.smart.testing.report;

import java.io.File;

class ExecutionReporterHelper {

    static String getBaseDir(Class<?> aClass) {
        final String path = aClass.getResource("/sample-report.xml").getPath();
        return path.substring(0, path.indexOf(File.separator + "target"));
    }
}
