package io.github.colusite.strayarmor;

import java.util.Locale;

/** Text cleanup shared by the StrayTags config reader. */
public final class TextUtil {

    private TextUtil() {
    }

    /**
     * Mirrors StrayTags' matching cleanup: strips section-sign formatting codes,
     * private-use icons, and anything the name pattern cannot match, then
     * collapses whitespace.
     */
    public static String clean(String raw) {
        if (raw == null) return null;
        int length = raw.length();
        StringBuilder output = new StringBuilder(length);
        boolean lastSpace = true;
        int i = 0;
        while (i < length) {
            char c = raw.charAt(i);
            if (c == '\u00a7' && i + 1 < length) {
                i += 2;
                continue;
            }
            if (c >= '\uE000' && c <= '\uF8FF') {
                i++;
                continue;
            }
            if (c >= '\uDB80' && c <= '\uDBFF' && i + 1 < length) {
                char next = raw.charAt(i + 1);
                if (next >= '\uDC00' && next <= '\uDFFF') {
                    i += 2;
                    continue;
                }
            }
            boolean allowed = (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9')
                    || c == '_' || c == '[' || c == ']' || c == ' ';
            if (!allowed) {
                i++;
                continue;
            }
            if (c == ' ') {
                if (lastSpace) {
                    i++;
                    continue;
                }
                output.append(' ');
                lastSpace = true;
            } else {
                output.append(c);
                lastSpace = false;
            }
            i++;
        }
        if (output.length() > 0 && output.charAt(output.length() - 1) == ' ') {
            output.setLength(output.length() - 1);
        }
        return output.toString();
    }

    /** Removes MiniMessage tags, legacy codes, and placeholders for keyword checks. */
    public static String stripMarkup(String value) {
        if (value == null) return "";
        String stripped = value.replaceAll("\u00a7.", " ");
        stripped = stripped.replaceAll("<[^>]*>", " ");
        stripped = stripped.replaceAll("%[A-Za-z0-9_]+%", " ");
        return stripped.toLowerCase(Locale.ROOT);
    }
}
