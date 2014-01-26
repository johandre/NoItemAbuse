/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.config;

import static org.bukkit.ChatColor.*;

import org.bukkit.entity.Player;

public enum Message implements MessageEnum {
    BAN("Banned $player for having invalid item: $item ($reason)"),
    KICK("Kicked $player for having invalid item: $item ($reason)"),
    CONFISCATE("Confiscated $item $event ($reason)"),
    PURIFY("Purified $item $event ($reason)"),
    REMOVE("Removed $item $event ($reason)"),
    CANCEL("Prevented use of $item $event ($reason)"),
    LOG("Item used: $item $event ($reason)"),
    ALERT_PREFIX(RED + "[" + DARK_RED + "NoItemAbuse" + RED + "] " + GOLD),
    REASON_BANNED_ITEM("banned item"),
    REASON_OVERDURABLE("durability $durability < 0"),
    REASON_OVERENCHANT("$enchant enchantment level $level > $max"),
    REASON_POTION_INVALID_LEVEL("invalid potion level $level"),
    REASON_POTION_INVALID_EFFECT_LEVEL("invalid potion effect $type: $effectlevel > 2"),
    REASON_POTION_INVALID_EFFECT_DURATION("invalid potion effect $type: $duration > 9600"),
    COMMAND_TOGGLE_ON(GREEN + "Alerts have been toggled on."),
    COMMAND_TOGGLE_OFF(GREEN + "Alerts have been toggled off.");
    private String message;

    private Message(String message) {
        setMessage(message);
    }

    public static String format(Player player, MessageEnum message, String... args) {
        String formatted = message.getMessage().replace("$player", player.getName());
        for (String str : args) {
            String[] split = str.split(":", 2);
            String var = split[0];
            String replacement = split[1];
            formatted = formatted.replace(var, replacement);
        }
        return formatted;
    }

    public static MessageEnum[] getMessages() {
        // merge the two arrays
        MessageEnum[] messages = values(), eventMessages = EventMessage.values();
        int mlen = messages.length, elen = eventMessages.length, size = mlen + elen, i = 0;
        MessageEnum[] all = new MessageEnum[size];
        for (; i < mlen; i++) {
            all[i] = messages[i];
        }
        for (; i < size; i++) {
            all[i] = eventMessages[i - mlen];
        }
        return all;
    }

    public String getMessage() {
        return message;
    }

    public String getNode() {
        return "messages." + name().toLowerCase();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}
