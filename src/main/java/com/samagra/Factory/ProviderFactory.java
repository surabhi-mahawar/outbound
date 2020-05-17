package com.samagra.Factory;

import com.samagra.Service.GupsShupWhatsappProviderService;

public class ProviderFactory {

  public static IProvider getProvider(String provider) {
    if (provider.equals("gupshup.whatsapp")) {
      return (IProvider) new GupsShupWhatsappProviderService();
    }
    return null;
  }

}
