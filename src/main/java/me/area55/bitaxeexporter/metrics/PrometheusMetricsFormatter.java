package me.area55.bitaxeexporter.metrics;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.StringJoiner;
import me.area55.bitaxeexporter.bitaxe.model.SystemInfo;
import org.springframework.stereotype.Component;

@Component
public class PrometheusMetricsFormatter {

  public String format(SystemInfo systemInfo) {
    if (systemInfo == null) {
      return "";
    }

    final var sb = new StringBuilder(2048);

    // Global labels
    final var hostname = blankIfNull(systemInfo.getHostname());
    final var ipv4 = blankIfNull(systemInfo.getIpv4());

    appendPowerMetrics(sb, systemInfo, hostname, ipv4);
    appendTemperatureMetrics(sb, systemInfo, hostname, ipv4);
    appendHashrateMetrics(sb, systemInfo, hostname, ipv4);
    appendDifficultyMetrics(sb, systemInfo, hostname, ipv4);
    appendMemoryAndVoltageMetrics(sb, systemInfo, hostname, ipv4);
    appendFrequencyMetrics(sb, systemInfo, hostname, ipv4);
    appendShareMetrics(sb, systemInfo, hostname, ipv4);
    appendUptimeAndLatencyMetrics(sb, systemInfo, hostname, ipv4);
    appendThermalControlMetrics(sb, systemInfo, hostname, ipv4);
    appendFanMetrics(sb, systemInfo, hostname, ipv4);
    appendBlockchainMetrics(sb, systemInfo, hostname, ipv4);
    appendFaultMetrics(sb, systemInfo, hostname, ipv4);

    return sb.toString();
  }

  // ---------------------------------------------------------------------------
  // Append groups
  // ---------------------------------------------------------------------------

  private static void appendPowerMetrics(StringBuilder sb, SystemInfo systemInfo,
      String hostname, String ipv4) {
    gaugeWithHelp(sb,
        "bitaxe_power_watts",
        "Power consumption in watts",
        systemInfo.getPower(),
        baseLabels(hostname, ipv4));

    gaugeWithHelp(sb,
        "bitaxe_voltage_volts",
        "Input voltage",
        millisToUnit(systemInfo.getVoltage()),
        baseLabels(hostname, ipv4));

    gaugeWithHelp(sb,
        "bitaxe_current_amps",
        "Current draw in amperes",
        millisToUnit(systemInfo.getCurrent()),
        baseLabels(hostname, ipv4));
  }

  private static void appendTemperatureMetrics(StringBuilder sb, SystemInfo systemInfo,
      String hostname, String ipv4) {
    gaugeWithHelp(sb,
        "bitaxe_temperature_celsius",
        "Chip temperature",
        systemInfo.getTemp(),
        baseLabels(hostname, ipv4, label("sensor", "avg")));

    gaugeWithHelp(sb,
        "bitaxe_vr_temperature_celsius",
        "Voltage regulator temperature",
        systemInfo.getVrTemp(),
        baseLabels(hostname, ipv4));
  }

  private static void appendHashrateMetrics(StringBuilder sb, SystemInfo systemInfo,
      String hostname, String ipv4) {
    // hashrate in GH/s -> normalize to H/s
    gaugeWithHelp(sb,
        "bitaxe_hashrate",
        "Current hashrate (H/s)",
        ghToHs(systemInfo.getHashRate()),
        baseLabels(hostname, ipv4));

    gaugeWithHelp(sb,
        "bitaxe_error_percentage",
        "Hash error percentage",
        systemInfo.getErrorPercentage(),
        baseLabels(hostname, ipv4));
  }

  private static void appendDifficultyMetrics(StringBuilder sb, SystemInfo systemInfo,
      String hostname, String ipv4) {
    gaugeWithHelp(sb,
        "bitaxe_best_difficulty",
        "Best difficulty achieved",
        systemInfo.getBestDiff(),
        baseLabels(hostname, ipv4));

    gaugeWithHelp(sb,
        "bitaxe_best_session_difficulty",
        "Best session difficulty",
        systemInfo.getBestSessionDiff(),
        baseLabels(hostname, ipv4));

    gaugeWithHelp(sb,
        "bitaxe_pool_difficulty",
        "Current pool difficulty",
        systemInfo.getPoolDifficulty(),
        baseLabels(hostname, ipv4));

    gaugeWithHelp(sb,
        "bitaxe_is_using_fallback_stratum",
        "1 if using fallback stratum",
        systemInfo.getIsUsingFallbackStratum(),
        baseLabels(hostname, ipv4));
  }

