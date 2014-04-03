/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse;

import org.bukkit.event.Listener;

import reflectlib.bukkit.Plugin;
import noitemabuse.config.*;

public abstract class Executor {
    protected Plugin plugin;
    protected CheckManager manager;
    protected ConfigManager config;
    protected Options options;

    public Executor(Plugin plugin) {
        this.plugin = plugin;
        config = plugin.getManager(ConfigManager.class);
        manager = plugin.getManager(CheckManager.class);
        options = getOptions();
    }

    public boolean defaultEnabled() {
        return true;
    }

    public String getName() {
        return getClass().getSimpleName().toLowerCase();
    }

    public Options getOptions() {
        return options;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void loadOptions() {
        if (getOptions() == null) {
            options = new Options(this);
        }
        getOptions().enabled = defaultEnabled();
        getOptions().init();
    }

    public void registerEvents() {
        if (this instanceof Listener) {
            plugin.getServer().getPluginManager().registerEvents((Listener) this, plugin);
        }
    }
}
