package org.arquillian.smart.testing.ftest.testbed.testresults;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * This is temporary solution - will be refined before test bed goes to master
 */
public class SurefireReportReader {

    public static Collection<TestResult> loadTestResults(InputStream surefireInputStream) {
        final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = null;

        try {
            eventReader = inputFactory.createXMLEventReader(surefireInputStream);
            return readTestResults(eventReader);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Fails reading test reports", e);
        } finally {
            if (eventReader != null) {
                try {
                    eventReader.close();
                } catch (XMLStreamException e) {
                    throw new RuntimeException("Fails reading test reports", e);
                }
            }
        }
    }

    private static Collection<TestResult> readTestResults(XMLEventReader eventReader) throws XMLStreamException {
        final Set<TestResult> testResults = new HashSet<>();
        TestResult currentTestResult = null;

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();

            if (event.isStartElement()) {
                final StartElement startElement = event.asStartElement();
                if ("testcase".equalsIgnoreCase(startElement.getName().getLocalPart())) {
                    // Read attributes
                    String name = null, classname = null;
                    Status status = null;

                    final Iterator<Attribute> attributes = startElement.getAttributes();

                    while (attributes.hasNext()) {
                        final Attribute attribute = attributes.next();
                        if ("classname".equalsIgnoreCase(attribute.getName().toString())) {
                            classname = attribute.getValue();
                        }

                        if ("name".equalsIgnoreCase(attribute.getName().toString())) {
                            name = attribute.getValue();
                        }
                    }

                    currentTestResult = new TestResult(classname, name, Status.PASSED);
                }

                if ("failure".equalsIgnoreCase(startElement.getName().getLocalPart())) {
                    currentTestResult.setStatus(Status.FAILURE);
                }

                if ("error".equalsIgnoreCase(startElement.getName().getLocalPart())) {
                    currentTestResult.setStatus(Status.ERROR);
                }

                if ("skipped".equalsIgnoreCase(startElement.getName().getLocalPart())) {
                    currentTestResult.setStatus(Status.SKIPPED);
                }

                if ("rerunFailure".equalsIgnoreCase(startElement.getName().getLocalPart())) {
                    currentTestResult.setStatus(Status.RE_RUN_FAILURE);
                }
            }

            if (event.isEndElement()) {
                final EndElement endElementElement = event.asEndElement();
                if ("testcase".equalsIgnoreCase(endElementElement.getName().getLocalPart())) {
                    testResults.add(currentTestResult);
                }
            }
        }
        return testResults;
    }
}
