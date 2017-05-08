package org.arquillian.smart.testing.strategies.failed.surefire;

import java.io.InputStream;
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
import org.arquillian.smart.testing.strategies.failed.TestResult;

// TODO move to spi for others implementations
// TODO should we just return failures/errors/rerunFailures or the reader should add all of them and filter later? If moved to SPI then all

public class SurefireReader {

    public static Set<TestResult> loadTestResults(InputStream surefireInputStream) throws XMLStreamException {

        final Set<TestResult> testResults = new HashSet<>();

        final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLEventReader eventReader = inputFactory.createXMLEventReader(surefireInputStream);

        TestResult currentTestResult = null;

        while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();

            if (event.isStartElement()) {
                final StartElement startElement = event.asStartElement();
                if ("testcase".equalsIgnoreCase(startElement.getName().getLocalPart())) {
                    // Read attributes
                    String name = null, classname = null, duration = null;

                    final Iterator<Attribute> attributes = startElement.getAttributes();

                    while (attributes.hasNext()) {
                        final Attribute attribute = attributes.next();
                        if ("classname".equalsIgnoreCase(attribute.getName().toString())) {
                            classname = attribute.getValue();
                        }

                        if ("name".equalsIgnoreCase(attribute.getName().toString())) {
                            name = attribute.getValue();
                        }

                        if ("time".equalsIgnoreCase(attribute.getName().toString())) {
                            duration = attribute.getValue();
                        }
                    }

                    final float durationInSeconds = Float.parseFloat(duration);
                    currentTestResult = new TestResult(classname, name, durationInSeconds);

                }

                if ("failure".equalsIgnoreCase(startElement.getName().getLocalPart())) {
                    currentTestResult.setResult(TestResult.Result.FAILURE);
                }

                if ("error".equalsIgnoreCase(startElement.getName().getLocalPart())) {
                    currentTestResult.setResult(TestResult.Result.ERROR);
                }

                if ("skipped".equalsIgnoreCase(startElement.getName().getLocalPart())) {
                    currentTestResult.setResult(TestResult.Result.SKIPPED);
                }

                if ("rerunFailure".equalsIgnoreCase(startElement.getName().getLocalPart())) {
                    currentTestResult.setResult(TestResult.Result.RE_RUN_FAILURE);
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
