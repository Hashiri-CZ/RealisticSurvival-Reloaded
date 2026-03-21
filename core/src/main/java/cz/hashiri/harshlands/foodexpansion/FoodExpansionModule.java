package cz.hashiri.harshlands.foodexpansion;

import cz.hashiri.harshlands.data.HLModule;
import cz.hashiri.harshlands.rsv.HLPlugin;

import java.util.Map;

/**
 * Stub module class for FoodExpansion. Will be fully implemented in a later task.
 */
public class FoodExpansionModule extends HLModule {

    public static final String NAME = "FoodExpansion";

    private final HLPlugin plugin;

    public FoodExpansionModule(HLPlugin plugin) {
        super(NAME, plugin, Map.of(), Map.of());
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        // TODO: will be implemented in a later task
    }

    @Override
    public void shutdown() {
        // TODO: will be implemented in a later task
    }
}
