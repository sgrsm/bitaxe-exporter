package me.area55.bitaxeexporter.bitaxe;

import jakarta.validation.constraints.NotNull;
import me.area55.bitaxeexporter.bitaxe.model.SystemInfo;
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

  @NotNull
  public Mono<SystemInfo> getSystemInfo() {
    return webClient.get()
        .uri("/api/system/info")
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(SystemInfo.class);
  }
}
