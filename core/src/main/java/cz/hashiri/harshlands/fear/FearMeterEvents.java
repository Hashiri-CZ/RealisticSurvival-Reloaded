/*
    Copyright (C) 2026  Hashiri_

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package cz.hashiri.harshlands.fear;

import cz.hashiri.harshlands.data.HLPlayer;
import cz.hashiri.harshlands.data.fear.DataModule;
import cz.hashiri.harshlands.rsv.HLPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FearMeterEvents implements Listener {

    private final HLPlugin plugin;
    private final FileConfiguration config;

    public FearMeterEvents(HLPlugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void initialize() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private DataModule getFearDM(Player player) {
        if (!HLPlayer.isValidPlayer(player)) return null;
        HLPlayer hlPlayer = HLPlayer.getPlayers().get(player.getUniqueId());
        return hlPlayer != null ? hlPlayer.getFearDataModule() : null;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!config.getBoolean("FearMeter.Reductions.EatCookedFood.Enabled", true)) return;
        Player player = event.getPlayer();
        List<String> foods = config.getStringList("FearMeter.Reductions.EatCookedFood.Foods");
        if (!foods.contains(event.getItem().getType().name())) return;
        DataModule dm = getFearDM(player);
        if (dm == null) return;
        dm.decreaseFear(config.getDouble("FearMeter.Reductions.EatCookedFood.Amount", 5.0));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSleep(PlayerBedEnterEvent event) {
        if (!config.getBoolean("FearMeter.Reductions.Sleep.Enabled", true)) return;
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) return;
        DataModule dm = getFearDM(event.getPlayer());
        if (dm == null) return;
        dm.decreaseFear(config.getDouble("FearMeter.Reductions.Sleep.Amount", 10.0));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        if (!config.getBoolean("FearMeter.Reductions.OnDeath.Enabled", true)) return;
        DataModule dm = getFearDM(event.getEntity());
        if (dm == null) return;
        dm.setFearLevel(config.getDouble("FearMeter.Reductions.OnDeath.ResetTo", 0.0));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMonsterHit(EntityDamageByEntityEvent event) {
        if (!config.getBoolean("FearMeter.Increases.MonsterHit.Enabled", true)) return;
        if (!(event.getEntity() instanceof Player player)) return;
        Entity damager = event.getDamager();
        boolean isMonsterAttack = (damager instanceof Monster) ||
            (damager instanceof Projectile proj && proj.getShooter() instanceof Monster);
        if (!isMonsterAttack) return;
        DataModule dm = getFearDM(player);
        if (dm == null) return;
        dm.increaseFear(config.getDouble("FearMeter.Increases.MonsterHit.Amount", 3.0));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBurnDamage(EntityDamageEvent event) {
        if (!config.getBoolean("FearMeter.Increases.Burning.Enabled", true)) return;
        if (!(event.getEntity() instanceof Player player)) return;
        DamageCause cause = event.getCause();
        if (cause != DamageCause.FIRE && cause != DamageCause.FIRE_TICK && cause != DamageCause.LAVA) return;
        DataModule dm = getFearDM(player);
        if (dm == null) return;
        dm.increaseFear(config.getDouble("FearMeter.Increases.Burning.Amount", 2.0));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBowShot(EntityShootBowEvent event) {
        if (!config.getBoolean("FearMeter.Effects.ProjectileInaccuracy.Enabled", true)) return;
        if (!(event.getEntity() instanceof Player player)) return;
        DataModule dm = getFearDM(player);
        if (dm == null) return;
        double minFear = config.getDouble("FearMeter.Effects.ProjectileInaccuracy.MinFear", 40.0);
        double fearLevel = dm.getFearLevel();
        if (fearLevel < minFear) return;
        Bukkit.getScheduler().runTask(plugin, () -> applyVelocitySpread(event.getProjectile(), fearLevel, minFear));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnderPearlThrow(ProjectileLaunchEvent event) {
        if (!config.getBoolean("FearMeter.Effects.ProjectileInaccuracy.Enabled", true)) return;
        if (!(event.getEntity() instanceof EnderPearl pearl)) return;
        if (!(pearl.getShooter() instanceof Player player)) return;
        DataModule dm = getFearDM(player);
        if (dm == null) return;
        double minFear = config.getDouble("FearMeter.Effects.ProjectileInaccuracy.MinFear", 40.0);
        double fearLevel = dm.getFearLevel();
        if (fearLevel < minFear) return;
        Bukkit.getScheduler().runTask(plugin, () -> applyVelocitySpread(pearl, fearLevel, minFear));
    }

    private void applyVelocitySpread(Entity projectile, double fearLevel, double minFear) {
        double maxSpread = config.getDouble("FearMeter.Effects.ProjectileInaccuracy.SpreadScale", 0.04);
        double t = (fearLevel - minFear) / (100.0 - minFear);
        double spread = t * maxSpread;
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        Vector v = projectile.getVelocity();
        v.add(new Vector(
            (rng.nextDouble() - 0.5) * spread,
            (rng.nextDouble() - 0.5) * spread,
            (rng.nextDouble() - 0.5) * spread
        ));
        projectile.setVelocity(v);
    }
}
