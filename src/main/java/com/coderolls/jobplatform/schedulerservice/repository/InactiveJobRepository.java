package com.coderolls.jobplatform.schedulerservice.repository;

import com.coderolls.jobplatform.schedulerservice.domain.InactiveJob;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface InactiveJobRepository extends MongoRepository<InactiveJob, String> {
    boolean existsByJobName(String jobName);
    Optional<InactiveJob> findByJobName(String jobName);
}