package cz.hashiri.harshlands.locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocaleManagerUtf8Test {

    @Test
    void loads_cjk_from_utf8_file(@TempDir Path root) throws IOException {
        Path zh = root.resolve("zh-CN");
        Files.createDirectories(zh);
        byte[] utf8 = "protein: \"\u86CB\u767D\u8D28\"\n".getBytes(StandardCharsets.UTF_8);
        Files.write(zh.resolve("x.yml"), utf8);

        LocaleManager m = new LocaleManager(root, "zh-CN");
        m.load();

        assertEquals("\u86CB\u767D\u8D28", m.get("protein"));
    }
}
