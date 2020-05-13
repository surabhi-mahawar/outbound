package Request;

import com.fasterxml.jackson.annotation.JsonProperty;
import Enums.PayLoadType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PayloadResponse {
  private String id;
  private String source;
  @JsonProperty
  private PayLoadType type;
  @JsonProperty
  private InboundMessagePayload payload;
  @JsonProperty
  private Sender sender;
  private String phone;
}
