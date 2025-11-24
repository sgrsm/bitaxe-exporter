package me.area55.bitaxeexporter.web;

import me.area55.bitaxeexporter.bitaxe.BitaxeClient;
import me.area55.bitaxeexporter.metrics.PrometheusMetricsFormatter;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping
public class BitaxeController {

  private static final MediaType PROM_CONTENT = MediaType.parseMediaType("text/plain; version=0.0.4; charset=utf-8");

  private final BitaxeClient client;
  private final PrometheusMetricsFormatter formatter;

  public BitaxeController(BitaxeClient client, PrometheusMetricsFormatter formatter) {
    this.client = client;
    this.formatter = formatter;
  }

  @GetMapping(value = "/bitaxe/{id}", produces = "text/plain; version=0.0.4; charset=utf-8")
  public Mono<@NonNull ResponseEntity<@NonNull String>> metrics(@PathVariable String id) {
    return client.getSystemInfo(id)
        .map(formatter::format)
        .map(body -> ResponseEntity.ok().contentType(PROM_CONTENT).body(body));
  }
}
