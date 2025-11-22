package me.area55.bitaxeexporter.config;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "bitaxe")
public record BitaxeProperties(@NonNull String baseUrl) {

}
