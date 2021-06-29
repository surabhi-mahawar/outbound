package com.samagra.consumers;

import com.samagra.adapter.provider.factory.IProvider;
import com.samagra.adapter.provider.factory.ProviderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import messagerosa.core.model.XMessage;
import messagerosa.dao.XMessageDAO;
import messagerosa.dao.XMessageDAOUtills;
import messagerosa.dao.XMessageRepo;
import messagerosa.xml.XMessageParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboundKafkaController {

    private final Flux<ReceiverRecord<String, String>> reactiveKafkaReceiver;

    @Autowired
    private ProviderFactory factoryProvider;

    @Autowired
    private XMessageRepo xMessageRepo;

    @EventListener(ApplicationStartedEvent.class)
    public void onMessage() {
        reactiveKafkaReceiver
                .doOnNext(new Consumer<ReceiverRecord<String, String>>() {
                    @Override
                    public void accept(ReceiverRecord<String, String> msg) {
                        log.info("Outbound Message rec: {}", msg);
                        XMessage currentXmsg = null;
                        try {
                            log.debug(msg.value());
                            currentXmsg = XMessageParser.parse(new ByteArrayInputStream(msg.value().getBytes()));
                            String channel = currentXmsg.getChannelURI();
                            String provider = currentXmsg.getProviderURI();
                            log.info("next msg {}", currentXmsg.getPayload().getText());
                            IProvider iprovider = factoryProvider.getProvider(provider, channel);
                            iprovider.processOutBoundMessageF(currentXmsg).subscribe(new Consumer<XMessage>() {
                                @Override
                                public void accept(XMessage xMessage) {
                                    XMessageDAO dao = XMessageDAOUtills.convertXMessageToDAO(xMessage);
                                    xMessageRepo.save(dao);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable e) {
                        System.out.println(e.getMessage());
                        log.error("KafkaFlux exception", e);
                    }
                })
                .subscribe();
    }
}