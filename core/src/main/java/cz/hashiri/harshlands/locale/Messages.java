package cz.hashiri.harshlands.locale;

import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

public final class Messages {

    private static volatile LocaleManager manager;

    private static final java.util.regex.Pattern PLACEHOLDER_PATTERN =
            java.util.regex.Pattern.compile("%([A-Za-z0-9_]+)%");

    private Messages() {}

    public static void bind(LocaleManager m) {
        manager = m;
    }

    public static void reset() {
        manager = null;
    }

    public static void reload() {
        LocaleManager m = manager;
        if (m != null) m.reload();
    }

    public static String get(String key) {
        LocaleManager m = manager;
        if (m == null) return "[" + key + "]";
        return m.get(key);
    }

    public static String get(String key, Map<String, ?> placeholders) {
        String raw = get(key);
        if (placeholders == null || placeholders.isEmpty()) return raw;

        // Build a case-insensitive lookup for the substitution map.
        java.util.Map<String, String> lookup = new java.util.HashMap<>(placeholders.size() * 2);
        for (Map.Entry<String, ?> e : placeholders.entrySet()) {
            lookup.put(e.getKey().toLowerCase(java.util.Locale.ROOT), String.valueOf(e.getValue()));
        }

        java.util.regex.Matcher m = PLACEHOLDER_PATTERN.matcher(raw);
        StringBuilder out = new StringBuilder(raw.length());
        int last = 0;
        boolean replaced = false;
        while (m.find()) {
            String name = m.group(1).toLowerCase(java.util.Locale.ROOT);
            String value = lookup.get(name);
            if (value == null) continue; // leave unknown placeholders intact
            out.append(raw, last, m.start());
            out.append(value);
            last = m.end();
            replaced = true;
        }
        if (!replaced) return raw;
        out.append(raw, last, raw.length());
        return out.toString();
    }

    public static List<String> getList(String key) {
        LocaleManager m = manager;
        if (m == null) return List.of();
        return m.getList(key);
    }

    public static Builder of(String key) {
        return new Builder(key);
    }

    public static final class Builder {
        private final String key;
        private final java.util.LinkedHashMap<String, Object> placeholders = new java.util.LinkedHashMap<>();

        private Builder(String key) {
            this.key = key;
        }

        public Builder with(String name, Object value) {
            placeholders.put(name, value);
            return this;
        }

        public String build() {
            return Messages.get(key, placeholders);
        }

        public void send(CommandSender recipient) {
            if (recipient != null) recipient.sendMessage(build());
        }
    }
}
