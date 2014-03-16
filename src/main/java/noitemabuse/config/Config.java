/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.config;

import reflectlib.bukkit.Plugin;
import reflectlib.bukkit.config.Reflecturation;

public class Config extends Reflecturation {
    public boolean default_notify = true;
    public boolean multi_alert = false;
    public boolean remove_invalid_potions = true;
    public boolean log_all_players = true;
    public boolean purify_all = true;
    public boolean notify_console = true;
    public int max_potion_effect_duration_ticks = 9600;
    public String actions = "remove,cancel,notify,log";

    public Config(Plugin plugin) {
        super(plugin);
        init();
    }
}
