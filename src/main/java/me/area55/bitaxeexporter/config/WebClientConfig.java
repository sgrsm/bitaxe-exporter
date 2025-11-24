package me.area55.bitaxeexporter.config;

import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(BitaxeProperties.class)
public class WebClientConfig {

  @Bean
  public Map<String, WebClient> bitaxeWebClients(BitaxeProperties props) {
    return props.instances().stream()
        .collect(Collectors.toUnmodifiableMap(BitaxeProperties.BitaxeInstance::id,
            instance -> WebClient.builder()
                .baseUrl(instance.baseUrl())
                .build()));
  }
}
