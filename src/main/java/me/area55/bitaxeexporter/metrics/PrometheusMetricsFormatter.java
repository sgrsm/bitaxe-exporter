package me.area55.bitaxeexporter.metrics;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.area55.bitaxeexporter.bitaxe.model.SharesRejectedReason;
import me.area55.bitaxeexporter.bitaxe.model.SystemInfo;
import org.springframework.stereotype.Component;

@Component
public class PrometheusMetricsFormatter {

  public String format(SystemInfo systemInfo) {
    var sb = new StringBuilder(2048);
    // Global labels to attach where helpful
    var hostname = nv(systemInfo.getHostname());
    var mac = nv(systemInfo.getMacAddr());
    var ssid = nv(systemInfo.getSsid());

    // hashrate
    helpType(sb, "bitaxe_hashrate", "Current hashrate", "gauge");
    gauge(sb, "bitaxe_hashrate", systemInfo.getHashRate(), labels(
        label("hostname", hostname), label("mac", mac)));

    // expected hashrate not present in current schema

    // best difficulties (source is a human-readable string with suffix K/M/G/T)
    // We normalize to a raw difficulty number (unitless) by applying the multiplier.
    helpType(sb, "bitaxe_best_difficulty", "Best difficulty achieved (normalized; K=1e3,M=1e6,G=1e9,T=1e12)", "gauge");
    gauge(sb, "bitaxe_best_difficulty", parseMagnitudeNumber(systemInfo.getBestDiff()), labels());

    helpType(sb, "bitaxe_best_session_difficulty", "Best session difficulty (normalized; K=1e3,M=1e6,G=1e9,T=1e12)", "gauge");
    gauge(sb, "bitaxe_best_session_difficulty", parseMagnitudeNumber(systemInfo.getBestSessionDiff()), labels());

    // error percentage
    helpType(sb, "bitaxe_error_percentage", "Hash error percentage", "gauge");
    gauge(sb, "bitaxe_error_percentage", systemInfo.getErrorPercentage(), labels());

    // power & voltage & current
    helpType(sb, "bitaxe_power_watts", "Power consumption in watts", "gauge");
    gauge(sb, "bitaxe_power_watts", systemInfo.getPower(), labels());

    helpType(sb, "bitaxe_voltage_volts", "Input voltage", "gauge");
    gauge(sb, "bitaxe_voltage_volts", mVtoV(systemInfo.getVoltage()), labels());

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

    // expected hashrate
    helpType(sb, "bitaxe_expected_hashrate", "Expected hashrate", "gauge");
    gauge(sb, "bitaxe_expected_hashrate", systemInfo.getExpectedHashrate(), labels());

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
        var count = BigDecimal.valueOf(r.getCount());
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

    // board power/voltage related
    helpType(sb, "bitaxe_max_power_watts", "Configured maximum board power", "gauge");
    gauge(sb, "bitaxe_max_power_watts", asBigDecimal(systemInfo.getMaxPower()), labels());

    helpType(sb, "bitaxe_nominal_voltage_volts", "Nominal board voltage", "gauge");
    gauge(sb, "bitaxe_nominal_voltage_volts", asBigDecimal(systemInfo.getNominalVoltage()), labels());

    helpType(sb, "bitaxe_core_voltage_volts", "Configured ASIC core voltage", "gauge");
    gauge(sb, "bitaxe_core_voltage_volts", mVtoV(systemInfo.getCoreVoltage()), labels());

    helpType(sb, "bitaxe_core_voltage_actual_volts", "Actual ASIC core voltage", "gauge");
    gauge(sb, "bitaxe_core_voltage_actual_volts", mVtoV(systemInfo.getCoreVoltageActual()), labels());

    // stratum and pool related
    helpType(sb, "bitaxe_is_using_fallback_stratum", "1 if using fallback stratum", "gauge");
    gauge(sb, "bitaxe_is_using_fallback_stratum", systemInfo.getIsUsingFallbackStratum(), labels());

    helpType(sb, "bitaxe_psram_available", "1 if PSRAM is available", "gauge");
    gauge(sb, "bitaxe_psram_available", systemInfo.getIsPSRAMAvailable(), labels());

    helpType(sb, "bitaxe_stratum_suggested_difficulty", "Primary pool suggested difficulty", "gauge");
    gauge(sb, "bitaxe_stratum_suggested_difficulty", systemInfo.getStratumSuggestedDifficulty(), labels());

    helpType(sb, "bitaxe_fallback_stratum_suggested_difficulty", "Fallback pool suggested difficulty", "gauge");
    gauge(sb, "bitaxe_fallback_stratum_suggested_difficulty", systemInfo.getFallbackStratumSuggestedDifficulty(), labels());

    helpType(sb, "bitaxe_stratum_port", "Primary stratum port", "gauge");
    gauge(sb, "bitaxe_stratum_port", systemInfo.getStratumPort(), labels());

    helpType(sb, "bitaxe_fallback_stratum_port", "Fallback stratum port", "gauge");
    gauge(sb, "bitaxe_fallback_stratum_port", systemInfo.getFallbackStratumPort(), labels());

    helpType(sb, "bitaxe_stratum_extranonce_subscribe", "Primary pool extranonce subscribe (0/1)", "gauge");
    gauge(sb, "bitaxe_stratum_extranonce_subscribe", asBigDecimal(systemInfo.getStratumExtranonceSubscribe()), labels());

    helpType(sb, "bitaxe_fallback_stratum_extranonce_subscribe", "Fallback pool extranonce subscribe (0/1)", "gauge");
    gauge(sb, "bitaxe_fallback_stratum_extranonce_subscribe", asBigDecimal(systemInfo.getFallbackStratumExtranonceSubscribe()), labels());

    // display / fan controls
    helpType(sb, "bitaxe_min_fan_speed_percent", "Minimum fan speed percent (auto mode)", "gauge");
    gauge(sb, "bitaxe_min_fan_speed_percent", asBigDecimal(systemInfo.getMinFanSpeed()), labels());

    helpType(sb, "bitaxe_temperature_target_celsius", "Target temperature (PID)", "gauge");
    gauge(sb, "bitaxe_temperature_target_celsius", systemInfo.getTemptarget(), labels());

    helpType(sb, "bitaxe_autofanspeed", "Automatic fan control (0/1)", "gauge");
    gauge(sb, "bitaxe_autofanspeed", systemInfo.getAutofanspeed(), labels());

    helpType(sb, "bitaxe_display_rotation", "Display rotation", "gauge");
    gauge(sb, "bitaxe_display_rotation", systemInfo.getRotation(), labels());

    helpType(sb, "bitaxe_display_inverted", "Display inverted (0/1)", "gauge");
    gauge(sb, "bitaxe_display_inverted", systemInfo.getInvertscreen(), labels());

    helpType(sb, "bitaxe_display_timeout_seconds", "Display timeout seconds (-1 = never)", "gauge");
    gauge(sb, "bitaxe_display_timeout_seconds", systemInfo.getDisplayTimeout(), labels());

    // response / stats
    helpType(sb, "bitaxe_response_time_ms", "Bitaxe API response time (ms)", "gauge");
    gauge(sb, "bitaxe_response_time_ms", systemInfo.getResponseTime(), labels());

    helpType(sb, "bitaxe_stats_frequency_seconds", "Stats update frequency (s)", "gauge");
    gauge(sb, "bitaxe_stats_frequency_seconds", systemInfo.getStatsFrequency(), labels());

    // other informative counters
    helpType(sb, "bitaxe_small_core_count", "Small core count", "gauge");
    gauge(sb, "bitaxe_small_core_count", systemInfo.getSmallCoreCount(), labels());

    helpType(sb, "bitaxe_overclock_enabled", "Overclock enabled (0/1)", "gauge");
    gauge(sb, "bitaxe_overclock_enabled", asBigDecimal(systemInfo.getOverclockEnabled()), labels());

    helpType(sb, "bitaxe_overheat_mode", "Overheat protection mode", "gauge");
    gauge(sb, "bitaxe_overheat_mode", systemInfo.getOverheatMode(), labels());

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

  private static BigDecimal asBigDecimal(Number n) {
    if (n == null) return null;
    if (n instanceof BigDecimal bd) return bd;
    return new BigDecimal(n.toString());
  }

  private static BigDecimal mVtoV(BigDecimal mv) {
    if (mv == null) return null;
    return mv.divide(BigDecimal.valueOf(1000L));
  }

  // Parses values like "1.2 K", "50.2 M", "123.8 G", "10.25 T" (case-insensitive, whitespace optional)
  // into a BigDecimal normalized by multipliers K=1e3, M=1e6, G=1e9, T=1e12. If unit missing, treats as raw.
  // Accepts a String input; returns null for null/blank/invalid inputs so the metric is skipped.
  private static final Pattern MAG_PATTERN = Pattern.compile("^\\s*([0-9]+(?:\\.[0-9]+)?)\\s*([kKmMgGtT])?\\s*$");

  private static BigDecimal parseMagnitudeNumber(String s) {
    if (s == null) return null;
    String in = s.trim();
    if (in.isEmpty()) return null;
    Matcher m = MAG_PATTERN.matcher(in);
    if (!m.matches()) return null;

    BigDecimal base;
    try {
      base = new BigDecimal(m.group(1));
    } catch (NumberFormatException e) {
      return null;
    }
    String unit = m.group(2);
    if (unit == null || unit.isEmpty()) return base;
    return switch (Character.toUpperCase(unit.charAt(0))) {
      case 'K' -> base.multiply(BigDecimal.valueOf(1_000L));
      case 'M' -> base.multiply(BigDecimal.valueOf(1_000_000L));
      case 'G' -> base.multiply(BigDecimal.valueOf(1_000_000_000L));
      case 'T' -> base.multiply(BigDecimal.valueOf(1_000_000_000_000L));
      default -> base; // fallback
    };
  }
}
