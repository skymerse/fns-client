package us.dot.faa.swim.fns.notamdb;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.BeforeAll;
import us.dot.faa.swim.fns.FnsMessage;
import us.dot.faa.swim.fns.FnsMessage.NotamStatus;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class NotamDbTest {
    private NotamDb notamDb;
    private NotamDbConfig config = new NotamDbConfig()
            .setDriver("org.postgresql.Driver")
            .setConnectionUrl("jdbc:postgresql://localhost:5432/notamdb_test")
            .setUsername("notam")
            .setPassword("notampass")
            .setSchema("public")
            .setTable("notams");


    @BeforeAll
    public void setUp() throws Exception {
        notamDb = new NotamDb(config);
        notamDb.dropNotamTable();
        notamDb.createNotamTable();
    }

    @BeforeEach
    public void setUpEach() throws Exception {
        try (Connection conn = notamDb.getDBConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM fns_notams");
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new Exception("Error deleting NOTAMS", e);
        }
    }

    private FnsMessage createFnsMessage() throws JAXBException {
        return createFnsMessage("<test>Sample AIXM message</test>");
    }

    private FnsMessage createFnsMessage(String aixmNotamMessage) throws JAXBException {
        int fnsId = 12345;
        long correlationId = 67890L;
        
        // Create proper timestamp objects
        Instant now = Instant.now();
        Instant future = now.plusSeconds(3600); // Valid for 1 hour
        
        String locationDesignator = "KLAX";
        String classification = "TEST";
        String notamAccountability = "TEST123";
        String notamText = "Test NOTAM message";

        // Create a test NOTAM message using the constructor with all fields
        FnsMessage testNotam = new FnsMessage(
            fnsId,
            correlationId,
            Timestamp.from(now),  // issuedTimestamp
            Timestamp.from(now),  // updatedTimestamp
            Timestamp.from(now),  // validFromTimestamp
            Timestamp.from(future),  // validToTimestamp
            locationDesignator,
            classification,
            notamAccountability,
            notamText,
            aixmNotamMessage
        );

        testNotam.setStatus(NotamStatus.ACTIVE);

        return testNotam;
    }

    @Test
    public void testInitialization() throws Exception {
        assertTrue(notamDb.notamTableExists(), "NotamDb should exist");
        assertFalse(notamDb.isValid(), "NotamDb should not be valid initially");
        assertFalse(notamDb.isInitializing(), "NotamDb should not be initializing");
    }

    @Test
    public void testPutNotam() throws Exception {
        // Create test data
        FnsMessage testNotam = createFnsMessage();

        // Put the NOTAM in the database
        NotamBean notam = notamDb.putNotamJdbi(testNotam);

        // Verify the NOTAM was stored by checking if it exists
        try (Connection conn = notamDb.getDBConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM fns_notams WHERE fnsid = ?");
            stmt.setLong(1, testNotam.getFNS_ID());
            ResultSet rs = stmt.executeQuery();

            assertTrue(rs.next(), "NOTAM should exist in database");
            assertEquals(testNotam.getFNS_ID(), rs.getLong("fnsid"));
            assertEquals(testNotam.getCorrelationId(), rs.getLong("correlationId"));
            assertEquals(testNotam.getClassification(), rs.getString("classification"));
            assertEquals(testNotam.getLocationDesignator(), rs.getString("locationDesignator"));
            assertEquals(testNotam.getNotamAccountability(), rs.getString("notamAccountability"));
            assertEquals(testNotam.getNotamText(), rs.getString("notamText"));
            assertEquals(testNotam.getStatus().toString(), rs.getString("status"));
            assertFalse(rs.next(), "Should only be one matching NOTAM");
        }
    }

    @Test
    public void testPutNotam_duplicate() throws Exception {
        FnsMessage testNotam = createFnsMessage();

        notamDb.putNotamJdbi(testNotam);
        notamDb.putNotamJdbi(testNotam);

        List<NotamBean> notams = notamDb.getAll();
        assertEquals(1, notams.size());
    }

    @Test
    public void testPutNotam_icaoParse() throws Exception {
        String aixmNotamMessage = "<ns13:AIXMBasicMessage ns5:id=\"FNS_ID_75742039\" xmlns:ns1=\"http://www.opengis.net/ows/1.1\" " +
            "xmlns:ns10=\"http://www.isotc211.org/2005/gts\" xmlns:ns11=\"http://www.aixm.aero/schema/5.1/event\" " +
            "xmlns:ns12=\"urn:us.gov.dot.faa.aim.fns\" xmlns:ns13=\"http://www.aixm.aero/schema/5.1/message\" " +
            "xmlns:ns14=\"http://www.opengis.net/wfs-util/2.0\" xmlns:ns2=\"http://www.w3.org/1999/xlink\" " +
            "xmlns:ns3=\"http://www.opengis.net/wfs/2.0\" xmlns:ns4=\"http://www.opengis.net/fes/2.0\" " +
            "xmlns:ns5=\"http://www.opengis.net/gml/3.2\" xmlns:ns6=\"http://www.aixm.aero/schema/5.1/extensions/FAA/FNSE\" " +
            "xmlns:ns7=\"http://www.isotc211.org/2005/gco\" xmlns:ns8=\"http://www.isotc211.org/2005/gmd\" " +
            "xmlns:ns9=\"http://www.aixm.aero/schema/5.1\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
            "<ns5:boundedBy xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"></ns5:boundedBy>" +
            "<ns13:hasMember><ns9:RunwayDirectionLightSystem ns5:id=\"ALS01_75742039\">" +
            "<ns5:boundedBy xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"></ns5:boundedBy>" +
            "<ns9:timeSlice><ns9:RunwayDirectionLightSystemTimeSlice ns5:id=\"ALS01_TS01_75742039\">" +
            "<ns5:validTime><ns5:TimeInstant ns5:id=\"TimeInstantType_14_75742039\">" +
            "<ns5:timePosition>2025-02-21T22:50:10.789Z</ns5:timePosition></ns5:TimeInstant></ns5:validTime>" +
            "<ns9:interpretation>SNAPSHOT</ns9:interpretation><ns9:position>OTHER:RWSL</ns9:position>" +
            "<ns9:associatedRunwayDirection ns2:href=\"#RunwayDirection_1_75742039\"></ns9:associatedRunwayDirection>" +
            "</ns9:RunwayDirectionLightSystemTimeSlice></ns9:timeSlice><ns9:timeSlice>" +
            "<ns9:RunwayDirectionLightSystemTimeSlice ns5:id=\"ALS01_TS02_75742039\"><ns5:validTime>" +
            "<ns5:TimePeriod ns5:id=\"RunwayDirection_TS_TP_1_75742039\">" +
            "<ns5:beginPosition>2025-02-26T06:00:00.000Z</ns5:beginPosition>" +
            "<ns5:endPosition>2025-02-26T13:00:00.000Z</ns5:endPosition></ns5:TimePeriod></ns5:validTime>" +
            "<ns9:interpretation>TEMPDELTA</ns9:interpretation><ns9:availability>" +
            "<ns9:GroundLightingAvailability ns5:id=\"GLA01_75742039\">" +
            "<ns9:operationalStatus>UNSERVICEABLE</ns9:operationalStatus></ns9:GroundLightingAvailability></ns9:availability>" +
            "<ns9:extension><ns11:RunwayDirectionLightSystemExtension ns5:id=\"RDLSE_EVENT_1_75742039\">" +
            "<ns11:theEvent ns2:href=\"#Event_1_75742039\"></ns11:theEvent></ns11:RunwayDirectionLightSystemExtension>" +
            "</ns9:extension></ns9:RunwayDirectionLightSystemTimeSlice></ns9:timeSlice></ns9:RunwayDirectionLightSystem>" +
            "</ns13:hasMember><ns13:hasMember><ns9:RunwayDirection ns5:id=\"RWYDIR01_75742039\">" +
            "<ns5:boundedBy xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"></ns5:boundedBy>" +
            "<ns9:timeSlice><ns9:RunwayDirectionTimeSlice ns5:id=\"RWYDIR01_TS01_75742039\"><ns5:validTime>" +
            "<ns5:TimeInstant ns5:id=\"RWYDIR01_TS01_TI01_75742039\">" +
            "<ns5:timePosition>2025-02-21T22:50:10.789Z</ns5:timePosition></ns5:TimeInstant></ns5:validTime>" +
            "<ns9:interpretation>SNAPSHOT</ns9:interpretation><ns9:designator>07L</ns9:designator>" +
            "<ns9:usedRunway ns2:href=\"#RWY01_75742039\"></ns9:usedRunway><ns9:extension>" +
            "<ns11:RunwayDirectionExtension ns5:id=\"RDE_EVENT_1_75742039\">" +
            "<ns11:theEvent ns2:href=\"#Event_1_75742039\"></ns11:theEvent></ns11:RunwayDirectionExtension>" +
            "</ns9:extension></ns9:RunwayDirectionTimeSlice></ns9:timeSlice></ns9:RunwayDirection></ns13:hasMember>" +
            "<ns13:hasMember><ns9:RunwayDirectionLightSystem ns5:id=\"ALS02_75742039\">" +
            "<ns5:boundedBy xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"></ns5:boundedBy>" +
            "<ns9:timeSlice><ns9:RunwayDirectionLightSystemTimeSlice ns5:id=\"ALS02_TS02_75742039\"><ns5:validTime>" +
            "<ns5:TimeInstant ns5:id=\"TimeInstantType_24_75742039\">" +
            "<ns5:timePosition>2025-02-21T22:50:10.789Z</ns5:timePosition></ns5:TimeInstant></ns5:validTime>" +
            "<ns9:interpretation>SNAPSHOT</ns9:interpretation><ns9:position>OTHER:RWSL</ns9:position>" +
            "<ns9:associatedRunwayDirection ns2:href=\"#RunwayDirection_2_75742039\"></ns9:associatedRunwayDirection>" +
            "</ns9:RunwayDirectionLightSystemTimeSlice></ns9:timeSlice><ns9:timeSlice>" +
            "<ns9:RunwayDirectionLightSystemTimeSlice ns5:id=\"ALS01_TS12_75742039\"><ns5:validTime>" +
            "<ns5:TimePeriod ns5:id=\"RunwayDirection_TS_TP_11_75742039\">" +
            "<ns5:beginPosition>2025-02-26T06:00:00.000Z</ns5:beginPosition>" +
            "<ns5:endPosition>2025-02-26T13:00:00.000Z</ns5:endPosition></ns5:TimePeriod></ns5:validTime>" +
            "<ns9:interpretation>TEMPDELTA</ns9:interpretation><ns9:availability>" +
            "<ns9:GroundLightingAvailability ns5:id=\"GLA11_75742039\">" +
            "<ns9:operationalStatus>UNSERVICEABLE</ns9:operationalStatus></ns9:GroundLightingAvailability></ns9:availability>" +
            "<ns9:extension><ns11:RunwayDirectionLightSystemExtension ns5:id=\"RDLSE_EVENT_2_75742039\">" +
            "<ns11:theEvent ns2:href=\"#Event_1_75742039\"></ns11:theEvent></ns11:RunwayDirectionLightSystemExtension>" +
            "</ns9:extension></ns9:RunwayDirectionLightSystemTimeSlice></ns9:timeSlice></ns9:RunwayDirectionLightSystem>" +
            "</ns13:hasMember><ns13:hasMember><ns9:RunwayDirection ns5:id=\"RWYDIR02_75742039\">" +
            "<ns5:boundedBy xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"></ns5:boundedBy>" +
            "<ns9:timeSlice><ns9:RunwayDirectionTimeSlice ns5:id=\"RWYDIR01_TS11_75742039\"><ns5:validTime>" +
            "<ns5:TimeInstant ns5:id=\"RWYDIR01_TS01_TI11_75742039\">" +
            "<ns5:timePosition>2025-02-21T22:50:10.789Z</ns5:timePosition></ns5:TimeInstant></ns5:validTime>" +
            "<ns9:interpretation>SNAPSHOT</ns9:interpretation><ns9:designator>25R</ns9:designator>" +
            "<ns9:usedRunway ns2:href=\"#RWY01_75742039\"></ns9:usedRunway><ns9:extension>" +
            "<ns11:RunwayDirectionExtension ns5:id=\"RDE_EVENT_2_75742039\">" +
            "<ns11:theEvent ns2:href=\"#Event_1_75742039\"></ns11:theEvent></ns11:RunwayDirectionExtension>" +
            "</ns9:extension></ns9:RunwayDirectionTimeSlice></ns9:timeSlice></ns9:RunwayDirection></ns13:hasMember>" +
            "<ns13:hasMember><ns9:Runway ns5:id=\"RWY01_75742039\">" +
            "<ns5:boundedBy xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"></ns5:boundedBy>" +
            "<ns9:timeSlice><ns9:RunwayTimeSlice ns5:id=\"Runway_TS_1_75742039\"><ns5:validTime>" +
            "<ns5:TimeInstant ns5:id=\"Runway_TS_I_1_75742039\">" +
            "<ns5:timePosition>2025-02-21T22:50:10.789Z</ns5:timePosition></ns5:TimeInstant></ns5:validTime>" +
            "<ns9:interpretation>SNAPSHOT</ns9:interpretation><ns9:designator>07L/25R</ns9:designator>" +
            "<ns9:associatedAirportHeliport ns2:href=\"#Airport_1_75742039\"></ns9:associatedAirportHeliport>" +
            "<ns9:extension><ns11:RunwayExtension ns5:id=\"RE_EVENT_1_75742039\">" +
            "<ns11:theEvent ns2:href=\"#Event_1_75742039\"></ns11:theEvent></ns11:RunwayExtension></ns9:extension>" +
            "</ns9:RunwayTimeSlice></ns9:timeSlice></ns9:Runway></ns13:hasMember><ns13:hasMember>" +
            "<ns9:RunwayElement ns5:id=\"RE1_75742039\">" +
            "<ns5:boundedBy xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"></ns5:boundedBy>" +
            "<ns9:timeSlice><ns9:RunwayElementTimeSlice ns5:id=\"RE01_TS1_75742039\"><ns5:validTime>" +
            "<ns5:TimeInstant ns5:id=\"RE_TI1_75742039\">" +
            "<ns5:timePosition>2025-02-21T22:50:10.789Z</ns5:timePosition></ns5:TimeInstant></ns5:validTime>" +
            "<ns9:interpretation>SNAPSHOT</ns9:interpretation>" +
            "<ns9:associatedRunway ns2:href=\"#RWY01_75742039\"></ns9:associatedRunway><ns9:extent>" +
            "<ns9:ElevatedSurface srsName=\"urn:ogc:def:crs:EPSG::4326\" srsDimension=\"2\" ns5:id=\"ES1_75742039\">" +
            "<ns5:patches><ns5:PolygonPatch><ns5:exterior><ns5:LinearRing>" +
            "<ns5:posList>33.9357561888753 -118.42209515307201 33.9353473667015 -118.42203456941101 " +
            "33.9396734675464 -118.379747724488 33.9400823102528 -118.379808109106 33.9357561888753 " +
            "-118.42209515307201</ns5:posList></ns5:LinearRing></ns5:exterior></ns5:PolygonPatch></ns5:patches>" +
            "</ns9:ElevatedSurface></ns9:extent><ns9:extension>" +
            "<ns11:RunwayElementExtension ns5:id=\"REE_EVENT_1_75742039\">" +
            "<ns11:theEvent ns2:href=\"#Event_1_75742039\"></ns11:theEvent></ns11:RunwayElementExtension>" +
            "</ns9:extension></ns9:RunwayElementTimeSlice></ns9:timeSlice></ns9:RunwayElement></ns13:hasMember>" +
            "<ns13:hasMember><ns9:AirportHeliport ns5:id=\"Airport_1_75742039\">" +
            "<ns5:boundedBy xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"></ns5:boundedBy>" +
            "<ns9:timeSlice><ns9:AirportHeliportTimeSlice ns5:id=\"Airport_TS_1_75742039\"><ns5:validTime>" +
            "<ns5:TimeInstant ns5:id=\"Airport_TS_TI_1_75742039\">" +
            "<ns5:timePosition>2025-02-21T22:50:10.789Z</ns5:timePosition></ns5:TimeInstant></ns5:validTime>" +
            "<ns9:interpretation>SNAPSHOT</ns9:interpretation><ns9:designator>LAX</ns9:designator>" +
            "<ns9:name>LOS ANGELES INTL</ns9:name><ns9:locationIndicatorICAO>KLAX</ns9:locationIndicatorICAO>" +
            "<ns9:ARP><ns9:ElevatedPoint srsName=\"urn:ogc:def:crs:EPSG::4326\" srsDimension=\"2\" ns5:id=\"EP01_75742039\">" +
            "<ns5:pos>33.9425003055556 -118.408073638889</ns5:pos></ns9:ElevatedPoint></ns9:ARP><ns9:extension>" +
            "<ns11:AirportHeliportExtension ns5:id=\"AHE_EVENT_1_75742039\">" +
            "<ns11:theEvent ns2:href=\"#Event_1_75742039\"></ns11:theEvent></ns11:AirportHeliportExtension>" +
            "</ns9:extension></ns9:AirportHeliportTimeSlice></ns9:timeSlice></ns9:AirportHeliport></ns13:hasMember>" +
            "<ns13:hasMember><ns11:Event ns5:id=\"Event_1_75742039\">" +
            "<ns5:boundedBy xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:nil=\"true\"></ns5:boundedBy>" +
            "<ns11:timeSlice><ns11:EventTimeSlice ns5:id=\"Event_TS_1_75742039\"><ns5:validTime>" +
            "<ns5:TimePeriod ns5:id=\"Event_TS_TP_1_75742039\">" +
            "<ns5:beginPosition>2025-02-26T06:00:00.000Z</ns5:beginPosition>" +
            "<ns5:endPosition>2025-02-26T13:00:00.000Z</ns5:endPosition></ns5:TimePeriod></ns5:validTime>" +
            "<ns9:interpretation>BASELINE</ns9:interpretation><ns11:scenario>578</ns11:scenario><ns11:textNOTAM>" +
            "<ns11:NOTAM ns5:id=\"NOTAM_1_75742039\"><ns11:number>220</ns11:number><ns11:year>2025</ns11:year>" +
            "<ns11:type>N</ns11:type><ns11:issued>2025-02-21T22:43:00.000Z</ns11:issued>" +
            "<ns11:affectedFIR>ZLA</ns11:affectedFIR><ns11:minimumFL>000</ns11:minimumFL>" +
            "<ns11:maximumFL>999</ns11:maximumFL><ns11:radius>5</ns11:radius><ns11:location>LAX</ns11:location>" +
            "<ns11:effectiveStart>202502260600</ns11:effectiveStart><ns11:effectiveEnd>202502261300</ns11:effectiveEnd>" +
            "<ns11:text>RWY 07L/25R RWY STATUS LGT SYSTEM U/S</ns11:text><ns11:translation>" +
            "<ns11:NOTAMTranslation ns5:id=\"NT01_75742039\"><ns11:type>LOCAL_FORMAT</ns11:type>" +
            "<ns11:simpleText>!LAX 02/220 LAX RWY 07L/25R RWY STATUS LGT SYSTEM U/S 2502260600-2502261300</ns11:simpleText>" +
            "</ns11:NOTAMTranslation></ns11:translation><ns11:translation>" +
            "<ns11:NOTAMTranslation ns5:id=\"NT02_75742039\"><ns11:type>OTHER:ICAO</ns11:type><ns11:formattedText>" +
            "<html:div xmlns:html=\"http://www.w3.org/1999/xhtml\" xmlns=\"http://www.aixm.aero/schema/5.1/message\" " +
            "xmlns:aixm=\"http://www.aixm.aero/schema/5.1\" xmlns:event=\"http://www.aixm.aero/schema/5.1/event\" " +
            "xmlns:fns=\"urn:us.gov.dot.faa.aim.fns\" xmlns:fnse=\"http://www.aixm.aero/schema/5.1/extensions/FAA/FNSE\" " +
            "xmlns:gco=\"http://www.isotc211.org/2005/gco\" xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" " +
            "xmlns:gml=\"http://www.opengis.net/gml/3.2\" xmlns:gsr=\"http://www.isotc211.org/2005/gsr\" " +
            "xmlns:gss=\"http://www.isotc211.org/2005/gss\" xmlns:gts=\"http://www.isotc211.org/2005/gts\" " +
            "xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
            "02/220 NOTAMN \nQ) KZLA/QLIAS/IV/BO/A/000/999/3356N11824W005 \nA) KLAX \nB) 2502260600 \nC) 2502261300 \n" +
            "E) RWY 07L/25R RWY STATUS LGT SYSTEM \n U/S</html:div></ns11:formattedText></ns11:NOTAMTranslation>" +
            "</ns11:translation></ns11:NOTAM></ns11:textNOTAM><ns11:extension>" +
            "<ns6:EventExtension ns5:id=\"ext_01_75742039\"><ns6:classification>DOM</ns6:classification>" +
            "<ns6:accountId>LAX</ns6:accountId><ns6:xoveraccountID>KLAX</ns6:xoveraccountID>" +
            "<ns6:xovernotamID>A0840/25</ns6:xovernotamID><ns6:airportname>LOS ANGELES INTL</ns6:airportname>" +
            "<ns6:lastUpdated>2025-02-21T22:49:00.000Z</ns6:lastUpdated><ns6:icaoLocation>KLAX</ns6:icaoLocation>" +
            "</ns6:EventExtension></ns11:extension></ns11:EventTimeSlice></ns11:timeSlice></ns11:Event></ns13:hasMember>" +
            "</ns13:AIXMBasicMessage>";

        // FnsMessage testNotam = createFnsMessage(aixmNotamMessage);
        FnsMessage testNotam = new FnsMessage(-1l, aixmNotamMessage);
        testNotam.setStatus(FnsMessage.NotamStatus.ACTIVE);

        NotamBean notam = notamDb.putNotamJdbi(testNotam);

        // Get the last NOTAM from database
        List<NotamBean> notams = notamDb.getAll();
        
        // Verify the NOTAM was retrieved correctly
        assertNotNull(notam);
        assertEquals(1, notams.size());
        assertEquals("KLAX", notam.getIcaoLocation());
    }
} 
