package me.area55.bitaxeexporter.metrics;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.StringJoiner;

import me.area55.bitaxeexporter.bitaxe.model.SharesRejectedReason;
import me.area55.bitaxeexporter.bitaxe.model.SystemInfo;
import org.springframework.stereotype.Component;

@Component
public class PrometheusMetricsFormatter {

  public String format(SystemInfo systemInfo) {
    StringBuilder sb = new StringBuilder(2048);
    // Global labels to attach where helpful
    String hostname = nv(systemInfo.getHostname());
    String mac = nv(systemInfo.getMacAddr());
    String ssid = nv(systemInfo.getSsid());

    // hashrate
    helpType(sb, "bitaxe_hashrate", "Current hashrate", "gauge");
    gauge(sb, "bitaxe_hashrate", systemInfo.getHashRate(), labels(
        label("hostname", hostname), label("mac", mac)));

    // expected hashrate not present in current schema

    // error percentage
    helpType(sb, "bitaxe_error_percentage", "Hash error percentage", "gauge");
    gauge(sb, "bitaxe_error_percentage", systemInfo.getErrorPercentage(), labels());

    // power & voltage & current
    helpType(sb, "bitaxe_power_watts", "Power consumption in watts", "gauge");
    gauge(sb, "bitaxe_power_watts", systemInfo.getPower(), labels());

    helpType(sb, "bitaxe_voltage_volts", "Input voltage", "gauge");
    gauge(sb, "bitaxe_voltage_volts", systemInfo.getVoltage(), labels());

    helpType(sb, "bitaxe_current_milliamps", "Current draw in milliamps", "gauge");
    gauge(sb, "bitaxe_current_milliamps", systemInfo.getCurrent(), labels());

    // temperatures
    helpType(sb, "bitaxe_temperature_celsius", "Chip temperature", "gauge");
    gauge(sb, "bitaxe_temperature_celsius", systemInfo.getTemp(), labels(label("sensor", "avg")));
    gauge(sb, "bitaxe_temperature_celsius", systemInfo.getTemp2(), labels(label("sensor", "avg2")));

    helpType(sb, "bitaxe_vr_temperature_celsius", "Voltage regulator temperature", "gauge");
    gauge(sb, "bitaxe_vr_temperature_celsius", systemInfo.getVrTemp(), labels());

    // fan
    helpType(sb, "bitaxe_fan_rpm", "Fan speed in RPM", "gauge");
    gauge(sb, "bitaxe_fan_rpm", systemInfo.getFanrpm(), labels());

    helpType(sb, "bitaxe_fan_speed_percent", "Fan speed percentage", "gauge");
    gauge(sb, "bitaxe_fan_speed_percent", systemInfo.getFanspeed(), labels());

    // shares
    helpType(sb, "bitaxe_shares_accepted_total", "Accepted shares", "counter");
    gaugeAsCounter(sb, "bitaxe_shares_accepted_total", systemInfo.getSharesAccepted(), labels());

    helpType(sb, "bitaxe_shares_rejected_total", "Rejected shares", "counter");
    gaugeAsCounter(sb, "bitaxe_shares_rejected_total", systemInfo.getSharesRejected(), labels());

    // rejected reasons
    helpType(sb, "bitaxe_shares_rejected_reason_total", "Rejected shares by reason", "counter");
    if (systemInfo.getSharesRejectedReasons() != null) {
      for (SharesRejectedReason r : systemInfo.getSharesRejectedReasons()) {
        if (r == null) continue;
        BigDecimal count = r.getCount() == null ? null : BigDecimal.valueOf(r.getCount());
        gaugeAsCounter(sb, "bitaxe_shares_rejected_reason_total", count,
            labels(label("reason", nv(r.getMessage()))));
      }
    }

    // pool & stratum
    helpType(sb, "bitaxe_pool_difficulty", "Current pool difficulty", "gauge");
    gauge(sb, "bitaxe_pool_difficulty", systemInfo.getPoolDifficulty(), labels());

    helpType(sb, "bitaxe_network_difficulty", "Network difficulty", "gauge");
    gauge(sb, "bitaxe_network_difficulty", systemInfo.getNetworkDifficulty(), labels());

    // wifi
    helpType(sb, "bitaxe_wifi_rssi_dbm", "WiFi RSSI", "gauge");
    gauge(sb, "bitaxe_wifi_rssi_dbm", systemInfo.getWifiRSSI(), labels(label("ssid", ssid)));

    // uptime
    helpType(sb, "bitaxe_uptime_seconds", "Uptime in seconds", "counter");
    gaugeAsCounter(sb, "bitaxe_uptime_seconds", systemInfo.getUptimeSeconds(), labels());

    // frequency
    helpType(sb, "bitaxe_frequency_mhz", "ASIC frequency in MHz", "gauge");
    gauge(sb, "bitaxe_frequency_mhz", systemInfo.getFrequency(), labels());

    // memory
    helpType(sb, "bitaxe_free_heap_bytes", "Free heap bytes", "gauge");
    gauge(sb, "bitaxe_free_heap_bytes", systemInfo.getFreeHeap(), labels(label("type", "total")));
    gauge(sb, "bitaxe_free_heap_bytes", systemInfo.getFreeHeapInternal(), labels(label("type", "internal")));
    gauge(sb, "bitaxe_free_heap_bytes", systemInfo.getFreeHeapSpiram(), labels(label("type", "spiram")));

    // power fault (as info metric with label)
    helpType(sb, "bitaxe_power_fault_info", "Power fault info (1 if present)", "gauge");
    BigDecimal pf = (systemInfo.getPowerFault() == null || systemInfo.getPowerFault().isBlank()) ? BigDecimal.ZERO : BigDecimal.ONE;
    gauge(sb, "bitaxe_power_fault_info", pf, labels(label("fault", nv(systemInfo.getPowerFault()))));

    return sb.toString();
  }

  private static String nv(String v) {
    return v == null ? "" : v;
  }

  private static String label(String k, String v) {
    return k + "=\"" + escape(v) + "\"";
  }

  private static String labels(String... kvs) {
    if (kvs == null || kvs.length == 0) return "";
    StringJoiner j = new StringJoiner(",", "{", "}");
    for (String kv : kvs) {
      if (kv == null || kv.isBlank()) continue;
      j.add(kv);
    }
    String s = j.toString();
    return s.equals("{}") ? "" : s;
  }

  private static void helpType(StringBuilder sb, String metric, String help, String type) {
    sb.append("# HELP ").append(metric).append(' ').append(escape(help)).append('\n');
    sb.append("# TYPE ").append(metric).append(' ').append(type).append('\n');
  }

  private static void gauge(StringBuilder sb, String metric, BigDecimal value, String labels) {
    if (value == null) return;
    sb.append(metric);
    if (!labels.isEmpty()) sb.append(labels);
    sb.append(' ').append(format(value)).append('\n');
  }

  private static void gaugeAsCounter(StringBuilder sb, String metric, BigDecimal value, String labels) {
    gauge(sb, metric, value, labels);
  }

  private static String escape(String s) {
    if (s == null) return "";
    return s.replace("\\", "\\\\").replace("\n", "\\n").replace("\"", "\\\"");
  }

  private static String format(BigDecimal d) {
    return String.format(Locale.ROOT, "%s", d.stripTrailingZeros().toPlainString());
  }
}
