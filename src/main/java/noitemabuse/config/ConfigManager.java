/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.config;

import java.util.*;

import org.bukkit.entity.Player;

import noitemabuse.action.Action;

import eu.icecraft_mc.frozenlib_R1.Plugin;
import eu.icecraft_mc.frozenlib_R1.manager.Manager;
import eu.icecraft_mc.frozenlib_R1.util.ClassFinder;
import eu.icecraft_mc.frozenlib_R1.util.config.SimpleConfiguration;

public class ConfigManager extends Manager {
    public List<String> toggled = new ArrayList<String>();
    public boolean defaultNotify = true;
    public boolean multiAlert = false;
    public boolean removeInvalidPotions = true;
    public String actionString = "cancel,remove,notify,log";
    public Action[] actions;
    private Action[] allActions;

    public ConfigManager(Plugin parent) {
        super(parent);
    }

    public String getActionMessage(Player p, String... args) {
        if (multiAlert) {
            StringBuilder alert = new StringBuilder();
            for (Action action : actions) {
                Message msg = action.getMessage();
                alert.append(Message.format(p, msg, args)).append("\n");
            }
            String str = alert.toString();
            return str.substring(0, str.length() - 1);
        } else {
            Message msg = actions[0].getMessage();
            return Message.format(p, msg, args);
        }
    }

    @Override
    public void init() {
        SimpleConfiguration cfg = new SimpleConfiguration();
        String folder = plugin.getDataFolder().getAbsolutePath();
        cfg.init(folder);
        defaultNotify = cfg.getBoolean("default_notify", defaultNotify);
        multiAlert = cfg.getBoolean("multi_alert", multiAlert);
        removeInvalidPotions = cfg.getBoolean("remove_invalid_potions", removeInvalidPotions);
        String as = cfg.getString("actions", actionString).toLowerCase();
        actions = parseActions(as.split(","));
        for (MessageEnum message : Message.getMessages()) {
            message.setMessage(cfg.getString(message.getNode(), message.getMessage()));
        }
        cfg.save(folder);
    }

    private Action[] getAllActions() {
        ClassFinder finder = plugin.getClassFinder();
        List<Action> actions = finder.getInstancesOfType(finder.getJarPath(getClass()), "", Action.class, new Class[] { Plugin.class }, plugin);
        return actions.toArray(new Action[actions.size()]);
    }

    private Action[] parseActions(String[] actions) {
        if (allActions == null) {
            allActions = getAllActions();
        }
        List<Action> list = new ArrayList<Action>();
        for (String str : actions) {
            str = str.trim();
            for (Action action : allActions) {
                if (action.getName().equals(str)) {
                    list.add(action);
                }
            }
        }
        return list.toArray(new Action[list.size()]);
    }
}
