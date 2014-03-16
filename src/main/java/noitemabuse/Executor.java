/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse;

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

    public String getName() {
        return getClass().getSimpleName().toLowerCase();
    }

    public Options getOptions() {
        return null;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void loadOptions() {
        if (options != null) {
            options.init();
        }
    }
}
