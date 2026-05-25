package cz.hashiri.harshlands.bodyhealth;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class BHDLoggerTest {

    private Logger captureLogger;
    private List<String> captured;

    @BeforeEach void setUp() {
        captureLogger = Logger.getLogger("BHDLoggerTest-" + System.nanoTime());
        captureLogger.setUseParentHandlers(false);
        captured = new ArrayList<>();
        captureLogger.addHandler(new Handler() {
            @Override public void publish(LogRecord r) { captured.add(r.getMessage()); }
            @Override public void flush() {}
            @Override public void close() {}
        });
        captureLogger.setLevel(Level.ALL);
    }

    @AfterEach void tearDown() {
        BHDLogger.enable(false, null);
    }

    @Test void disabled_logger_is_a_noop() {
        BHDLogger.enable(false, captureLogger);
        BHDLogger.log("should not appear");
        BHDLogger.logf("%s=%d", "x", 5);
        assertTrue(captured.isEmpty(), "disabled BHDLogger must not emit; got: " + captured);
        assertFalse(BHDLogger.isEnabled());
    }

    @Test void enabled_logger_prefixes_with_BHD() {
        BHDLogger.enable(true, captureLogger);
        BHDLogger.log("hello");
        assertEquals(1, captured.size());
        assertEquals("[BHD] hello", captured.get(0));
        assertTrue(BHDLogger.isEnabled());
    }

    @Test void enabled_logger_formats_with_logf() {
        BHDLogger.enable(true, captureLogger);
        BHDLogger.logf("%s=%d", "x", 5);
        assertEquals(1, captured.size());
        assertEquals("[BHD] x=5", captured.get(0));
    }

    @Test void enable_false_with_null_logger_does_not_crash() {
        BHDLogger.enable(false, null);
        BHDLogger.log("ignored");
        assertTrue(captured.isEmpty());
    }
}
