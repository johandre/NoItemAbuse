/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.config;

import java.util.*;

import org.bukkit.entity.Player;

import reflectlib.bukkit.Plugin;
import reflectlib.manager.Manager;
import noitemabuse.CheckManager;
import noitemabuse.action.Action;
import noitemabuse.check.Check;

public class ConfigManager extends Manager {
    public final Config values;
    private List<String> toggled = new ArrayList<String>();
    private Action[] actionList;

    public ConfigManager(Plugin parent) {
        super(parent);
        values = new Config(parent);
    }

    public String getActionMessage(Player p, String... args) {
        if (values.getBoolean("actions.log.multi_alert")) {
            StringBuilder alert = new StringBuilder();
            for (Action action : actionList) {
                Message msg = action.getMessage();
                if (actionList.length > 1 && (msg == Message.LOG || msg == Message.CANCEL)) {
                    continue; // prevent multi-alert from being *too* spammy
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
    public boolean loadAfter(Manager manager) {
        return manager instanceof CheckManager;
    }

    @Override
    public void init() {
        CheckManager checkManager = plugin.getManager(CheckManager.class);
        Config cfg = new Config(plugin);
        actionList = getAllActions();
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
        }
        for (Action action : actionList) {
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
