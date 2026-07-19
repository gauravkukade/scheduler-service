package com.coderolls.jobplatform.schedulerservice.client;

import com.coderolls.jobplatform.schedulerservice.domain.JobInstance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.json.JsonMapper;


@Slf4j
@Component
public class ProcessorClient {

    private final JsonMapper jsonMapper;
    private final String baseUrl;
    private final RestClient restClient;

    public ProcessorClient(JsonMapper jsonMapper, @Value("${processor.base-url}") String baseUrl) {
        this.jsonMapper = jsonMapper;
        this.baseUrl = baseUrl;
        this.restClient = RestClient.create(baseUrl);
    }

    public boolean dispatch(JobInstance instance) {

        log.info("Dispatching to Processor [{}]: {}",
                baseUrl + "/api/processor/execute",
                jsonMapper.writeValueAsString(instance));

        try {
            restClient.post()
                    .uri("/api/processor/execute")
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(instance)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.warn("Processor unavailable for instance {}: {}", instance.getInstanceId(), e.getMessage());
            return false;
        }
    }
}