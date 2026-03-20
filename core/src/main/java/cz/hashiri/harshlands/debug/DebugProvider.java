package cz.hashiri.harshlands.debug;

import java.util.Collection;

public interface DebugProvider {
    String getModuleName();
    Collection<String> getSubsystems();
}
