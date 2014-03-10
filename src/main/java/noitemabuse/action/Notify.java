/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.action;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;
import noitemabuse.config.Message;

public class Notify extends Log {
    public Notify(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void perform(Player player, ItemStack item, Event event, String eventMessage) {
        String message = getMessage(player, item, event, eventMessage);
        message = Message.ALERT_PREFIX + message;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("noitemabuse.notify") && shouldNotify(p)) {
                p.sendMessage(message);
            }
        }
        if (config.values.notify_console) {
            plugin.getServer().getConsoleSender().sendMessage(message);
        }
    }

    private boolean shouldNotify(Player player) {
        return config.getToggledPlayers().contains(player.getName().toLowerCase()) != config.values.default_notify;
    }
}
