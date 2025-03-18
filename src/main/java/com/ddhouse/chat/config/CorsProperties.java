package com.ddhouse.chat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "custom.host")
public class CorsProperties {

    private List<String> client;

    public List<String> getClient() {
        return client;
    }

    public void setClient(List<String> client) {
        this.client = client;
    }
}
