package me.area55.bitaxeexporter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import me.area55.bitaxeexporter.config.BitaxeProperties;

@SpringBootApplication
@EnableConfigurationProperties(BitaxeProperties.class)
public class BitaxeExporterApplication {

  public static void main(String[] args) {
    SpringApplication.run(BitaxeExporterApplication.class, args);
  }

}
