package dev.tnhzr.astrarp.util;

public final class Args {

    private Args() {}

    /**
     * Reconstructs a single quoted argument starting from index {@code from}.
     * If the first token starts with a quote, returns the joined tokens up to the
     * matching closing quote. Otherwise returns the first token unchanged.
     */
    public static String parseQuoted(String[] args, int from) {
        if (from >= args.length) return null;
        String first = args[from];
        if (first.startsWith("\"")) {
            StringBuilder sb = new StringBuilder(first.substring(1));
            if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '"') {
                sb.deleteCharAt(sb.length() - 1);
                return sb.toString();
            }
            for (int i = from + 1; i < args.length; i++) {
                sb.append(' ');
                String part = args[i];
                if (part.endsWith("\"")) {
                    sb.append(part, 0, part.length() - 1);
                    return sb.toString();
                } else {
                    sb.append(part);
                }
            }
            return sb.toString();
        }
        return first;
    }

    /** Returns everything from index {@code from} joined by spaces, stripping outer quotes if present. */
    public static String joinRest(String[] args, int from) {
        if (from >= args.length) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < args.length; i++) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(args[i]);
        }
        String joined = sb.toString();
        if (joined.startsWith("\"") && joined.endsWith("\"") && joined.length() >= 2) {
            return joined.substring(1, joined.length() - 1);
        }
        return joined;
    }
}
