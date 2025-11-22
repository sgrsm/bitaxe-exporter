package me.area55.bitaxeexporter.web;

import me.area55.bitaxeexporter.bitaxe.BitaxeClient;
import me.area55.bitaxeexporter.metrics.PrometheusMetricsFormatter;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping
public class BitaxeController {

  private static final MediaType PROMETHEUS_TEXT = MediaType.parseMediaType("text/plain; version=0.0.4; charset=utf-8");

  private final BitaxeClient client;
  private final PrometheusMetricsFormatter formatter;

  public BitaxeController(BitaxeClient client, PrometheusMetricsFormatter formatter) {
    this.client = client;
    this.formatter = formatter;
  }

  @GetMapping(value = "/bitaxe", produces = "text/plain; version=0.0.4; charset=utf-8")
  public Mono<@NonNull ResponseEntity<@NonNull String>> metrics() {
    return client.getSystemInfo()
        .map(formatter::format)
        .map(body -> ResponseEntity.ok().contentType(PROMETHEUS_TEXT).body(body));
  }
}
