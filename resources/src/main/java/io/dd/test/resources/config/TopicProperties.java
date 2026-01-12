package io.dd.test.resources.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties("app.kafka.topics.resources-command")
public class TopicProperties {
    private String name;
    private Integer partitions;
    private Integer replicas;
    private String minInSyncReplicas;
}