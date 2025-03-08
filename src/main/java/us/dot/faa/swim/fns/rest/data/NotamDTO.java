package us.dot.faa.swim.fns.rest.data;

import us.dot.faa.swim.fns.notamdb.NotamBean;
import us.dot.faa.swim.fns.notamdb.NotamUtils;

import com.google.gson.annotations.SerializedName;



public class NotamDTO {
    private String message;
    private String status;
    private String location;
    @SerializedName("icao_location")
    private String icaoLocation;
    private transient String aixmNotamMessage;
    private String icaoMessage;
    @SerializedName("notam_series")
    private String notamSeries;
    @SerializedName("notam_number")
    private long notamNumber;
    @SerializedName("notam_year")
    private String notamYear;
    private String classification;

    private NotamDTO() {}

    public static Builder builder() {
        return new Builder();
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public String getIcaoMessage() {
        return icaoMessage;
    }

    public void setIcaoMessage(String icaoMessage) {
        this.icaoMessage = icaoMessage;
    }

    public String getLocation() {
        return location;
    }

    public String getIcaoLocation() {
        return icaoLocation;
    }

    public void setIcaoLocation(String icaoLocation) {
        this.icaoLocation = icaoLocation;
    }

    public String getNotamSeries() {
        return notamSeries;
    }

    public long getNotamNumber() {
        return notamNumber;
    }

    public String getNotamYear() {
        return notamYear;
    }

    public void setNotamYear(String notamYear) {
        this.notamYear = notamYear;
    }

    public void setNotamSeries(String notamSeries) {
        this.notamSeries = notamSeries;
    }

    public void setNotamNumber(long notamNumber) {
        this.notamNumber = notamNumber;
    }

    public String getAixmNotamMessage() {
        return aixmNotamMessage;
    }

    private void setMessage(String message) {
        this.message = message;
    }

    private void setStatus(String status) {
        this.status = status;
    }

    private void setLocation(String location) {
        this.location = location;
    }

    private void setAixmNotamMessage(String aixmNotamMessage) {
        this.aixmNotamMessage = aixmNotamMessage;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    public static NotamDTO fromNotamBean(NotamBean notamBean) {
        return NotamDTO.builder()
                .message(notamBean.getNotamtext())
                .status(notamBean.getStatus())
                .location(notamBean.getLocationdesignator())
                .icaoLocation(notamBean.getIcaoLocation())
                .notamSeries(notamBean.getNotamSeries())
                .notamNumber(notamBean.getNotamNumber())
                .notamYear(notamBean.getNotamYear())
                .aixmNotamMessage(notamBean.getAixmnotammessage())
                .build();
    }

    public static class Builder {
        private final NotamDTO notamDTO;

        private Builder() {
            notamDTO = new NotamDTO();
        }

        public Builder message(String message) {
            notamDTO.setMessage(message);
            return this;
        }

        public Builder status(String status) {
            notamDTO.setStatus(status);
            return this;
        }

        public Builder location(String location) {
            notamDTO.setLocation(location);
            return this;
        }

        public Builder icaoLocation(String icaoLocation) {
            notamDTO.setIcaoLocation(icaoLocation);
            return this;
        }

        public Builder notamSeries(String notamSeries) {
            notamDTO.setNotamSeries(notamSeries);
            return this;
        }

        public Builder notamNumber(long notamNumber) {
            notamDTO.setNotamNumber(notamNumber);
            return this;
        }

        public Builder notamYear(String notamYear) {
            notamDTO.setNotamYear(notamYear);
            return this;
        }

        public Builder aixmNotamMessage(String aixmNotamMessage) {
            notamDTO.setAixmNotamMessage(aixmNotamMessage);
            return this;
        }

        public NotamDTO build() {
            if (notamDTO.aixmNotamMessage != null) {
                // TODO(pasierb): Extract it in FnsMessage
                notamDTO.icaoMessage = NotamUtils.extractIcaoMessage(notamDTO.aixmNotamMessage);
            }

            return notamDTO;
        }
    }
}
