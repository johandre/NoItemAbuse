/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.config;

import java.util.*;

import org.bukkit.entity.Player;

import reflectlib.bukkit.Plugin;
import reflectlib.manager.Manager;
import noitemabuse.action.*;
import noitemabuse.check.Check;
import noitemabuse.manager.*;

public class ConfigManager extends Manager {
    public final Config values;
    private List<String> toggled = new ArrayList<String>();
    private Action[] actions;
    public Log logAction = null;

    public ConfigManager(Plugin parent) {
        super(parent);
        values = new Config(parent);
    }

    public String getActionMessage(Player p, String... args) {
        if (values.getBoolean("actions.Log.multi_alert")) {
            StringBuilder alert = new StringBuilder();
            for (Action action : actions) {
                Message msg = action.getMessage();
                if (actions.length > 1 && (msg == Message.LOG || msg == Message.CANCEL)) {
                    continue; // prevent multi-alert from being *too* spammy
                }
                alert.append(Message.format(p, msg, args)).append("\n");
            }
            String str = alert.toString();
            return str.substring(0, str.length() - 1);
        } else {
            Message msg = actions[0].getMessage();
            return Message.format(p, msg, args);
        }
    }

    public Action[] getActions() {
        return actions;
    }

    public List<String> getToggledPlayers() {
        return toggled;
    }

    @Override
    public void init() {
        CheckManager checkManager = plugin.getManager(CheckManager.class);
        Config cfg = new Config(plugin);
        actions = getAllActions();
        for (MessageEnum message : Message.getMessages()) {
            String node = message.getNode(), value = message.getMessage();
            if (cfg.get(node) == null) {
                cfg.set(node, value);
            } else {
                message.setMessage(cfg.getString(node, value));
            }
        }
        checkManager.checks = getAllChecks();
        for (Check check : checkManager.checks) {
            check.registerEvents();
            check.loadOptions();
        }
        for (Action action : actions) {
            action.loadOptions();
        }
        cfg.save();
    }

    private Action[] getAllActions() {
        List<Action> actions = plugin.getClassFinder().getInstancesOfType(Action.class, new Class[] { Plugin.class }, plugin);
        for (Iterator<Action> iterator = actions.iterator(); iterator.hasNext();) {
            Action action = iterator.next();
            action.loadOptions();
            if (!action.getOptions().enabled) {
                iterator.remove();
                continue;
            }
            if(action instanceof Log) {
                Log log = (Log) action;
                if(log.options.execute_first) {
                    iterator.remove();
                    this.logAction = log;
                }
            }
        }
        return actions.toArray(new Action[actions.size()]);
    }

    private Check[] getAllChecks() {
        List<Check> list = plugin.getClassFinder().getInstancesOfType(Check.class, new Class[] { Plugin.class }, plugin);
        for (Iterator<Check> iterator = list.iterator(); iterator.hasNext();) {
            Check check = iterator.next();
            check.loadOptions();
            if (!check.getOptions().enabled) {
                iterator.remove();
            }
        }
        return list.toArray(new Check[list.size()]);
    }
}
