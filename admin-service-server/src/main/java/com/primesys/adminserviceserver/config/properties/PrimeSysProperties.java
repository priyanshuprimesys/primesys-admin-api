package com.primesys.adminserviceserver.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Data
@RefreshScope
@Configuration
@ConfigurationProperties("primesys.device")
public class PrimeSysProperties {
    private String socketUrl;
    private String distUnit;
}
