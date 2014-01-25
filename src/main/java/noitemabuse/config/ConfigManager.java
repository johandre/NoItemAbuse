package noitemabuse.config;

import java.util.*;

import org.bukkit.entity.Player;

import eu.icecraft_mc.frozenlib_R1.Plugin;
import eu.icecraft_mc.frozenlib_R1.manager.Manager;
import eu.icecraft_mc.frozenlib_R1.util.config.SimpleConfiguration;

public class ConfigManager extends Manager {
    public List<String> toggled = new ArrayList<String>();
    public boolean defaultNotify = true;
    public String actionString = "cancel,remove,notify,log";
    public String[] actions = actionString.split(",");

    public ConfigManager(Plugin parent) {
        super(parent);
    }

    @Override
    public void init() {
        SimpleConfiguration cfg = new SimpleConfiguration();
        String folder = plugin.getDataFolder().getAbsolutePath();
        cfg.init(folder);
        defaultNotify = cfg.getBoolean("default_notify", defaultNotify);
        String as = cfg.getString("actions", actionString).toLowerCase();
        actions = as.split(",");
        for (MessageEnum message : Message.getMessages()) {
            message.setMessage(cfg.getString(message.getNode(), message.getMessage()));
        }
        cfg.save(folder);
    }

    public String getActionMessage(Player p, String... args) {
        Message msg = null;
        for (String str : actions) {
            if (str.equals("ban")) {
                msg = Message.BAN;
            } else if (str.equals("kick")) {
                msg = Message.KICK;
            } else if (str.equals("remove")) {
                msg = Message.REMOVE;
            } else if (str.equals("confiscate")) {
                msg = Message.CONFISCATE;
            }
        }
        return Message.format(p, msg, args);
    }
}
