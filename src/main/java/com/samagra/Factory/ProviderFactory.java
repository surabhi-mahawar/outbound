package com.samagra.Factory;

import org.springframework.stereotype.Component;
import com.samagra.Service.GupsShupWhatsappProviderService;

@Component
public class ProviderFactory {

  public static IProvider getProvider(String provider) {
    if (provider.equals("gupshup.whatsapp")) {
      return (IProvider) new GupsShupWhatsappProviderService();
    }
    return null;
  }

}
