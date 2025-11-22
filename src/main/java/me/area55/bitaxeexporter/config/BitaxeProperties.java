package me.area55.bitaxeexporter.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "bitaxe")
public class BitaxeProperties {

  /** Base URL of the Bitaxe device, e.g. http://192.168.1.10 */
  @NotBlank
  private String baseUrl = "http://192.168.1.10";

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }
}
