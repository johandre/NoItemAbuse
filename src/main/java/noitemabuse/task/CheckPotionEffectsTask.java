/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.task;

import static noitemabuse.config.EventMessage.POTION_DRINK;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

import noitemabuse.NIAManager;

import eu.icecraft_mc.frozenlib_R1.Plugin;

public class CheckPotionEffectsTask implements Task {
    private NIAManager manager;
    private Event event;
    private ItemStack item;
    private Player player;

    public CheckPotionEffectsTask(NIAManager manager, ItemStack item, PlayerEvent event) {
        this.manager = manager;
        this.event = event;
        this.item = item;
        this.player = event.getPlayer();
    }

    public void run() {
        manager.check(player, item, event, POTION_DRINK);
    }

    public void schedule(Plugin plugin) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, 1);
    }
}