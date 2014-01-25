/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.config;


public enum EventMessage implements MessageEnum {
    BLOCK_BREAK("held by $player on block break"),
    RECEIVED_ATTACK("worn by $player on received attack"),
    ATTACK("wielded by $player when attacking $player2"),
    INVENTORY_CLICK("in inventory of $player"),
    CONTAINER_OPEN("in $container at $location opened by $player"),
    ITEM_DROP("dropped by $player at $location"),
    POTION_THROW("thrown by $player"),
    //
    ;
    private String message;

    private EventMessage(String message) {
        setMessage(message);
    }

    public String getNode() {
        return "event_messages." + name().toLowerCase();
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
