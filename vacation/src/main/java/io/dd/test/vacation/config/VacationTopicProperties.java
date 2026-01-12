package io.dd.test.vacation.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("app.kafka.topics.vacation-command")
public class VacationTopicProperties {
    private String name;
    private Integer partitions;
    private Integer replicas;
    private String minInSyncReplicas;
}