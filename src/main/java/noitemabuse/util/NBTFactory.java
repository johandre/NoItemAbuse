/* This file is part of NoItemAbuse (GPL v2 or later), see LICENSE.md */
package noitemabuse.util;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.*;
import org.bukkit.inventory.ItemStack;

import com.google.common.base.Splitter;
import com.google.common.collect.*;
import com.google.common.primitives.Primitives;

public class NBTFactory {
    private static final BiMap<Integer, Class<?>> classes = HashBiMap.create();
    private static final BiMap<Integer, NBTType> types = HashBiMap.create();
    private static Class<?> baseClass;
    private static Class<?> compoundClass;
    static Method nbtCreateTag;
    static Method nbtGetType;
    static Field nbtListType;
    private static final Field[] dataField = new Field[12];
    static Class<?> craftItemStack;
    static Field craftHandle;
    static Field stackTag;
    private static NBTFactory instance;

    /**
     * Construct an instance of the NBT factory by deducing the class of NBTBase.
     */
    private NBTFactory() {
        if (baseClass == null) {
            try {
                ClassLoader loader = NBTFactory.class.getClassLoader();
                String packageName = getPackageName();
                Class<?> offlinePlayer = loader.loadClass(packageName + ".CraftOfflinePlayer");
                // Prepare NBT
                compoundClass = getMethod(0, Modifier.STATIC, offlinePlayer, "getData").getReturnType();
                baseClass = compoundClass.getSuperclass();
                nbtGetType = getMethod(0, Modifier.STATIC, baseClass, "getTypeId");
                nbtCreateTag = getMethod(Modifier.STATIC, 0, baseClass, "createTag", byte.class);
                // Prepare CraftItemStack
                craftItemStack = loader.loadClass(packageName + ".inventory.CraftItemStack");
                craftHandle = getField(null, craftItemStack, "handle");
                stackTag = getField(null, craftHandle.getType(), "tag");
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Unable to find offline player.", e);
            }
        }
    }

    /**
     * Construct a new NBT compound.
     * <p>
     * Use {@link NBTCompound#asMap()} to modify it.
     * @return The NBT compound.
     */
    public static NBTCompound createCompound() {
        return get().new NBTCompound(instance.createNBTTag(NBTType.TAG_COMPOUND, null));
    }

    /**
     * Construct a new NBT list of an unspecified type.
     */
    public static NBTList createList(Iterable<? extends Object> iterable) {
        NBTList list = get().new NBTList(instance.createNBTTag(NBTType.TAG_LIST, null));
        // Add the content as well
        for (Object obj : iterable) {
            list.add(obj);
        }
        return list;
    }

    /**
     * Construct a new NBT list of an unspecified type.
     */
    public static NBTList createList(Object... content) {
        return createList(Arrays.asList(content));
    }

    /**
     * Construct a new NBT wrapper from a compound.
     * @param nmsCompound - the NBT compund.
     */
    public static NBTCompound fromCompound(Object nmsCompound) {
        return get().new NBTCompound(nmsCompound);
    }

    /**
     * Construct a wrapper for an NBT tag stored (in memory) in an item stack.
     * <p>
     * This is where auxillary data such as enchanting, name and lore is stored.
     * <p>
     * It does not include the item's material, damage value or count.
     * <p>
     * The item stack must be a wrapper for a CraftItemStack.
     * @param stack - the item stack.
     */
    public static NBTCompound fromItemTag(ItemStack stack) {
        checkItemStack(stack);
        Object nms = getFieldValue(get().craftHandle, stack);
        Object tag = getFieldValue(get().stackTag, nms);
        // Create the tag if it doesn't exist
        if (tag == null) {
            NBTCompound compound = createCompound();
            setItemTag(stack, compound);
            return compound;
        }
        return fromCompound(tag);
    }

    /**
     * Construct a new NBT wrapper from a list.
     * @param nmsList - the NBT list.
     */
    public static NBTList fromList(Object nmsList) {
        return get().new NBTList(nmsList);
    }

    /**
     * Retrieve a CraftItemStack version of the stack.
     * @param stack - the stack to convert.
     * @return The CraftItemStack version.
     */
    public static ItemStack getCraftItemStack(ItemStack stack) {
        get();
        if (stack == null || NBTFactory.craftItemStack.isAssignableFrom(stack.getClass())) return stack;
        try {
            Constructor<?> caller = NBTFactory.craftItemStack.getDeclaredConstructor(ItemStack.class);
            caller.setAccessible(true);
            return (ItemStack) caller.newInstance(stack);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to convert " + stack + " + to a CraftItemStack.", e);
        }
    }

