package Request;

import javax.xml.bind.annotation.XmlRootElement;
import com.fasterxml.jackson.annotation.JsonProperty;
import Enums.MessageType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlRootElement
public class MessageRequest {
  private String app;
  private Long timestamp;
  private int version;
  @JsonProperty
  private String type;
  @JsonProperty
  private Payload payload;
}
