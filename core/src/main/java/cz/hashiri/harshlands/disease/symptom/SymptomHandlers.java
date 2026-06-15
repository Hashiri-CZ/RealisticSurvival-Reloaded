package cz.hashiri.harshlands.disease.symptom;

import java.util.HashMap;
import java.util.Map;

public final class SymptomHandlers {
    private final Map<String, SymptomHandler> handlers = new HashMap<>();

    public void register(String name, SymptomHandler handler) {
        handlers.put(name, handler);
    }

    public SymptomHandler get(String name) {
        return handlers.get(name);
    }
}
