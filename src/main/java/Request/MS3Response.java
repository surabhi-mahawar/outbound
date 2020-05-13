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
public class MS3Response {
  private boolean isLastResponse;
  private UserState userState;
  private MessageRequest messageRequest;
}
