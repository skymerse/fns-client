package us.dot.faa.swim.fns.notamdb;

import java.io.StringReader;
import java.sql.Timestamp;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Unmarshaller;

import aero.aixm.message.AIXMBasicMessageType;

public class NotamBean {
    private Integer fnsid;
    private Long correlationid;
    private Timestamp issuedtimestamp;
    private Timestamp storedtimestamp;
    private Timestamp updatedtimestamp;
    private Timestamp validfromtimestamp;
    private Timestamp validtotimestamp;
    private String classification;
    private String locationdesignator;
    private String notamaccountability;
    private String notamtext;
    private String aixmnotammessage;
    private String status;
    private AIXMBasicMessageType aixmNotamMessageObject;

    // Default constructor
    public NotamBean() {
    }

    // Getters and Setters
    public Integer getFnsid() {
        return fnsid;
    }

    public void setFnsid(Integer fnsid) {
        this.fnsid = fnsid;
    }

    public Long getCorrelationid() {
        return correlationid;
    }

    public void setCorrelationid(Long correlationid) {
        this.correlationid = correlationid;
    }

    public Timestamp getIssuedtimestamp() {
        return issuedtimestamp;
    }

    public void setIssuedtimestamp(Timestamp issuedtimestamp) {
        this.issuedtimestamp = issuedtimestamp;
    }

    public Timestamp getStoredtimestamp() {
        return storedtimestamp;
    }

    public void setStoredtimestamp(Timestamp storedtimestamp) {
        this.storedtimestamp = storedtimestamp;
    }

    public Timestamp getUpdatedtimestamp() {
        return updatedtimestamp;
    }

    public void setUpdatedtimestamp(Timestamp updatedtimestamp) {
        this.updatedtimestamp = updatedtimestamp;
    }

    public Timestamp getValidfromtimestamp() {
        return validfromtimestamp;
    }

    public void setValidfromtimestamp(Timestamp validfromtimestamp) {
        this.validfromtimestamp = validfromtimestamp;
    }

    public Timestamp getValidtotimestamp() {
        return validtotimestamp;
    }

    public void setValidtotimestamp(Timestamp validtotimestamp) {
        this.validtotimestamp = validtotimestamp;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public String getLocationdesignator() {
        return locationdesignator;
    }

    public void setLocationdesignator(String locationdesignator) {
        this.locationdesignator = locationdesignator;
    }

    public String getNotamaccountability() {
        return notamaccountability;
    }

    public void setNotamaccountability(String notamaccountability) {
        this.notamaccountability = notamaccountability;
    }

    public String getNotamtext() {
        return notamtext;
    }

    public void setNotamtext(String notamtext) {
        this.notamtext = notamtext;
    }

    public String getAixmnotammessage() {
        return aixmnotammessage;
    }

    public void setAixmnotammessage(String aixmnotammessage) {
        this.aixmnotammessage = aixmnotammessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public AIXMBasicMessageType getAixmNotamMessageObject() throws JAXBException {
        if (aixmNotamMessageObject != null) {
            return aixmNotamMessageObject;
        }


        JAXBContext jaxbContext = JAXBContext.newInstance(AIXMBasicMessageType.class);
        // Unmarshaller jaxbContext.createUnmarshaller();
        StringReader reader = new StringReader(aixmnotammessage);
        AIXMBasicMessageType fnsAixmMessageType = null;

        try {
            fnsAixmMessageType = (AIXMBasicMessageType) JAXBIntrospector
                    .getValue(jaxbContext.createUnmarshaller().unmarshal(reader));
        } catch (JAXBException e) {
            throw e;
        } finally {
            reader.close();
        }

        this.aixmNotamMessageObject = fnsAixmMessageType;
        return fnsAixmMessageType;
    }
}
