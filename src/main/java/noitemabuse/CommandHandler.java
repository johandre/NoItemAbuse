/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerQuitEvent;

import reflectlib.bukkit.*;
import noitemabuse.config.*;

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
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;
        final String name = sender.getName().toLowerCase(), cmd = args[0];
        if (!(sender instanceof Player) && !cmd.equals("reload")) return false;
        if (cmd.equals("toggle") && sender.hasPermission("noitemabuse.notify")) {
            Player player = (Player) sender;
            if (!config.getToggledPlayers().contains(name)) {
                add(player, name);
            } else {
                remove(player, name);
            }
            sender.sendMessage(getToggleMessage(name).getMessage());
        } else if (cmd.equals("reload") && sender.hasPermission("noitemabuse.reload")) {
            config.init();
            sender.sendMessage(Message.COMMAND_RELOAD.getMessage());
        } else return false;
        return true;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String name = event.getPlayer().getName().toLowerCase();
        config.getToggledPlayers().remove(name);
    }

    void add(Player player, String name) {
        config.getToggledPlayers().add(name);
    }

    Message getToggleMessage(String name) {
        return config.getToggledPlayers().contains(name) != config.values.getBoolean("actions.notify.default_notify") ? Message.COMMAND_TOGGLE_ON : Message.COMMAND_TOGGLE_OFF;
    }

    void remove(Player player, String name) {
        config.getToggledPlayers().remove(name);
    }
}
