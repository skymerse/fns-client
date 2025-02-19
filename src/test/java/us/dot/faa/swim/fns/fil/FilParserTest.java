package us.dot.faa.swim.fns.fil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

@ExtendWith(MockitoExtension.class)
public class FilParserTest {

    private static final int WORKER_THREAD_COUNT = 2;
    private static final int WORK_QUEUE_SIZE = 10;

    private TestFilParserWorker filParserWorker;

    private FilParser filParser;

    @BeforeEach
    void setUp() {
        filParser = new FilParser(WORKER_THREAD_COUNT, WORK_QUEUE_SIZE);
        filParserWorker = new TestFilParserWorker();
    }

    @Test
    void testGetPendingJobCount() {
        assertEquals(0, filParser.getPendingJobCount());
    }

    @Test
    void testParseValidInput() throws Exception {
        // Create a simple valid XML input
        String xmlInput = "<?xml version=\"1.0\"?>" +
                "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                "  <soap:Body>\n" +
                "    <ns3:FeatureCollection\n" +
                "        xmlns:ns1=\"http://www.opengis.net/ows/1.1\"\n" + 
                "        xmlns:ns2=\"http://www.w3.org/1999/xlink\"\n" +
                "        xmlns:ns3=\"http://www.opengis.net/wfs/2.0\"\n" +
                "        xmlns:ns4=\"http://www.opengis.net/fes/2.0\"\n" +
                "        xmlns:ns5=\"http://www.opengis.net/gml/3.2\"\n" +
                "        xmlns:ns6=\"http://www.aixm.aero/schema/5.1/extensions/FAA/FNSE\"\n" +
                "        xmlns:ns7=\"http://www.isotc211.org/2005/gco\"\n" +
                "        xmlns:ns8=\"http://www.isotc211.org/2005/gmd\"\n" +
                "        xmlns:ns9=\"http://www.aixm.aero/schema/5.1\"\n" +
                "        xmlns:ns10=\"http://www.isotc211.org/2005/gts\"\n" +
                "        xmlns:ns11=\"http://www.aixm.aero/schema/5.1/event\"\n" +
                "        xmlns:ns12=\"urn:us.gov.dot.faa.aim.fns\"\n" +
                "        xmlns:ns13=\"http://www.aixm.aero/schema/5.1/message\"\n" +
                "        xmlns:ns14=\"http://www.opengis.net/wfs-util/2.0\"\n" +
                "        numberReturned=\"78003\"\n" +
                "        numberMatched=\"78003\"\n" +
                "        timeStamp=\"1900-01-01T00:00:00.000Z\">\n" +
                "      <ns3:member>\n" +
                "        <ns13:AIXMBasicMessage ns5:id=\"FNS_ID_34821838\">\n" +
                "          <ns5:boundedBy xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>\n" +
                "          <ns13:hasMember>\n" +
                "            <ns11:Event ns5:id=\"Event_1_34821838\">\n" +
                "              <ns5:boundedBy xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"/>\n" +
                "              <ns11:timeSlice>\n" +
                "                <ns11:EventTimeSlice ns5:id=\"Event_TS_1_34821838\">\n" +
                "                  <ns5:validTime>\n" +
                "                    <ns5:TimePeriod ns5:id=\"Event_TS_TP_1_34821838\">\n" +
                "                      <ns5:beginPosition>2009-02-25T06:57:00.000Z</ns5:beginPosition>\n" +
                "                      <ns5:endPosition indeterminatePosition=\"unknown\">2009-03-26T08:00:00.000Z</ns5:endPosition>\n" +
                "                    </ns5:TimePeriod>\n" +
                "                  </ns5:validTime>\n" +
                "                  <ns9:interpretation>BASELINE</ns9:interpretation>\n" +
                "                  <ns11:scenario>6000</ns11:scenario>\n" +
                "                  <ns11:textNOTAM>\n" +
                "                    <ns11:NOTAM ns5:id=\"NOTAM_1_34821838\">\n" +
                "                      <ns11:series>B</ns11:series>\n" +
                "                      <ns11:number>131</ns11:number>\n" +
                "                      <ns11:year>2009</ns11:year>\n" +
                "                      <ns11:type>R</ns11:type>\n" +
                "                      <ns11:issued>2009-02-25T09:51:00.000Z</ns11:issued>\n" +
                "                      <ns11:affectedFIR>DAAA</ns11:affectedFIR>\n" +
                "                      <ns11:selectionCode>QFWAS</ns11:selectionCode>\n" +
                "                      <ns11:traffic>V</ns11:traffic>\n" +
                "                      <ns11:purpose>M</ns11:purpose>\n" +
                "                      <ns11:scope>A</ns11:scope>\n" +
                "                      <ns11:minimumFL>000</ns11:minimumFL>\n" +
                "                      <ns11:maximumFL>999</ns11:maximumFL>\n" +
                "                      <ns11:coordinates>3513N00009E</ns11:coordinates>\n" +
                "                      <ns11:radius>005</ns11:radius>\n" +
                "                      <ns11:location>DAOV</ns11:location>\n" +
                "                      <ns11:effectiveStart>200902250657</ns11:effectiveStart>\n" +
                "                      <ns11:effectiveEnd>200903260800EST</ns11:effectiveEnd>\n" +
                "                      <ns11:text>WIND DIRECTION INDICATOR UNSERVICEABLE)\n" +
                "DUPE</ns11:text>\n" +
                "                      <ns11:translation>\n" +
                "                        <ns11:NOTAMTranslation ns5:id=\"NT02_34821838\">\n" +
                "                          <ns11:type>OTHER:ICAO</ns11:type>\n" +
                "                          <ns11:formattedText>\n" +
                "                            <html:div xmlns:html=\"http://www.w3.org/1999/xhtml\"\n" +
                "                                xmlns=\"http://www.aixm.aero/schema/5.1/message\"\n" +
                "                                xmlns:aixm=\"http://www.aixm.aero/schema/5.1\"\n" +
                "                                xmlns:event=\"http://www.aixm.aero/schema/5.1/event\"\n" +
                "                                xmlns:fnse=\"http://www.aixm.aero/schema/5.1/extensions/FAA/FNSE\"\n" +
                "                                xmlns:gco=\"http://www.isotc211.org/2005/gco\"\n" +
                "                                xmlns:gmd=\"http://www.isotc211.org/2005/gmd\"\n" +
                "                                xmlns:gml=\"http://www.opengis.net/gml/3.2\"\n" +
                "                                xmlns:gsr=\"http://www.isotc211.org/2005/gsr\"\n" +
                "                                xmlns:gss=\"http://www.isotc211.org/2005/gss\"\n" +
                "                                xmlns:gts=\"http://www.isotc211.org/2005/gts\"\n" +
                "                                xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n" +
                "                                xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">&lt;pre&gt;\n" +
                "B0131/09 NOTAMR B0066/09\n" +
                "Q) DAAA/QFWAS/V/M/A/000/999/3513N00009E005\n" +
                "A) DAOV B) 0902250657 C) 0903260800 EST\n" +
                "E) WIND DIRECTION INDICATOR UNSERVICEABLE)\n" +
                "DUPE\n" +
                "&lt;/pre&gt;</html:div>\n" +
                "                          </ns11:formattedText>\n" +
                "                        </ns11:NOTAMTranslation>\n" +
                "                      </ns11:translation>\n" +
                "                    </ns11:NOTAM>\n" +
                "                  </ns11:textNOTAM>\n" +
                "                </ns11:EventTimeSlice>\n" +
                "              </ns11:timeSlice>\n" +
                "            </ns11:Event>\n" +
                "          </ns13:hasMember>\n" +
                "        </ns13:AIXMBasicMessage>\n" +
                "      </ns3:member>\n" +
                "    </ns3:FeatureCollection>\n" +
                "  </soap:Body>\n" +
                "</soap:Envelope>";
        
        InputStream inputStream = new ByteArrayInputStream(
            xmlInput.getBytes(StandardCharsets.UTF_8)
        );

        // Execute
        filParser.parseFilFile(inputStream, filParserWorker);

        assertEquals(1, filParserWorker.messages.size());
    }

    @Test
    void testParseInvalidXml() {
        // Create invalid XML input
        String invalidXml = "This is not XML";
        InputStream inputStream = new ByteArrayInputStream(
            invalidXml.getBytes(StandardCharsets.UTF_8)
        );

        // Execute and verify exception is thrown
        assertThrows(Exception.class, () -> 
            filParser.parseFilFile(inputStream, filParserWorker)
        );
    }

    private static class TestFilParserWorker implements FilParserWorker {
        public ArrayList<String> messages;

        TestFilParserWorker() {
            this.messages = new ArrayList<>();
        }

        @Override
        public void processesMessage(String message) {
            messages.add(message);
        }
    }
} 