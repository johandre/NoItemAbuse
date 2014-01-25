/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerQuitEvent;

import noitemabuse.config.*;

import eu.icecraft_mc.frozenlib_R1.Plugin;
import eu.icecraft_mc.frozenlib_R1.manager.*;

public class CommandHandler extends AbstractCommandManager implements Listener {
    private ConfigManager config;

    public CommandHandler(Plugin parent) {
        super(parent);
    }

    @Override
    public void init() {
        this.config = plugin.getManager(ConfigManager.class);
    }

    @Override
    public boolean loadAfter(Manager manager) {
        return manager instanceof ConfigManager;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        String name = sender.getName().toLowerCase();
        if (args[0].equals("toggle")) {
            if (!config.toggled.contains(name)) {
                add(player, name);
            } else {
                remove(player, name);
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String name = event.getPlayer().getName().toLowerCase();
        config.toggled.remove(name);
    }

    void remove(Player player, String name) {
        config.toggled.remove(name);
        player.sendMessage(Message.COMMAND_TOGGLE_OFF.getMessage());
    }

    void add(Player player, String name) {
        config.toggled.add(name);
        player.sendMessage(Message.COMMAND_TOGGLE_ON.getMessage());
    }
}
