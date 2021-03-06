/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.manager;

import java.io.File;
import java.util.*;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;
import reflectlib.manager.Manager;
import noitemabuse.action.Action;
import noitemabuse.check.Check;
import noitemabuse.config.*;
import noitemabuse.util.FileLogger;

public class CheckManager extends Manager {
    public FileLogger log;
    public Action[] actions;
    public Check[] checks;
    ConfigManager config;

    public CheckManager(Plugin parent) {
        super(parent);
    }

    public void check(Player player, ItemStack item, Event event, EventMessage eventMessage, String... args) {
        if (item == null || item.getType() == Material.AIR || player.hasPermission("noitemabuse.allow")) return;
        List<Check> failedChecks = getFailedChecks(player, item);
        if (!failedChecks.isEmpty()) {
            performActions(player, item, event, Message.format(player, eventMessage, args), failedChecks);
        }
    }

    @Override
    public void deinit() {
        log.close();
    }

    public List<Check> getFailedChecks(Player player, ItemStack item) {
        List<Check> failed = new ArrayList<Check>(checks.length);
        for (Check check : checks) {
            if (!check.hasPermissions(player, item) && check.check(player, item)) {
                failed.add(check);
            }
        }
        return failed;
    }

    public String getItemName(ItemStack item) {
        return item.getType().toString().toLowerCase().replace("_", " ");
    }

    @Override
    public void init() {
        config = plugin.getManager(ConfigManager.class);
        try {
            log = new FileLogger(new File(plugin.getDataFolder(), "NoItemAbuse.log"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void performActions(Player player, ItemStack item, Event event, String message, List<Check> failedChecks) {
        if (config.logAction != null) {
            config.logAction.perform(player, item, event, message, failedChecks);
        }
        for (Action action : actions) {
            action.perform(player, item, event, message, failedChecks);
        }
    }

    public void removeItem(Player player, ItemStack item) {
        // item.setType(Material.AIR); // does not work.
        player.getInventory().remove(item);
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            if (item.equals(armor[i])) {
                armor[i] = null;
            }
        }
        player.getInventory().setArmorContents(armor);
    }
}
