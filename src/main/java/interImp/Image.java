package interImp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Image implements TypeInterface {
  public String url;
  public String urlExpiry;
  public String caption;
}
