package com.example.demo.order;

import io.zeebe.client.ZeebeClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository repository;

    private final ZeebeClient client;

    @Autowired
    public OrderController(final OrderRepository repository,
                           final ZeebeClient client) {
        this.repository = repository;
        this.client = client;
    }

    @GetMapping
    public Flux<Order> list() {
        return this.repository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Order> fetchOne(@PathVariable final long id) {
        return this.repository.findById(id);
    }

    @PostMapping
    public Mono<Order> create(@RequestBody Order payload) {
        final Mono<Order> result = this.repository.save(payload);

        return result.flatMap(o -> {
            this.client.newCreateInstanceCommand()
                    .bpmnProcessId("order-process")
                    .latestVersion()
                    .variables(o)
                    .send()
                    .join();
            return Mono.just(o);
        });
    }

    @PutMapping("/{id}")
    public Mono<Order> update(@PathVariable final long id) {
        return this.repository.findById(id)
                .flatMap(o -> {
                    o.setStatus("payment-received");
                    return this.repository.save(o);
                }).flatMap(o -> {
            this.client.newPublishMessageCommand()
                    .messageName("payment-received")
                    .correlationKey(o.getId().toString())
                    .timeToLive(Duration.ofSeconds(10))
                    .messageId(UUID.randomUUID().toString())
                    .variables(o)
                    .send()
                    .join();
            return Mono.just(o);
        });
    }
}
