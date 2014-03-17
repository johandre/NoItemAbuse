/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.config;

import reflectlib.bukkit.Plugin;
import reflectlib.bukkit.config.Reflecturation;

public class Config extends Reflecturation {
    public Config(Plugin plugin) {
        super(plugin);
        init();
    }
}
