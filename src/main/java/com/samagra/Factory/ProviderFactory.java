package com.samagra.Factory;

import com.samagra.Service.GupsShupWhatsappProviderService;

public class ProviderFactory {

  public static Provider getProvider(String provider) {
    if (provider.equals("gupshup.whatsapp")) {
      return new GupsShupWhatsappProviderService();
    }
    return null;
  }

}
