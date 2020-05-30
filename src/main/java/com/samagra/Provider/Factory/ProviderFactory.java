package com.samagra.Provider.Factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ProviderFactory {

  @Autowired
  @Qualifier("gupshupWhatsappService")
  private IProvider gupshupWhatsapp;
  
  @Autowired
  @Qualifier("twilioWhatsappService")
  private IProvider twilioWhatsapp;

  public IProvider getProvider(String provider) {
    if (provider.equals("gupshup.whatsapp")) {
      return gupshupWhatsapp;
    }
    return null;
  }

}