    /**
     * Set the NBT compound tag of a given item stack.
     * <p>
     * The item stack must be a wrapper for a CraftItemStack. Use {@link MinecraftReflection#getBukkitItemStack(ItemStack)} if not.
     * @param stack - the item stack, cannot be air.
     * @param compound - the new NBT compound, or NULL to remove it.
     * @throws IllegalArgumentException If the stack is not a CraftItemStack, or it represents air.
     */
    public static void setItemTag(ItemStack stack, NBTCompound compound) {
        checkItemStack(stack);
        Object nms = getFieldValue(get().craftHandle, stack);
        // Update the tag compound
        setFieldValue(get().stackTag, nms, compound.getHandle());
    }

    /**
     * Ensure that the given stack can store arbitrary NBT information.
     * @param item - the item to check.
     */
    private static void checkItemStack(ItemStack item) {
        if (item == null) throw new IllegalArgumentException("Stack cannot be null.");
        get();
        if (!NBTFactory.craftItemStack.isAssignableFrom(item.getClass())) throw new IllegalArgumentException("item must be a CraftItemStack.");
        if (item.getType() == Material.AIR) throw new IllegalArgumentException("Item cannot be air");
    }

    /**
     * Retrieve or construct a shared NBT factory.
     * @return The factory.
     */
    private static NBTFactory get() {
        if (instance == null) {
            instance = new NBTFactory();
        }
        return instance;
    }

