package me.area55.bitaxeexporter;

import me.area55.bitaxeexporter.config.BitaxeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BitaxeExporterApplication {

  private static final Logger log = LoggerFactory.getLogger(BitaxeExporterApplication.class);

  static void main(String[] args) {
    SpringApplication.run(BitaxeExporterApplication.class, args);
  }

  @Bean
  public ApplicationRunner applicationRunner(BitaxeProperties props) {
    return _ -> {
      log.info("Observing {} Bitaxe instances:", props.instances().size());
      props.instances()
          .forEach(instanceInfo -> log.info("{} -> ID: {}; ipv4: {}",
              props.instances().indexOf(instanceInfo) + 1,
              instanceInfo.id(),
              instanceInfo.baseUrl()));
    };
  }
}
