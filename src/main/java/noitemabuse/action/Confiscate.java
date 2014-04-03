/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.action;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;
import noitemabuse.check.Check;
import noitemabuse.config.*;

public class Confiscate extends Log {
    public Confiscate(Plugin plugin) {
        super(plugin);
    }

    @Override
    public boolean defaultEnabled() {
        return false;
    }
    @Override
    public Options getOptions() {
        return new Options(this);
    }

    @Override
    public void perform(Player player, ItemStack item, Event event, String eventMessage, List<Check> failedChecks) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("noitemabuse.confiscate")) {
                if (!config.values.getBoolean("actions.Log.multi_alert")) {
                    String message = Message.format(player, Message.CONFISCATE, "$item:" + manager.getItemName(item), "$reason:" + getReasons(player, item, failedChecks), "$event:" + eventMessage);
                    message = Message.ALERT_PREFIX + message;
                    p.sendMessage(message);
                }
                manager.removeItem(player, item);
                p.getInventory().addItem(item);
                return;
            }
        }
    }
}
