package me.area55.bitaxeexporter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  @Bean
  public WebClient bitaxeWebClient(BitaxeProperties props) {
    // Increase buffer in case the device returns larger payloads in future
    ExchangeStrategies strategies = ExchangeStrategies.builder()
        .codecs(c -> c.defaultCodecs().maxInMemorySize(512 * 1024))
        .build();
    return WebClient.builder()
        .baseUrl(props.getBaseUrl())
        .exchangeStrategies(strategies)
        .build();
  }
}
