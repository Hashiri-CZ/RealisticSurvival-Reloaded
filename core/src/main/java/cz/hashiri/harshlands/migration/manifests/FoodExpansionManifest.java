package cz.hashiri.harshlands.migration.manifests;

import cz.hashiri.harshlands.migration.ModuleManifest;

import java.util.Set;

public class FoodExpansionManifest extends ModuleManifest {

    @Override
    public String moduleId() {
        return "foodexpansion";
    }

    @Override
    protected Set<String> translationRoots() {
        return Set.of(
                "FoodExpansion.Initialize",
                "FoodExpansion.Shutdown",
                "FoodExpansion.Overeating",
                "FoodExpansion.CustomFoods"
        );
    }

    @Override
    protected Set<String> translationLeafDenylist() {
        // Gameplay fields whose raw YAML values are enum strings or cross-references
        // into custom-food IDs. They must NOT be moved into Translations because the
        // readers (CustomFoodRecipes, CustomFoodRegistry, CustomFoodDrops) look them
        // up from Settings/foodexpansion.yml via getString / getStringList.
        return Set.of(
                "Type",          // recipe type enum ("SHAPELESS", "FURNACE", ...)
                "Ingredients",   // list of ingredient item IDs
                "Pattern",       // list of shaped-recipe rows
                "Input",         // furnace input item ID
                "Output",        // furnace output item ID
                "Item",          // mob-drop custom-food ID reference
                "CookedItem",    // mob-drop cooked custom-food ID reference
                "RawItem",       // raw variant custom-food ID reference
                "BaseMaterial",  // vanilla Material enum
                "Flags"          // ItemFlag enum list
        );
    }
}
