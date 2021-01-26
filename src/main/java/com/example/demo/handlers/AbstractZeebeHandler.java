package com.example.demo.handlers;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.api.worker.JobClient;

public interface AbstractZeebeHandler {

    void handleJob(JobClient client, ActivatedJob job);
}
