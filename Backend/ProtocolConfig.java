package src5;

public final class ProtocolConfig {
    private ProtocolConfig() {}

    public static boolean strictProtocol() {
        return readFlag("STRICT_PROTOCOL");
    }

    public static boolean devFallback() {
        return readFlag("DEV_FALLBACK");
    }

    private static boolean readFlag(String key) {
        String value = System.getProperty(key);
        if (value == null || value.isEmpty()) {
            value = System.getenv(key);
        }
        if (value == null) {
            return false;
        }
        String normalized = value.trim().toLowerCase();
        return normalized.equals("true") || normalized.equals("1") || normalized.equals("yes");
    }
}
