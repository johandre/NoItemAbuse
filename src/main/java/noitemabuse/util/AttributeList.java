/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.util;

import java.util.*;

import javax.annotation.*;

import org.bukkit.inventory.ItemStack;

import com.google.common.base.*;
import com.google.common.collect.Iterators;

import noitemabuse.util.NBTFactory.NBTCompound;
import noitemabuse.util.NBTFactory.NBTList;

public class AttributeList implements Iterable<AttributeList.Attribute> {
    // This may be modified
    public ItemStack stack;
    private NBTList attributes;
    private static final Iterator<Attribute> emptyIterator = new Iterator<Attribute>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Attribute next() {
            return null;
        }

        @Override
        public void remove() {}
    };

    public AttributeList(ItemStack stack) {
        this.stack = NBTFactory.getCraftItemStack(stack);
        loadAttributes(false);
    }

    /**
     * Add a new attribute to the list.
     * @param attribute - the new attribute.
     */
    public void add(Attribute attribute) {
        Preconditions.checkNotNull(attribute.getName(), "must specify an attribute name.");
        loadAttributes(true);
        attributes.add(attribute.data);
    }

    public void clear() {
        removeAttributes();
    }

    /**
     * Retrieve the attribute at a given index.
     * @param index - the index to look up.
     * @return The attribute at that index.
     */
    public Attribute get(int index) {
        return new Attribute((NBTCompound) attributes.get(index));
    }

    /**
     * Retrieve the modified item stack.
     * @return The modified item stack.
     */
    public ItemStack getStack() {
        return stack;
    }

    @Override
    public Iterator<Attribute> iterator() {
        if (size() == 0) return emptyIterator;
        return Iterators.transform(attributes.iterator(), new Function<Object, Attribute>() {
            @Override
            public Attribute apply(@Nullable Object element) {
                return new Attribute((NBTCompound) element);
            }
        });
    }

    /**
     * Remove the first instance of the given attribute.
     * <p>
     * The attribute will be removed using its UUID.
     * @param attribute - the attribute to remove.
     * @return TRUE if the attribute was removed, FALSE otherwise.
     */
    public boolean remove(Attribute attribute) {
        UUID uuid = attribute.getUUID();
        for (Iterator<Attribute> it = iterator(); it.hasNext();) {
            if (it.next().getUUID().equals(uuid)) {
                it.remove();
                if (size() == 0) {
                    removeAttributes();
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieve the number of attributes.
     * @return Number of attributes.
     */
    public int size() {
        if (attributes == null) return 0;
        return attributes.size();
    }

    private void loadAttributes(boolean createIfMissing) {
        if (this.attributes == null) {
            NBTCompound nbt = NBTFactory.fromItemTag(this.stack);
            this.attributes = nbt.getList("AttributeModifiers", createIfMissing);
        }
    }

    private void removeAttributes() {
        NBTCompound nbt = NBTFactory.fromItemTag(this.stack);
        nbt.remove("AttributeModifiers");
        this.attributes = null;
    }

    public static class Attribute {
        private NBTCompound data;

        private Attribute(NBTCompound data) {
            this.data = data;
        }

        public double getAmount() {
            return data.getDouble("Amount", 0.0);
        }

        public String getName() {
            return data.getString("Name", null);
        }

        public Operation getOperation() {
            return Operation.values()[data.getInteger("Operation", 0)];
        }

        public AttributeType getType() {
            return AttributeType.fromId(data.getString("AttributeName", null));
        }

        public UUID getUUID() {
            return new UUID(data.getLong("UUIDMost", null), data.getLong("UUIDLeast", null));
        }

        public void setAmount(double amount) {
            data.put("Amount", amount);
        }

        public void setAttributeType(@Nonnull AttributeType type) {
            Preconditions.checkNotNull(type, "type cannot be null.");
            data.put("AttributeName", type.getID());
        }

        public void setName(@Nonnull String name) {
            Preconditions.checkNotNull(name, "name cannot be null.");
            data.put("Name", name);
        }

        public void setOperation(@Nonnull Operation operation) {
            Preconditions.checkNotNull(operation, "operation cannot be null.");
            data.put("Operation", operation.getID());
        }

        public void setUUID(@Nonnull UUID id) {
            Preconditions.checkNotNull(id, "id cannot be null.");
            data.put("UUIDLeast", id.getLeastSignificantBits());
            data.put("UUIDMost", id.getMostSignificantBits());
        }
    }

    public static enum AttributeType {
        GENERIC_MAX_HEALTH("generic.maxHealth"),
        GENERIC_FOLLOW_RANGE("generic.followRange"),
        GENERIC_ATTACK_DAMAGE("generic.attackDamage"),
        GENERIC_MOVEMENT_SPEED("generic.movementSpeed"),
        GENERIC_KNOCKBACK_RESISTANCE("generic.knockbackResistance");
        private final String id;

        private AttributeType(String id) {
            this.id = id;
        }

        /**
         * Retrieve the attribute type associated with a given ID.
         * @param id The ID to search for.
         * @return The attribute type, or null if not found.
         */
        public static AttributeType fromId(String id) {
            for (AttributeType type : values()) {
                if (type.id.equals(id)) return type;
            }
            return null;
        }

        /**
         * Retrieve the associated minecraft ID.
         * @return The associated ID.
         */
        public String getID() {
            return id;
        }
    }

    public enum Operation {
        ADD_NUMBER,
        MULTIPLY_PERCENTAGE,
        ADD_PERCENTAGE;
        public int getID() {
            Operation[] values = values();
            for (int i = 0; i < values.length; i++) {
                Operation operation = values[i];
                if (this == operation) return i;
            }
            return 0;
        }
    }
}
