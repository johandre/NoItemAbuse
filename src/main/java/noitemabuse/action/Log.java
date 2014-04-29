/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.action;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import reflectlib.bukkit.Plugin;
import noitemabuse.check.Check;
import noitemabuse.config.*;
import noitemabuse.util.Executor;

public class Log extends Action {
    public LogOptions options = new LogOptions(this);

    public Log(Plugin plugin) {
        super(plugin);
    }

    public String getMessage(Player player, ItemStack item, Event event, String eventMessage, List<Check> checks) {
        final String itemName;
        if (item == null) {
            itemName = Message.ITEM_POTION_EFFECT.getMessage();
        } else {
            itemName = manager.getItemName(item);
        }
        String message = config.getActionMessage(player, "$item:" + itemName, "$reason:" + getReasons(player, item, checks), "$event:" + eventMessage);
        return message;
    }

    @Override
    public Options getOptions() {
        return options;
    }
    
    public Message getMessage() {
        try {
            return Message.valueOf(options.log_message.toUpperCase());
        } catch(Throwable ex) {
            return super.getMessage();
        }
    }

    @Override
    public void perform(Player player, ItemStack item, Event event, String message, List<Check> failedChecks) {
        log(player, getMessage(player, item, event, message, failedChecks));
    }

    protected String getReasons(Player player, ItemStack item, List<Check> checks) {
        String[] reasons = new String[checks.size()];
        for (int i = 0; i < reasons.length; i++) {
            reasons[i] = checks.get(i).getLogMessage(player, item);
        }
        StringBuilder reasonbuilder = new StringBuilder();
        for (String r : reasons) {
            reasonbuilder.append(r).append(", ");
        }
        String reason = reasonbuilder.toString();
        return reason.substring(0, reason.length() - 2);
    }

    private void log(Player p, String message) {
        if (options.log_all_players || p.hasPermission("noitemabuse.log")) {
            manager.log.log(message);
        }
    }

    public class LogOptions extends Options {
        public boolean execute_first = true;
        public boolean multi_alert = false;
        public boolean log_all_players = true;
        public String log_message = "remove";

        public LogOptions(Executor executor) {
            super(executor);
        }
    }
}
