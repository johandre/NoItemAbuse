/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse;

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

public class CheckManager extends Manager {
    public FileLogger log;
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
            if (check.check(player, item)) {
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

    @Override
    public boolean loadAfter(Manager manager) {
        return manager instanceof ConfigManager;
    }

    public void performActions(Player player, ItemStack item, Event event, String message, List<Check> checks) {
        for (Action action : config.getActions()) {
            action.perform(player, item, event, message, checks);
        }
    }
}
