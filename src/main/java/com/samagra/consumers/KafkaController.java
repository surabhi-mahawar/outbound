package com.samagra.consumers;

import com.samagra.adapter.provider.factory.ProviderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaController {

    private final Flux<ReceiverRecord<String, String>> reactiveKafkaReceiver;

    @Autowired
    private ProviderFactory factoryProvider;

    private WebClient webClient;

    private Mono<String> getEmpDetails(Integer id) {
        return WebClient
                .create("http://localhost:8181/test")
                .get()
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(s -> log.info("API Call Done" + id));
    }

    @EventListener(ApplicationStartedEvent.class)
    public void onMessage() {
        reactiveKafkaReceiver
            .doOnNext(new Consumer<ReceiverRecord<String, String>>() {
                @Override
                public void accept(ReceiverRecord<String, String> msg) {
                    log.info(msg.value());
                }
            })
            .doOnNext(new Consumer<ReceiverRecord<String, String>>() {
                @Override
                public void accept(ReceiverRecord<String, String> msg) {
//                    List<Integer> listEmpId = IntStream.rangeClosed(0, 50000)
//                            .boxed().collect(Collectors.toList());
//                    Flux<String> flux = Flux.fromIterable(listEmpId)
//                            .flatMap(new Function<Integer, Mono<String>>() {
//                                @Override
//                                public Mono<String> apply(Integer id) {
//                                    log.info("Calling API Now::" + id);
//                                    return getEmpDetails(id);
//                                }
//                            });
//
//                    flux.subscribe(s -> log.info("Done"));
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