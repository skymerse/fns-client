package us.dot.faa.swim.fns.notamdb;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
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
        if (!notamDb.notamTableExists()) {
            notamDb.createNotamTable();
        }
    }

    @BeforeEach
    public void setUpEach() throws Exception {
        try (Connection conn = notamDb.getDBConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM NOTAMS");
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new Exception("Error deleting NOTAMS", e);
        }
    }

    private FnsMessage createFnsMessage() throws JAXBException {
        int fnsId = 12345;
        long correlationId = 67890L;
        Timestamp now = Timestamp.from(Instant.now());
        Timestamp future = Timestamp.from(Instant.now().plusSeconds(3600)); // Valid for 1 hour
        String locationDesignator = "KLAX";
        String classification = "TEST";
        String notamAccountability = "TEST123";
        String notamText = "Test NOTAM message";
        String aixmNotamMessage = "<test>Sample AIXM message</test>";

        // Create a test NOTAM message using the constructor with all fields
        FnsMessage testNotam = new FnsMessage(
            fnsId,
            correlationId,
            now,  // issuedTimestamp
            now,  // updatedTimestamp
            now,  // validFromTimestamp
            future,  // validToTimestamp
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
        notamDb.putNotam(testNotam);

        // Verify the NOTAM was stored by checking if it exists
        try (Connection conn = notamDb.getDBConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM NOTAMS WHERE fnsid = ?");
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

        notamDb.putNotam(testNotam);
        notamDb.putNotam(testNotam);
    }
} 