/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.task;

import static noitemabuse.config.EventMessage.POTION_DRINK;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;
import noitemabuse.manager.CheckManager;

public class CheckPotionEffectsTask implements Task {
    private CheckManager manager;
    private Event event;
    private ItemStack item;
    private Player player;

    public CheckPotionEffectsTask(CheckManager manager, ItemStack item, PlayerEvent event) {
        this(manager, item, event.getPlayer(), event);
    }
    
    public CheckPotionEffectsTask(CheckManager manager, ItemStack item, Player player, Event event) {
        this.manager = manager;
        this.event = event;
        this.item = item;
        this.player = player;
    }

    @Override
    public void run() {
        manager.check(player, item, event, POTION_DRINK);
    }

    @Override
    public void schedule(Plugin plugin) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, this, 1);
    }
}
