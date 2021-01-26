package com.example.demo.handlers;

import com.example.demo.order.Order;
import com.example.demo.order.OrderRepository;
import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;
import io.zeebe.spring.client.annotation.ZeebeWorker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InitiatePaymentHandler implements AbstractZeebeHandler {

    private final OrderRepository repository;

    public InitiatePaymentHandler(final OrderRepository repository) {
        this.repository = repository;
    }

    @Override
    @ZeebeWorker(type = "initiate-payment")
    public void handleJob(final JobClient client, final ActivatedJob job) {
        log.info("initiate payment: " + job.getVariables());
        final Order order = job.getVariablesAsType(Order.class);
        order.setStatus("payment-started");
        this.repository.save(order).subscribe(o -> {
            client.newCompleteCommand(job.getKey())
                    .variables(order)
                    .send()
                    .join();
        }, err -> {
            log.error(err.getLocalizedMessage());
        }, () -> log.info("Successfully initiated payment"));
    }
}
