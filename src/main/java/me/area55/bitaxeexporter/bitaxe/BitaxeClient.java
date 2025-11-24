package me.area55.bitaxeexporter.bitaxe;

import java.util.Map;
import me.area55.bitaxeexporter.bitaxe.model.SystemInfo;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class BitaxeClient {

  private final Map<String, WebClient> clients;

  public BitaxeClient(Map<String, WebClient> bitaxeWebClients) {
    this.clients = bitaxeWebClients;
  }

  public Mono<@NonNull SystemInfo> getSystemInfo(@NonNull String id) {
    var client = clients.get(id);
    if (client == null) {
      return Mono.error(new IllegalArgumentException("Unknown bitaxe id: " + id));
    }

    return client.get()
        .uri("/api/system/info")
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(SystemInfo.class);
  }
}
