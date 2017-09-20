package org.arquillian.smart.testing.report;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

class ExecutionReportMarshaller {

    static void marshal(File reportFile, Object object) {
        try {
            JAXBContext context = JAXBContext.newInstance(object.getClass());
            javax.xml.bind.Marshaller m = context.createMarshaller();
            m.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            m.marshal(object, reportFile);
        } catch (JAXBException e) {
            throw new IllegalStateException("Error during marshaling execution", e);
        }
    }
}