    /**
     * Search for the first publically and privately defined field of the given name.
     * @param instance - an instance of the class with the field.
     * @param clazz - an optional class to start with, or NULL to deduce it from instance.
     * @param fieldName - the field name.
     * @return The first field by this name.
     * @throws IllegalStateException If we cannot find this field.
     */
    private static Field getField(Object instance, Class<?> clazz, String fieldName) {
        if (clazz == null) {
            clazz = instance.getClass();
        }
        // Ignore access rules
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getName().equals(fieldName)) {
                field.setAccessible(true);
                return field;
            }
        }
        // Recursively find the correct field
        if (clazz.getSuperclass() != null) return getField(instance, clazz.getSuperclass(), fieldName);
        throw new IllegalStateException("Unable to find field " + fieldName + " in " + instance);
    }

    private static Object getFieldValue(Field field, Object target) {
        try {
            return field.get(target);
        } catch (Exception e) {
            throw new RuntimeException("Unable to retrieve " + field + " for " + target, e);
        }
    }

    /**
     * Search for the first publically and privately defined method of the given name and parameter count.
     * @param modifiers - modifiers that are required.
     * @param disallowedModifiers - modifiers that are banned.
     * @param clazz - a class to start with.
     * @param methodName - the method name, or NULL to skip.
     * @param params - the expected parameters.
     * @return The first method by this name.
     * @throws IllegalStateException If we cannot find this method.
     */
    private static Method getMethod(int modifiers, int disallowedModifiers, Class<?> clazz, String methodName, Class<?>... params) {
        for (Method method : clazz.getDeclaredMethods()) {
            // Limitation: Doesn't handle overloads
            if ((method.getModifiers() & modifiers) == modifiers && (method.getModifiers() & disallowedModifiers) == 0 && (methodName == null || method.getName().equals(methodName))
                    && Arrays.equals(method.getParameterTypes(), params)) {
                method.setAccessible(true);
                return method;
            }
        }
        // Search in every superclass
        if (clazz.getSuperclass() != null) return getMethod(modifiers, disallowedModifiers, clazz.getSuperclass(), methodName, params);
        throw new IllegalStateException(String.format("Unable to find method %s (%s).", methodName, Arrays.asList(params)));
    }

    /**
     * Invoke a method on the given target instance using the provided parameters.
     * @param method - the method to invoke.
     * @param target - the target.
     * @param params - the parameters to supply.
     * @return The result of the method.
     */
    private static Object invokeMethod(Method method, Object target, Object... params) {
        try {
            return method.invoke(target, params);
        } catch (Exception e) {
            throw new RuntimeException("Unable to invoke method " + method + " for " + target, e);
        }
    }

    private static void setFieldValue(Field field, Object target, Object value) {
        try {
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Unable to set " + field + " for " + target, e);
        }
    }

    /**
     * Construct a new NMS NBT tag initialized with the given value.
     * @param type - the NBT type.
     * @param value - the value, or NULL to keep the original value.
     * @return The created tag.
     */
    private Object createNBTTag(NBTType type, Object value) {
        Object tag = invokeMethod(nbtCreateTag, null, (byte) type.id);
        if (value != null) {
            setFieldValue(getDataField(type, tag), tag, value);
        }
        return tag;
    }

    /**
     * Retrieve the field where the NBT class stores its value.
     * @param type - the NBT type.
     * @param nms - the NBT class instance.
     * @return The corresponding field.
     */
    private Field getDataField(NBTType type, Object nms) {
        if (dataField[type.id] == null) {
            dataField[type.id] = getField(nms, null, type.getFieldName());
        }
        return dataField[type.id];
    }

    @SuppressWarnings("unchecked")
    private List<Object> getDataList(Object handle) {
        return (List<Object>) getFieldValue(getDataField(NBTType.TAG_LIST, handle), handle);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getDataMap(Object handle) {
        return (Map<String, Object>) getFieldValue(getDataField(NBTType.TAG_COMPOUND, handle), handle);
    }

    /**
     * Retrieve the NBT type from a given NMS NBT tag.
     * @param nms - the native NBT tag.
     * @return The corresponding type.
     */
    private NBTType getNBTType(Object nms) {
        int type = (Byte) invokeMethod(nbtGetType, nms);
        return types.get(type);
    }

    private String getPackageName() {
        return Bukkit.getServer().getClass().getPackage().getName();
    }

    /**
     * Retrieve the nearest NBT type for a given primitive type.
     * @param primitive - the primitive type.
     * @return The corresponding type.
     */
    private NBTType getPrimitiveType(Object primitive) {
        NBTType type = types.get(classes.inverse().get(Primitives.unwrap(primitive.getClass())));
        // Display the illegal value at least
        if (type == null) throw new IllegalArgumentException(String.format("Illegal type: %s (%s)", primitive.getClass(), primitive));
        return type;
    }

    /**
     * Convert wrapped List and Map objects into their respective NBT counterparts.
     * @param name - the name of the NBT element to create.
     * @param value - the value of the element to create. Can be a List or a Map.
     * @return The NBT element.
     */
    private Object unwrapValue(Object value) {
        if (value == null) return null;
        if (value instanceof Wrapper) return ((Wrapper) value).getHandle();
        else if (value instanceof List) throw new IllegalArgumentException("Can only insert a WrappedList.");
        else if (value instanceof Map) throw new IllegalArgumentException("Can only insert a WrappedCompound.");
        else return createNBTTag(getPrimitiveType(value), value);
    }

    /**
     * Convert a given NBT element to a primitive wrapper or List/Map equivalent.
     * <p>
     * All changes to any mutable objects will be reflected in the underlying NBT element(s).
     * @param nms - the NBT element.
     * @return The wrapper equivalent.
     */
    private Object wrapNative(Object nms) {
        if (nms == null) return null;
        if (baseClass.isAssignableFrom(nms.getClass())) {
            final NBTType type = getNBTType(nms);
            // Handle the different types
            switch (type) {
            case TAG_COMPOUND:
                return new NBTCompound(nms);
            case TAG_LIST:
                return new NBTList(nms);
            default:
                return getFieldValue(getDataField(type, nms), nms);
            }
        }
        throw new IllegalArgumentException("Unexpected type: " + nms);
    }

    /**
     * Represents a root NBT compound.
     * <p>
     * All changes to this map will be reflected in the underlying NBT compound. Values may only be one of the following:
     * <ul>
     * <li>Primitive types</li>
     * <li>{@link java.lang.String String}</li>
     * <li>{@link NBTList}</li>
     * <li>{@link NBTCompound}</li>
     * </ul>
     * <p>
     * See also:
     * <ul>
     * <li>{@link NBTFactory#createCompound()}</li>
     * <li>{@link NBTFactory#fromCompound(Object)}</li>
     * </ul>
     */
    public final class NBTCompound extends ConvertedMap {
        private NBTCompound(Object handle) {
            super(handle, getDataMap(handle));
        }

        // Simplifiying access to each value
        public Byte getByte(String key, Byte defaultValue) {
            return containsKey(key) ? (Byte) get(key) : defaultValue;
        }

        public byte[] getByteArray(String key, byte[] defaultValue) {
            return containsKey(key) ? (byte[]) get(key) : defaultValue;
        }

        public Double getDouble(String key, Double defaultValue) {
            return containsKey(key) ? (Double) get(key) : defaultValue;
        }

        public Float getFloat(String key, Float defaultValue) {
            return containsKey(key) ? (Float) get(key) : defaultValue;
        }

        public Integer getInteger(String key, Integer defaultValue) {
            return containsKey(key) ? (Integer) get(key) : defaultValue;
        }

        public int[] getIntegerArray(String key, int[] defaultValue) {
            return containsKey(key) ? (int[]) get(key) : defaultValue;
        }

        /**
         * Retrieve the list by the given name.
         * @param key - the name of the list.
         * @param createNew - whether or not to create a new list if its missing.
         * @return An existing list, a new list or NULL.
         */
        public NBTList getList(String key, boolean createNew) {
            NBTList list = (NBTList) get(key);
            if (list == null && createNew) {
                put(key, list = createList());
            }
            return list;
        }

        public Long getLong(String key, Long defaultValue) {
            return containsKey(key) ? (Long) get(key) : defaultValue;
        }

        /**
         * Retrieve the map by the given name.
         * @param key - the name of the map.
         * @param createNew - whether or not to create a new map if its missing.
         * @return An existing map, a new map or NULL.
         */
        public NBTCompound getMap(String key, boolean createNew) {
            return getMap(Arrays.asList(key), createNew);
        }

        /**
         * Retrieve the value of a given entry in the tree.
         * <p>
         * Every element of the path (except the end) are assumed to be compounds. The retrieval operation will be cancelled if any of them are missing.
         * @param path - path to the entry.
         * @return The value, or null if not found.
         */
        @SuppressWarnings("unchecked")
        public <T> T getPath(String path) {
            List<String> entries = getPathElements(path);
            NBTCompound map = getMap(entries.subList(0, entries.size() - 1), false);
            if (map != null) return (T) map.get(entries.get(entries.size() - 1));
            return null;
        }

        public Short getShort(String key, Short defaultValue) {
            return containsKey(key) ? (Short) get(key) : defaultValue;
        }

        public String getString(String key, String defaultValue) {
            return containsKey(key) ? (String) get(key) : defaultValue;
        }

        // Done
        /**
         * Set the value of an entry at a given location.
         * <p>
         * Every element of the path (except the end) are assumed to be compounds, and will be created if they are missing.
         * @param path - the path to the entry.
         * @param value - the new value of this entry.
         * @return This compound, for chaining.
         */
        public NBTCompound putPath(String path, Object value) {
            List<String> entries = getPathElements(path);
            Map<String, Object> map = getMap(entries.subList(0, entries.size() - 1), true);
            map.put(entries.get(entries.size() - 1), value);
            return this;
        }

        /**
         * Retrieve a map from a given path.
         * @param path - path of compounds to look up.
         * @param createNew - whether or not to create new compounds on the way.
         * @return The map at this location.
         */
        private NBTCompound getMap(Iterable<String> path, boolean createNew) {
            NBTCompound compound = this;
            for (String entry : path) {
                NBTCompound child = (NBTCompound) compound.get(entry);
                if (child == null) {
                    if (!createNew) return null;
                    compound.put(entry, child = createCompound());
                }
                compound = child;
            }
            return compound;
        }

        /**
         * Split the path into separate elements.
         * @param path - the path to split.
         * @return The elements.
         */
        private List<String> getPathElements(String path) {
            return Lists.newArrayList(Splitter.on(".").omitEmptyStrings().split(path));
        }
    }

    /**
     * Represents a root NBT list. See also:
     * <ul>
     * <li>{@link NBTFactory#createNBTList()}</li>
     * <li>{@link NBTFactory#fromList(Object)}</li>
     * </ul>
     */
    public final class NBTList extends ConvertedList {
        private NBTList(Object handle) {
            super(handle, getDataList(handle));
        }
    }

    /**
     * Represents an object that provides a view of a native NMS class.
     */
    public static interface Wrapper {
        /**
         * Retrieve the underlying native NBT tag.
         * @return The underlying NBT.
         */
        public Object getHandle();
    }

    /**
     * Represents a class for caching wrappers.
     */
    private final class CachedNativeWrapper {
        // Don't recreate wrapper objects
        private final ConcurrentMap<Object, Object> cache = new MapMaker().weakKeys().makeMap();

        public Object wrap(Object value) {
            Object current = cache.get(value);
            if (current == null) {
                current = wrapNative(value);
                // Only cache composite objects
                if (current instanceof ConvertedMap || current instanceof ConvertedList) {
                    cache.put(value, current);
                }
            }
            return current;
        }
    }

    /**
     * Represents a list that wraps another list and converts elements of its type and another exposed type.
     */
    private class ConvertedList extends AbstractList<Object> implements Wrapper {
        private final Object handle;
        private final List<Object> original;
        private final CachedNativeWrapper cache = new CachedNativeWrapper();

        public ConvertedList(Object handle, List<Object> original) {
            if (nbtListType == null) {
                nbtListType = getField(handle, null, "type");
            }
            this.handle = handle;
            this.original = original;
        }

        @Override
        public void add(int index, Object element) {
            Object nbt = unwrapIncoming(element);
            // Set the list type if its the first element
            if (size() == 0) {
                setFieldValue(nbtListType, handle, (byte) getNBTType(nbt).id);
            }
            original.add(index, nbt);
        }

        @Override
        public Object get(int index) {
            return wrapOutgoing(original.get(index));
        }

        @Override
        public Object getHandle() {
            return handle;
        }

        @Override
        public Object remove(int index) {
            return wrapOutgoing(original.remove(index));
        }

        @Override
        public boolean remove(Object o) {
            return original.remove(unwrapIncoming(o));
        }

        @Override
        public Object set(int index, Object element) {
            return wrapOutgoing(original.set(index, unwrapIncoming(element)));
        }

        @Override
        public int size() {
            return original.size();
        }

        protected Object unwrapIncoming(Object wrapped) {
            return unwrapValue(wrapped);
        }

        protected Object wrapOutgoing(Object value) {
            return cache.wrap(value);
        }
    }

    /**
     * Represents a map that wraps another map and automatically converts entries of its type and another exposed type.
     */
    private class ConvertedMap extends AbstractMap<String, Object> implements Wrapper {
        private final Object handle;
        private final Map<String, Object> original;
        private final CachedNativeWrapper cache = new CachedNativeWrapper();

        public ConvertedMap(Object handle, Map<String, Object> original) {
            this.handle = handle;
            this.original = original;
        }

        @Override
        public boolean containsKey(Object key) {
            return original.containsKey(key);
        }

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return new AbstractSet<Entry<String, Object>>() {
                @Override
                public boolean add(Entry<String, Object> e) {
                    String key = e.getKey();
                    Object value = e.getValue();
                    original.put(key, unwrapIncoming(value));
                    return true;
                }

                @Override
                public Iterator<Entry<String, Object>> iterator() {
                    return ConvertedMap.this.iterator();
                }

                @Override
                public int size() {
                    return original.size();
                }
            };
        }

        // Performance
        @Override
        public Object get(Object key) {
            return wrapOutgoing(original.get(key));
        }

        @Override
        public Object getHandle() {
            return handle;
        }

        // Modification
        @Override
        public Object put(String key, Object value) {
            return wrapOutgoing(original.put(key, unwrapIncoming(value)));
        }

        @Override
        public Object remove(Object key) {
            return wrapOutgoing(original.remove(key));
        }

        protected Object unwrapIncoming(Object wrapped) {
            return unwrapValue(wrapped);
        }

        // For converting back and forth
        protected Object wrapOutgoing(Object value) {
            return cache.wrap(value);
        }

        private Iterator<Entry<String, Object>> iterator() {
            final Iterator<Entry<String, Object>> proxy = original.entrySet().iterator();
            return new Iterator<Entry<String, Object>>() {
                @Override
                public boolean hasNext() {
                    return proxy.hasNext();
                }

                @Override
                public Entry<String, Object> next() {
                    Entry<String, Object> entry = proxy.next();
                    return new SimpleEntry<String, Object>(entry.getKey(), wrapOutgoing(entry.getValue()));
                }

                @Override
                public void remove() {
                    proxy.remove();
                }
            };
        }
    }

    private enum NBTType {
        TAG_END(0, Void.class),
        TAG_BYTE(1, byte.class),
        TAG_SHORT(2, short.class),
        TAG_INT(3, int.class),
        TAG_LONG(4, long.class),
        TAG_FLOAT(5, float.class),
        TAG_DOUBLE(6, double.class),
        TAG_BYTE_ARRAY(7, byte[].class),
        TAG_INT_ARRAY(11, int[].class),
        TAG_STRING(8, String.class),
        TAG_LIST(9, List.class),
        TAG_COMPOUND(10, Map.class);
        // Unique NBT id
        public final int id;

        private NBTType(int id, Class<?> type) {
            this.id = id;
            classes.put(id, type);
            types.put(id, this);
        }

        private String getFieldName() {
            if (this == TAG_COMPOUND) return "map";
            else if (this == TAG_LIST) return "list";
            else return "data";
        }
    }
}
