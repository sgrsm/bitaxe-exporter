package me.area55.bitaxeexporter.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableConfigurationProperties(BitaxeProperties.class)
public class WebClientConfig {

  @Bean
  public WebClient bitaxeWebClient(BitaxeProperties props) {
    // Increase buffer in case the device returns larger payloads in future
    var strategies = ExchangeStrategies.builder()
        .codecs(c -> c.defaultCodecs().maxInMemorySize(512 * 1024))
        .build();
    return WebClient.builder()
        .baseUrl(props.baseUrl())
        .exchangeStrategies(strategies)
        .build();
  }
}
