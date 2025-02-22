package us.dot.faa.swim.fns.rest.data;

import us.dot.faa.swim.fns.notamdb.NotamBean;
import org.json.JSONObject;
import org.json.XML;



public class NotamDTO {
    private String message;
    private String status;
    private String location;
    private transient String aixmNotamMessage;
    private JSONObject aixmJson;

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

    public String getLocation() {
        return location;
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

    public static NotamDTO fromNotamBean(NotamBean notamBean) {
        return NotamDTO.builder()
                .message(notamBean.getNotamtext())
                .status(notamBean.getStatus())
                .location(notamBean.getLocationdesignator())
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

        public Builder aixmNotamMessage(String aixmNotamMessage) {
            notamDTO.setAixmNotamMessage(aixmNotamMessage);
            return this;
        }

        public NotamDTO build() {
            if (notamDTO.aixmNotamMessage != null) {
                JSONObject json = XML.toJSONObject(notamDTO.aixmNotamMessage);
                notamDTO.aixmJson = json;
            }


            return notamDTO;
        }
    }
}
