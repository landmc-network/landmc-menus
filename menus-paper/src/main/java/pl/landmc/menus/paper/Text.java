package pl.landmc.menus.paper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import pl.landmc.platform.component.ComponentFormatter;

/**
 * Fills the placeholders in a configured line and turns it into a component.
 *
 * <p>Substitution happens before parsing, and that order is deliberate: a player name is
 * inserted as text into the template, so a name containing something that looks like a tag is
 * still parsed - and there is nothing a Minecraft name may contain that MiniMessage reads as
 * one, since a name is letters, digits and underscores.
 *
 * <p>The same is not true of a punishment reason, which is free text somebody typed. That is
 * why {@link #escaped} exists and why the reason goes through it.
 */
public final class Text {

    private final ComponentFormatter formatter;

    public Text(ComponentFormatter formatter) {
        this.formatter = Objects.requireNonNull(formatter, "formatter");
    }

    public Component of(String template) {
        return this.formatter.format(template);
    }

    public Component of(String template, Map<String, String> placeholders) {
        return this.formatter.format(replace(template, placeholders));
    }

    public List<Component> ofAll(List<String> templates, Map<String, String> placeholders) {
        List<Component> lines = new ArrayList<>(templates.size());
        for (String template : templates) {
            lines.add(this.of(template, placeholders));
        }
        return lines;
    }

    /**
     * Makes a value safe to put into a template.
     *
     * <p>A punishment reason, a server display name and anything else a person typed can
     * contain a {@code <} - and a value that reaches the parser unescaped can turn the rest of
     * the line a different colour, or throw the parse away entirely. Escaping the value rather
     * than the template keeps the template's own tags working.
     */
    public String escaped(String value) {
        return this.formatter.miniMessage().escapeTags(value);
    }

    /**
     * The same text with its colour taken off.
     *
     * <p>For putting a value that carries its own colour into a template that has to win - the
     * name of the server you are standing on, which the old menu turned red. Colour inside the
     * value would otherwise override the template's from the first tag onwards.
     */
    public String stripped(String value) {
        return this.formatter.plain(this.formatter.format(value));
    }

    private static String replace(String template, Map<String, String> placeholders) {
        String result = template;
        for (Map.Entry<String, String> placeholder : placeholders.entrySet()) {
            result = result.replace(placeholder.getKey(), placeholder.getValue());
        }
        return result;
    }
}
