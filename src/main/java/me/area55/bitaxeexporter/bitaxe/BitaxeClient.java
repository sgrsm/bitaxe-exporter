package me.area55.bitaxeexporter.bitaxe;

import me.area55.bitaxeexporter.bitaxe.model.SystemInfo;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class BitaxeClient {

  private final WebClient webClient;

  public BitaxeClient(WebClient bitaxeWebClient) {
    this.webClient = bitaxeWebClient;
  }

  public Mono<@NonNull SystemInfo> getSystemInfo() {
    return webClient.get()
        .uri("/api/system/info")
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(SystemInfo.class);
  }
}
