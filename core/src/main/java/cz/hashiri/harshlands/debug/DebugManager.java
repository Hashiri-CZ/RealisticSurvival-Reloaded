package cz.hashiri.harshlands.debug;

import cz.hashiri.harshlands.HLPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class DebugManager implements Listener {

    private final HLPlugin plugin;
    private final Logger logger;
    private final Object lock = new Object();

    // observer UUID -> module name -> set of subsystem names ("*" for all)
    private final Map<UUID, Map<String, Set<String>>> activeSubscriptions = new ConcurrentHashMap<>();
    // observer UUID -> target player UUID (absent = self)
    private final Map<UUID, UUID> observerTargets = new ConcurrentHashMap<>();
    // module name -> provider
    private final Map<String, DebugProvider> providers = new ConcurrentHashMap<>();
    // precomputed hot-path lookup: "module.subsystem:targetUUID"
    private volatile Set<String> activeKeys = Collections.emptySet();
    // reverse index: key -> set of observer UUIDs
    private volatile Map<String, Set<UUID>> keyToObservers = Collections.emptyMap();

    public DebugManager(@Nonnull HLPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void registerProvider(@Nonnull DebugProvider provider) {
        providers.put(provider.getModuleName(), provider);
    }

    /**
     * Toggles a subscription. Returns true if now enabled, false if now disabled.
     */
    public boolean toggle(@Nonnull UUID observer, @Nonnull String module,
                          @Nullable String subsystem, @Nullable UUID target) {
        synchronized (lock) {
            if (!providers.containsKey(module)) {
                return false;
            }

            // Validate subsystem name if provided
            if (subsystem != null) {
                DebugProvider provider = providers.get(module);
                if (!provider.getSubsystems().contains(subsystem)) {
                    return false;
                }
            }

            if (target != null) {
                observerTargets.put(observer, target);
            } else {
                observerTargets.remove(observer);
            }

            Map<String, Set<String>> subs = activeSubscriptions.computeIfAbsent(observer, k -> new ConcurrentHashMap<>());
            Set<String> subsystems = subs.computeIfAbsent(module, k -> ConcurrentHashMap.newKeySet());

            String key = subsystem != null ? subsystem : "*";
            boolean wasPresent = subsystems.remove(key);
            if (!wasPresent) {
                subsystems.add(key);
            }

            // Clean up empty sets
            if (subsystems.isEmpty()) {
                subs.remove(module);
            }
            if (subs.isEmpty()) {
                activeSubscriptions.remove(observer);
                observerTargets.remove(observer);
            }

            rebuildKeys();
            return !wasPresent;
        }
    }

    /**
     * Toggles "Everything" — all modules+subsystems. Returns true if enabled, false if disabled.
     */
    public boolean toggleAll(@Nonnull UUID observer, @Nullable UUID target) {
        synchronized (lock) {
            // If observer already has any subscriptions, clear all
            if (activeSubscriptions.containsKey(observer)) {
                clearAllInternal(observer);
                return false;
            }

            if (target != null) {
                observerTargets.put(observer, target);
            } else {
                observerTargets.remove(observer);
            }

            Map<String, Set<String>> subs = new ConcurrentHashMap<>();
            for (DebugProvider provider : providers.values()) {
                Set<String> set = ConcurrentHashMap.newKeySet();
                set.add("*");
                subs.put(provider.getModuleName(), set);
            }
            activeSubscriptions.put(observer, subs);

            rebuildKeys();
            return true;
        }
    }

    public void clearAll(@Nonnull UUID observer) {
        synchronized (lock) {
            clearAllInternal(observer);
        }
    }

    private void clearAllInternal(@Nonnull UUID observer) {
        activeSubscriptions.remove(observer);
        observerTargets.remove(observer);
        rebuildKeys();
    }

    /**
     * Fast O(1) check: is anyone observing this module+subsystem for this player?
     */
    public boolean isActive(@Nonnull String module, @Nonnull String subsystem, @Nonnull UUID targetPlayer) {
        Set<String> keys = activeKeys;
        if (keys.isEmpty()) return false;
        return keys.contains(module + "." + subsystem + ":" + targetPlayer);
    }

    /**
     * Routes debug output to matching observers and console.
     * Callers must gate on {@link #isActive} before calling this method.
     */
    public void send(@Nonnull String module, @Nonnull String subsystem, @Nonnull UUID targetPlayer,
                     @Nonnull String chatLine, @Nonnull String consoleLine) {
        String targetName = Optional.ofNullable(Bukkit.getPlayer(targetPlayer))
                .map(Player::getName).orElse(targetPlayer.toString());
        logger.info("[DEBUG][" + module + "." + subsystem + "][" + targetName + "] " + consoleLine);

        String key = module + "." + subsystem + ":" + targetPlayer;
        Map<String, Set<UUID>> k2o = keyToObservers;
        Set<UUID> observers = k2o.get(key);
        if (observers == null) return;

        if (!chatLine.isEmpty()) {
            for (UUID observerUuid : observers) {
                Player observer = Bukkit.getPlayer(observerUuid);
                if (observer != null && observer.isOnline()) {
                    observer.sendMessage(chatLine);
                }
            }
        }
    }

    @Nonnull
    public List<String> getStatus(@Nonnull UUID observer) {
        List<String> lines = new ArrayList<>();
        Map<String, Set<String>> subs = activeSubscriptions.get(observer);
        if (subs == null || subs.isEmpty()) {
            lines.add("\u00a76[Debug] No active subscriptions.");
            return lines;
        }

        UUID targetUuid = observerTargets.get(observer);
        String targetName;
        if (targetUuid == null) {
            targetName = "yourself";
        } else {
            Player target = Bukkit.getPlayer(targetUuid);
            targetName = target != null ? target.getName() : targetUuid.toString();
        }

        lines.add("\u00a76[Debug] Active subscriptions:");
        for (Map.Entry<String, Set<String>> entry : subs.entrySet()) {
            String mod = entry.getKey();
            for (String sub : entry.getValue()) {
                lines.add("\u00a77 - " + mod + "." + sub + " \u00a7f\u2192 \u00a77" + targetName);
            }
        }
        return lines;
    }

    @Nonnull
    public Map<String, DebugProvider> getProviders() {
        return Collections.unmodifiableMap(providers);
    }

    public boolean hasProvider(@Nonnull String moduleName) {
        return providers.containsKey(moduleName);
    }

    @EventHandler
    public void onPlayerQuit(@Nonnull PlayerQuitEvent event) {
        synchronized (lock) {
            UUID quitter = event.getPlayer().getUniqueId();

            boolean hadSubscriptions = activeSubscriptions.containsKey(quitter);

            // Clear quitter's own subscriptions
            activeSubscriptions.remove(quitter);
            observerTargets.remove(quitter);

            // Clear subscriptions from others targeting this player, notify them
            if (!observerTargets.isEmpty()) {
                String quitterName = event.getPlayer().getName();
                for (Map.Entry<UUID, UUID> entry : new HashMap<>(observerTargets).entrySet()) {
                    if (quitter.equals(entry.getValue())) {
                        UUID observerUuid = entry.getKey();
                        activeSubscriptions.remove(observerUuid);
                        observerTargets.remove(observerUuid);
                        hadSubscriptions = true;

                        Player observer = Bukkit.getPlayer(observerUuid);
                        if (observer != null && observer.isOnline()) {
                            observer.sendMessage("\u00a7c[Debug] \u00a7fTarget " + quitterName + " disconnected \u2014 subscription removed");
                        }
                    }
                }
            }

            if (hadSubscriptions) {
                rebuildKeys();
            }
        }
    }

    public void shutdown() {
        activeSubscriptions.clear();
        observerTargets.clear();
        activeKeys = Collections.emptySet();
        keyToObservers = Collections.emptyMap();
    }

    private void rebuildKeys() {
        Set<String> newKeys = new HashSet<>();
        Map<String, Set<UUID>> newK2O = new HashMap<>();

        for (Map.Entry<UUID, Map<String, Set<String>>> observerEntry : activeSubscriptions.entrySet()) {
            UUID observerUuid = observerEntry.getKey();
            UUID targetUuid = observerTargets.getOrDefault(observerUuid, observerUuid);

            for (Map.Entry<String, Set<String>> moduleEntry : observerEntry.getValue().entrySet()) {
                String moduleName = moduleEntry.getKey();
                Set<String> subsystems = moduleEntry.getValue();

                if (subsystems.contains("*")) {
                    DebugProvider provider = providers.get(moduleName);
                    if (provider != null) {
                        for (String sub : provider.getSubsystems()) {
                            String key = moduleName + "." + sub + ":" + targetUuid;
                            newKeys.add(key);
                            newK2O.computeIfAbsent(key, k -> new HashSet<>()).add(observerUuid);
                        }
                    }
                } else {
                    for (String sub : subsystems) {
                        String key = moduleName + "." + sub + ":" + targetUuid;
                        newKeys.add(key);
                        newK2O.computeIfAbsent(key, k -> new HashSet<>()).add(observerUuid);
                    }
                }
            }
        }

        this.activeKeys = Collections.unmodifiableSet(newKeys);
        this.keyToObservers = Collections.unmodifiableMap(newK2O);
    }
}
