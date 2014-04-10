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
import noitemabuse.util.Executor;

public class Notify extends Log {
    private NotifyOptions options = new NotifyOptions(this);

    public Notify(Plugin plugin) {
        super(plugin);
    }

    @Override
    public Message getMessage() {
        return Message.LOG;
    }

    @Override
    public Options getOptions() {
        return options;
    }

    @Override
    public void perform(Player player, ItemStack item, Event event, String eventMessage, List<Check> failedChecks) {
        String message = getMessage(player, item, event, eventMessage, failedChecks);
        message = Message.ALERT_PREFIX + message;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("noitemabuse.notify") && shouldNotify(p)) {
                p.sendMessage(message);
            }
        }
        if (options.notify_console) {
            plugin.getServer().getConsoleSender().sendMessage(message);
        }
    }

    private boolean shouldNotify(Player player) {
        return config.getToggledPlayers().contains(player.getName().toLowerCase()) != options.default_notify;
    }

    public class NotifyOptions extends Options {
        public boolean default_notify = true;
        public boolean notify_console = true;

        public NotifyOptions(Executor executor) {
            super(executor);
        }
    }
}
