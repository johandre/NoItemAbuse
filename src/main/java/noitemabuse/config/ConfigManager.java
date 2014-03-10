/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.config;

import java.util.*;

import org.bukkit.entity.Player;

import reflectlib.bukkit.Plugin;
import reflectlib.manager.Manager;
import noitemabuse.action.Action;

public class ConfigManager extends Manager {
    public final Config values;
    private List<String> toggled = new ArrayList<String>();
    private Action[] actionList;
    private Action[] allActions;

    public ConfigManager(Plugin parent) {
        super(parent);
        values = new Config(parent);
    }

    public String getActionMessage(Player p, String... args) {
        if (values.multi_alert) {
            StringBuilder alert = new StringBuilder();
            for (Action action : actionList) {
                Message msg = action.getMessage();
                // prevent multi-alert from being *too* spammy
                if (actionList.length > 1 && (msg == Message.LOG || msg == Message.CANCEL)) {
                    continue;
                }
                alert.append(Message.format(p, msg, args)).append("\n");
            }
            String str = alert.toString();
            return str.substring(0, str.length() - 1);
        } else {
            Message msg = actionList[0].getMessage();
            return Message.format(p, msg, args);
        }
    }

    public Action[] getActions() {
        return actionList;
    }

    public List<String> getToggledPlayers() {
        return toggled;
    }

    @Override
    public void init() {
        Config cfg = new Config(plugin);
        actionList = parseActions(values.actions.split(","));
        for (MessageEnum message : Message.getMessages()) {
            String node = message.getNode(), value = message.getMessage();
            if (cfg.get(node) == null) {
                cfg.set(node, value);
            } else {
                message.setMessage(cfg.getString(node, value));
            }
        }
        cfg.save();
    }

    private Action[] getAllActions() {
        List<Action> actions = plugin.getClassFinder().getInstancesOfType(Action.class, new Class[] { Plugin.class }, plugin);
        return actions.toArray(new Action[actions.size()]);
    }

    private Action[] parseActions(String[] actions) {
        if (allActions == null) {
            allActions = getAllActions();
        }
        List<Action> list = new ArrayList<Action>();
        for (String str : actions) {
            str = str.trim().toLowerCase();
            for (Action action : allActions) {
                if (action.getName().equals(str)) {
                    list.add(action);
                }
            }
        }
        return list.toArray(new Action[list.size()]);
    }
}