  private static void appendMemoryAndVoltageMetrics(StringBuilder sb, SystemInfo systemInfo,
      String hostname, String ipv4) {
    gaugeWithHelp(sb,
        "bitaxe_free_heap_bytes",
        "Free heap bytes",
        systemInfo.getFreeHeap(),
        baseLabels(hostname, ipv4, label("type", "total")));

    gaugeWithHelp(sb,
        "bitaxe_core_voltage_mv",
        "Configured ASIC core voltage",
        systemInfo.getCoreVoltage(),
        baseLabels(hostname, ipv4));

    gaugeWithHelp(sb,
        "bitaxe_core_voltage_actual_mv",
        "Actual ASIC core voltage",
        systemInfo.getCoreVoltageActual(),
        baseLabels(hostname, ipv4));
  }

  private static void appendFrequencyMetrics(StringBuilder sb, SystemInfo systemInfo,
      String hostname, String ipv4) {
    gaugeWithHelp(sb,
        "bitaxe_frequency_mhz",
        "ASIC frequency in MHz",
        systemInfo.getFrequency(),
        baseLabels(hostname, ipv4));
  }

  private static void appendShareMetrics(StringBuilder sb, SystemInfo systemInfo,
      String hostname, String ipv4) {
    counterWithHelp(sb,
        "bitaxe_shares_accepted_total",
        "Accepted shares",
        systemInfo.getSharesAccepted(),
        baseLabels(hostname, ipv4));

    counterWithHelp(sb,
        "bitaxe_shares_rejected_total",
        "Rejected shares",
        systemInfo.getSharesRejected(),
        baseLabels(hostname, ipv4));

    // rejected reasons
    helpType(sb,
        "bitaxe_shares_rejected_reason_total",
        "Rejected shares by reason",
        "counter");

    if (systemInfo.getSharesRejectedReasons() != null) {
      for (var reason : systemInfo.getSharesRejectedReasons()) {
        if (reason == null) {
          continue;
        }
        var count = BigDecimal.valueOf(reason.getCount());
        counter(sb,
            "bitaxe_shares_rejected_reason_total",
            count,
            baseLabels(hostname, ipv4, label("reason", blankIfNull(reason.getMessage()))));
      }
    }
  }

  private static void appendUptimeAndLatencyMetrics(StringBuilder sb, SystemInfo systemInfo,
      String hostname, String ipv4) {
    counterWithHelp(sb,
        "bitaxe_uptime_seconds_total",
        "Uptime in seconds",
        systemInfo.getUptimeSeconds(),
        baseLabels(hostname, ipv4));

    gaugeWithHelp(sb,
        "bitaxe_response_time_ms",
        "Bitaxe API response time (ms)",
        systemInfo.getResponseTime(),
        baseLabels(hostname, ipv4));
  }

  private static void appendThermalControlMetrics(StringBuilder sb, SystemInfo systemInfo,
      String hostname, String ipv4) {
    gaugeWithHelp(sb,
        "bitaxe_overheat_mode",
        "Overheat protection mode",
        systemInfo.getOverheatMode(),
        baseLabels(hostname, ipv4));

    gaugeWithHelp(sb,
        "bitaxe_autofanspeed",
        "Automatic fan control (0/1)",
        systemInfo.getAutofanspeed(),
        baseLabels(hostname, ipv4));
  }

  private static void appendFanMetrics(StringBuilder sb, SystemInfo systemInfo,
      String hostname, String ipv4) {
    gaugeWithHelp(sb,
        "bitaxe_fan_speed_percent",
        "Fan speed percentage",
        systemInfo.getFanspeed(),
        baseLabels(hostname, ipv4));

    gaugeWithHelp(sb,
        "bitaxe_fan_rpm",
        "Fan speed in RPM",
        systemInfo.getFanrpm(),
        baseLabels(hostname, ipv4));
  }

