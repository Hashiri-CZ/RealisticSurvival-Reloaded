package cz.hashiri.harshlands.foodexpansion;

import cz.hashiri.harshlands.data.HLPlayer;
import cz.hashiri.harshlands.data.foodexpansion.DataModule;
import cz.hashiri.harshlands.foodexpansion.items.CustomFoodRegistry;
import cz.hashiri.harshlands.locale.Messages;

import net.kyori.adventure.audience.Audience;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Per-player task that decides whether to send the three-cell nutrient preview
 * to the action bar based on the player's currently-held main-hand item. Runs
 * on a fixed tick cadence (configurable via {@code FoodExpansion.HUD.Preview.RefreshTicks}).
 *
 * <p>The companion check on the receiving side is in {@link FoodPreviewState#isActive(Player)};
 * {@code DisplayTask} consults the same predicate to suppress its own action-bar send when
 * the preview is active, so exactly one writer drives the action bar in any given tick.</p>
 */
public class NutritionPreviewController extends BukkitRunnable {

    private final Player player;
    private final FoodExpansionModule module;
    private String lastSignature = null; // no-op guard

    private final int cellSpacing;

    public NutritionPreviewController(Player player, FoodExpansionModule module) {
        this.player = player;
        this.module = module;
        org.bukkit.configuration.file.FileConfiguration cfg = module.getUserConfig().getConfig();
        this.cellSpacing = cfg.getInt("FoodExpansion.HUD.Preview.CellSpacing", 24);
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            cancel();
            return;
        }
        if (!FoodPreviewState.isActive(player)) {
            clear();
            return;
        }

        ItemStack mainHand = player.getInventory().getItemInMainHand();
        CustomFoodRegistry cfRegistry = module.getCustomFoodRegistry();
        String itemKey;
        if (cfRegistry != null && cfRegistry.isCustomFood(mainHand)) {
            itemKey = cfRegistry.getFoodId(mainHand);
        } else {
            itemKey = mainHand.getType().name();
        }
        NutrientProfile profile = module.getNutrientProfile(itemKey);
        // FoodPreviewState.isActive already guarantees profile != null, but defensive null-check
        // protects against config reloads racing the tick.
        if (profile == null) { clear(); return; }

        HLPlayer hl = HLPlayer.getPlayers().get(player.getUniqueId());
        if (hl == null) { clear(); return; }
        DataModule dm = hl.getNutritionDataModule();
        if (dm == null || dm.getData() == null) { clear(); return; }
        PlayerNutritionData data = dm.getData();

        double comfort = module.getComfortMultiplier(player);

        String sig = itemKey + "|" + (int) data.getProtein() + "|" + (int) data.getCarbs()
                + "|" + (int) data.getFats() + "|" + comfort
                + "|" + profile.protein() + "|" + profile.carbs() + "|" + profile.fats();
        if (sig.equals(lastSignature)) return;
        lastSignature = sig;

        NutritionPreviewLayout.Row row = NutritionPreviewLayout.buildRow(
                profile,
                data.getProtein(), data.getCarbs(), data.getFats(),
                comfort,
                module.getSevereThreshold(), module.getMalnourishedThreshold(),
                module.getWellNourishedThreshold(), module.getPeakThreshold(),
                Messages.get("foodexpansion.food_expansion.preview.protein"),
                Messages.get("foodexpansion.food_expansion.preview.carbs"),
                Messages.get("foodexpansion.food_expansion.preview.fat"),
                cellSpacing);

        ((Audience) player).sendActionBar(row.component());
    }

    /** Reset the no-op guard. The action bar is not actively cleared — Mojang fades it out
     *  on its own once we stop sending, and {@link cz.hashiri.harshlands.utils.DisplayTask}
     *  will resume sending its thirst/temp/fear text on its next tick. */
    public void clear() {
        lastSignature = null;
    }
}
