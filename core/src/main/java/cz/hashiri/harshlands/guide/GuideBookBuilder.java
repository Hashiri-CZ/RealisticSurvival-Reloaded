/*
    Copyright (C) 2026  Hashiri_
    GNU GPL v3.
 */
package cz.hashiri.harshlands.guide;

import cz.hashiri.harshlands.hints.HintSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class GuideBookBuilder {

    private GuideBookBuilder() {}

    /** Build a signed written book from the given translations config. */
    public static ItemStack buildBook(FileConfiguration cfg) {
        List<BaseComponent[]> pages = buildPages(cfg);

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta == null) return book;

        String rawTitle = cfg.getString("guide.Title", "Harshlands Field Manual");
        String plainTitle = ChatColor.stripColor(legacy(rawTitle));
        if (plainTitle.length() > 32) plainTitle = plainTitle.substring(0, 32);
        meta.setTitle(plainTitle);

        meta.setAuthor(cfg.getString("guide.Author", "Harshlands"));
        meta.spigot().setPages(pages);
        book.setItemMeta(meta);
        return book;
    }

    /** Package-visible for testing: produce the page list without wrapping in an ItemStack. */
    public static List<BaseComponent[]> buildPages(FileConfiguration cfg) {
        ConfigurationSection root = cfg.getConfigurationSection("guide");
        if (root == null) {
            return singlePage(fallbackPage("Missing.", "&e%name%", ""));
        }

        ConfigurationSection chaptersSection = root.getConfigurationSection("Chapters");
        List<Map<?, ?>> tocEntries = root.getMapList("Contents.Entries");

        String itemTagTemplate = root.getString("ItemTag", "&e%name%");
        String clickHint = root.getString("ClickHint", "&7Click for obtain instructions");

        if (chaptersSection == null || tocEntries.isEmpty()) {
            String fallback = root.getString("MissingContentFallback", "Missing.");
            return singlePage(fallbackPage(fallback, itemTagTemplate, clickHint));
        }

        String backLabel = root.getString("BackToContentsLabel", "");

        // First pass: collect chapters in ToC order, compute their starting page numbers.
        // Page numbering is 1-based. Cover = page 1, ToC = page 2, chapters start at 3.
        List<ChapterResolved> chapters = new ArrayList<>();
        int cursorPage = 3;
        for (Map<?, ?> entry : tocEntries) {
            Object keyObj = entry.get("key");
            Object labelObj = entry.get("label");
            if (keyObj == null || labelObj == null) continue;

            String key = keyObj.toString();
            String label = labelObj.toString();

            List<String> chapterPages = chaptersSection.getStringList(key);
            if (chapterPages.isEmpty()) {
                // Chapter referenced in ToC but not defined — skip.
                continue;
            }

            chapters.add(new ChapterResolved(key, label, chapterPages, cursorPage));
            cursorPage += chapterPages.size();
        }

        List<BaseComponent[]> pages = new ArrayList<>();

        // Cover
        pages.add(renderPage(joinLines(root.getStringList("Cover")), /*trailing*/ null, itemTagTemplate, clickHint));

        // ToC
        pages.add(buildTocPage(root.getString("Contents.Heading", "Contents"), chapters));

        // Chapters
        for (ChapterResolved ch : chapters) {
            for (int i = 0; i < ch.pages.size(); i++) {
                String raw = ch.pages.get(i);
                boolean isLast = (i == ch.pages.size() - 1);
                String trailing = (isLast && !backLabel.isEmpty()) ? backLabel : null;
                pages.add(renderPage(raw, trailing, itemTagTemplate, clickHint));
            }
        }

        return pages;
    }

    private static BaseComponent[] fallbackPage(String message, String itemTagTemplate, String clickHint) {
        String translated = legacy(message);
        return HintSender.renderClickable(translated, itemTagTemplate, clickHint);
    }

    private static List<BaseComponent[]> singlePage(BaseComponent[] page) {
        List<BaseComponent[]> list = new ArrayList<>();
        list.add(page);
        return list;
    }

    private static BaseComponent[] buildTocPage(String heading, List<ChapterResolved> chapters) {
        ComponentBuilder builder = new ComponentBuilder();
        String translatedHeading = legacy(heading);
        for (BaseComponent bc : TextComponent.fromLegacyText(translatedHeading + "\n\n")) {
            builder.append(bc, ComponentBuilder.FormatRetention.NONE);
        }

        for (ChapterResolved ch : chapters) {
            TextComponent link = new TextComponent();
            String translatedLabel = legacy(ch.label);
            for (BaseComponent bc : TextComponent.fromLegacyText(translatedLabel)) {
                link.addExtra(bc);
            }
            link.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, Integer.toString(ch.startPage)));
            builder.append(link, ComponentBuilder.FormatRetention.NONE);
            builder.append("\n", ComponentBuilder.FormatRetention.NONE);
        }

        return builder.create();
    }

    private static BaseComponent[] renderPage(String raw, String trailingBackLabel,
                                              String itemTagTemplate, String clickHint) {
        // YAML pipe-literal "|" preserves a trailing newline; strip it so we don't stack blanks.
        String normalized = raw.replaceAll("\\s+$", "");
        // Translate & color codes before feeding to fromLegacyText / HintSender.
        String translated = legacy(normalized);
        // Use the hints renderer to parse %item_<key>% tokens into clickable tags.
        BaseComponent[] body = HintSender.renderClickable(translated, itemTagTemplate, clickHint);

        if (trailingBackLabel == null) return body;

        ComponentBuilder builder = new ComponentBuilder();
        for (BaseComponent bc : body) builder.append(bc, ComponentBuilder.FormatRetention.NONE);
        builder.append("\n\n", ComponentBuilder.FormatRetention.NONE);

        TextComponent back = new TextComponent();
        String translatedBack = legacy(trailingBackLabel);
        for (BaseComponent bc : TextComponent.fromLegacyText(translatedBack)) {
            back.addExtra(bc);
        }
        back.setClickEvent(new ClickEvent(ClickEvent.Action.CHANGE_PAGE, "2"));
        builder.append(back, ComponentBuilder.FormatRetention.NONE);

        return builder.create();
    }

    private static String joinLines(List<String> lines) {
        return String.join("\n", lines);
    }

    /** Translates &-style legacy color codes. Used at every page-render boundary. */
    private static String legacy(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    private record ChapterResolved(String key, String label, List<String> pages, int startPage) {}
}
