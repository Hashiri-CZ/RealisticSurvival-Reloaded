package cz.hashiri.harshlands.disease.mitigation;

import org.bukkit.entity.Player;

public interface Mitigation {
    boolean isActive(Player player);
}
