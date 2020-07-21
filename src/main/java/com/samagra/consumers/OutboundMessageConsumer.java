package com.samagra.consumers;

import com.samagra.adapter.gs.whatsapp.GupShupWhatsappAdapter;
import com.samagra.adapter.provider.factory.IProvider;
import com.samagra.adapter.provider.factory.ProviderFactory;
import messagerosa.core.model.XMessage;
import messagerosa.xml.XMessageParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.util.List;

@Slf4j
@Service
@RestController
public class OutboundMessageConsumer {

    @Autowired
    private ProviderFactory factoryProvider;

    @KafkaListener(id = "gsbmb", topics = "outbound")
    public void consumeMessage(List<String> listNextXmsg) throws Exception {
        log.info("size of list {}", listNextXmsg.size());
        for (String nextXmsg : listNextXmsg) {
            XMessage currentXmsg = XMessageParser.parse(new ByteArrayInputStream(nextXmsg.getBytes()));

            String channel = currentXmsg.getChannelURI();
            String provider = currentXmsg.getProviderURI();

            log.info("next msg {}", currentXmsg.getPayload().getText());
            IProvider iprovider = factoryProvider.getProvider(provider, channel);
            iprovider.processInBoundMessage(currentXmsg);
        }
    }
}
