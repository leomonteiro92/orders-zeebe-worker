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
public class ShipWithoutInsuranceHandler implements AbstractZeebeHandler {

    private final OrderRepository repository;

    public ShipWithoutInsuranceHandler(final OrderRepository repository) {
        this.repository = repository;
    }

    @ZeebeWorker(type = "ship-without-insurance")
    @Override
    public void handleJob(final JobClient client, final ActivatedJob job) {
        log.info("ship without insurance: " + job.getVariables());
        final Order order = job.getVariablesAsType(Order.class);
        order.setInsurance(false);
        this.repository.save(order).subscribe(o -> {
            client.newCompleteCommand(job.getKey())
                    .variables(o)
                    .send()
                    .join();
        }, err -> {
            log.error(err.getLocalizedMessage());
        }, () -> {
            log.info("Successfully shiped without insurance");
        });
    }
}