  private static void appendBlockchainMetrics(StringBuilder sb, SystemInfo systemInfo,
      String hostname, String ipv4) {
    gaugeWithHelp(sb,
        "bitaxe_block_found",
        "1 if a block was found in this session, else 0",
        systemInfo.getBlockFound(),
        baseLabels(hostname, ipv4));

    gaugeWithHelp(sb,
        "bitaxe_block_height",
        "Current block height",
        systemInfo.getBlockHeight(),
        baseLabels(hostname, ipv4));

    gaugeWithHelp(sb,
        "bitaxe_network_difficulty",
        "Network difficulty",
        systemInfo.getNetworkDifficulty(),
        baseLabels(hostname, ipv4));
  }

  private static void appendFaultMetrics(StringBuilder sb, SystemInfo systemInfo,
      String hostname, String ipv4) {
    // power fault (as info metric with label)
    helpType(sb,
        "bitaxe_power_fault_info",
        "Power fault info (1 if present)",
        "gauge");

    var pf = (systemInfo.getPowerFault() == null || systemInfo.getPowerFault().isBlank())
        ? BigDecimal.ZERO
        : BigDecimal.ONE;

    gauge(sb,
        "bitaxe_power_fault_info",
        pf,
        baseLabels(hostname, ipv4, label("fault", blankIfNull(systemInfo.getPowerFault()))));
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  private static String blankIfNull(String v) {
    return (v == null || v.isBlank()) ? null : v;
  }

  private static String label(String k, String v) {
    if (v == null) {
      return null;
    }
    return k + "=\"" + escape(v) + "\"";
  }

  private static String labels(String... kvs) {
    if (kvs == null || kvs.length == 0) {
      return "";
    }
    StringJoiner j = new StringJoiner(",", "{", "}");
    for (String kv : kvs) {
      if (kv == null || kv.isBlank()) {
        continue;
      }
      j.add(kv);
    }
    String s = j.toString();
    return s.equals("{}") ? "" : s;
  }

  private static String baseLabels(String hostname, String ipv4, String... extra) {
    String[] all = new String[2 + (extra == null ? 0 : extra.length)];
    all[0] = label("hostname", hostname);
    all[1] = label("ipv4", ipv4);
    if (extra != null && extra.length > 0) {
      System.arraycopy(extra, 0, all, 2, extra.length);
    }
    return labels(all);
  }

  private static void helpType(StringBuilder sb, String metric, String help, String type) {
    sb.append("# HELP ").append(metric).append(' ').append(escape(help)).append('\n');
    sb.append("# TYPE ").append(metric).append(' ').append(type).append('\n');
  }

  private static void gauge(StringBuilder sb, String metric, BigDecimal value, String labels) {
    if (value == null) {
      return;
    }
    sb.append(metric);
    if (!labels.isEmpty()) {
      sb.append(labels);
    }
    sb.append(' ').append(format(value)).append('\n');
  }

  private static void gauge(StringBuilder sb, String metric, Integer value, String labels) {
    if (value == null) {
      return;
    }
    sb.append(metric);
    if (!labels.isEmpty()) {
      sb.append(labels);
    }
    sb.append(' ').append(value).append('\n');
  }

  private static void counter(StringBuilder sb, String metric, BigDecimal value, String labels) {
    gauge(sb, metric, value, labels);
  }

  private static void gaugeWithHelp(StringBuilder sb, String metric, String help,
      BigDecimal value, String labels) {
    helpType(sb, metric, help, "gauge");
    gauge(sb, metric, value, labels);
  }

  private static void gaugeWithHelp(StringBuilder sb, String metric, String help,
      Integer value, String labels) {
    helpType(sb, metric, help, "gauge");
    gauge(sb, metric, value, labels);
  }

  private static void counterWithHelp(StringBuilder sb, String metric, String help,
      BigDecimal value, String labels) {
    helpType(sb, metric, help, "counter");
    counter(sb, metric, value, labels);
  }

  private static String escape(String s) {
    if (s == null) {
      return "";
    }
    return s.replace("\\", "\\\\")
        .replace("\n", "\\n")
        .replace("\"", "\\\"");
  }

  private static String format(BigDecimal d) {
    return String.format(Locale.ROOT, "%s", d.stripTrailingZeros().toPlainString());
  }

  private static BigDecimal millisToUnit(BigDecimal mv) {
    if (mv == null) {
      return null;
    }
    return mv.divide(BigDecimal.valueOf(1000L), RoundingMode.HALF_UP);
  }

  private static BigDecimal ghToHs(BigDecimal gh) {
    if (gh == null) {
      return null;
    }
    return gh.multiply(BigDecimal.valueOf(1_000_000_000L));
  }
}
