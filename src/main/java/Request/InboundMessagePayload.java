package Request;

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
public class InboundMessagePayload {
  private String text;
  private String url;
  private String caption;
  private String urlExpiry;
  private String longitude;
  private String latitude;
  private Long ts;
  private String reason;
  private Long code;
  private String whatsappMessageId;
  private String type;
  private String phone;
}
