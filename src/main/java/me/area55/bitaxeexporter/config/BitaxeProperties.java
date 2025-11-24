package me.area55.bitaxeexporter.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "bitaxe")
public record BitaxeProperties(List<BitaxeInstance> instances) {

  public record BitaxeInstance(String id, String baseUrl) {}
}
