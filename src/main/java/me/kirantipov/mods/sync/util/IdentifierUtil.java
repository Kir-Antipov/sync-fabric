package me.kirantipov.mods.sync.util;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class IdentifierUtil {
    private static final Pattern FIRST_LETTER = Pattern.compile("^\\w|_\\w");

    public static String prettify(Identifier identifier) {
        Matcher matcher = FIRST_LETTER.matcher(identifier.getPath());
        return matcher.replaceAll(x -> {
            String value = x.group(0);
            return value.length() == 1 ? value.toUpperCase() : value.toUpperCase().replace('_', ' ');
        });
    }

    public static Text prettifyAsText(Identifier identifier) {
        return Text.of(prettify(identifier));
    }
}