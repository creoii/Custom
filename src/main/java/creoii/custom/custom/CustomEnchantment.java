package creoii.custom.custom;

import com.google.gson.*;
import creoii.custom.data.CustomObject;
import creoii.custom.eventsystem.event.Event;
import creoii.custom.util.StringToObject;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.lang.reflect.Type;

public class CustomEnchantment extends Enchantment implements CustomObject {
    private final Identifier identifier;
    private final boolean offeredByLibrarians;
    private final boolean randomlySelectable;
    private final int minPlayerLevel;
    private final int maxPlayerLevel;
    private final int maxLevel;
    private final int minLevel;
    private final Identifier[] blacklist;
    private final Event[] events;

    public CustomEnchantment(
            Identifier identifier,
            Rarity rarity, EnchantmentTarget type, EquipmentSlot[] slotTypes,
            boolean offeredByLibrarians, boolean randomlySelectable,
            int minPlayerLevel, int maxPlayerLevel, int maxLevel, int minLevel,
            Identifier[] blacklist, Event[] events
    ) {
        super(rarity, type, slotTypes);
        this.identifier = identifier;
        this.offeredByLibrarians = offeredByLibrarians;
        this.randomlySelectable = randomlySelectable;
        this.minPlayerLevel = minPlayerLevel;
        this.maxPlayerLevel = maxPlayerLevel;
        this.maxLevel = maxLevel;
        this.minLevel = minLevel;
        this.blacklist = blacklist;
        this.events = events;

        Registry.register(Registry.ENCHANTMENT, identifier, this);
    }

    @Override
    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    protected boolean canAccept(Enchantment other) {
        for (Identifier id : blacklist) {
            if (Registry.ENCHANTMENT.get(id) == other) {
                return false;
            }
        }
        return super.canAccept(other);
    }

    @Override
    public void onTargetDamaged(LivingEntity user, Entity target, int level) {
        super.onTargetDamaged(user, target, level);
        Event event = Event.findEvent(events, Event.TARGET_DAMAGED);
        if (event != null) {
            event.applyEnchantmentEvent(user, target, level);
        }
    }

    @Override
    public void onUserDamaged(LivingEntity user, Entity attacker, int level) {
        super.onUserDamaged(user, attacker, level);
        Event event = Event.findEvent(events, Event.USER_DAMAGED);
        if (event != null) {
            System.out.println("user damaged");
            event.applyEnchantmentEvent(user, attacker, level);
        }
    }

    @Override
    public boolean isAvailableForEnchantedBookOffer() {
        return offeredByLibrarians;
    }

    @Override
    public boolean isAvailableForRandomSelection() {
        return randomlySelectable;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public int getMaxPower(int level) {
        return maxPlayerLevel;
    }

    @Override
    public int getMinPower(int level) {
        return minPlayerLevel;
    }

    @Override
    public int getMinLevel() {
        return minLevel;
    }

    public static class Serializer implements JsonDeserializer<CustomEnchantment>, JsonSerializer<CustomEnchantment> {
        @Override
        public CustomEnchantment deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = JsonHelper.asObject(json, "enchantment");
            Identifier identifier = Identifier.tryParse(JsonHelper.getString(object, "identifier"));
            Rarity rarity = StringToObject.enchantmentRarity(JsonHelper.getString(object, "rarity", "common"));
            EnchantmentTarget target = StringToObject.enchantmentTarget(JsonHelper.getString(object, "target", "breakable"));
            EquipmentSlot[] slots;
            if (JsonHelper.hasArray(object, "equipment_slots")) {
                JsonArray array = JsonHelper.getArray(object, "equipment_slots");
                slots = new EquipmentSlot[array.size()];
                for (int i = 0; i < slots.length; ++i) {
                    if (array.get(i).isJsonPrimitive()) slots[i] = StringToObject.equipmentSlot(array.get(i).getAsString());
                }
            } else slots = new EquipmentSlot[]{};
            boolean offeredByLibrarians = JsonHelper.getBoolean(object, "offered_by_librarians", true);
            boolean randomlySelectable = JsonHelper.getBoolean(object, "randomly_selectable", true);
            int minPlayerLevel = JsonHelper.getInt(object, "min_player_level", 1);
            int maxPlayerLevel = JsonHelper.getInt(object, "max_player_level", 30);
            int maxLevel = JsonHelper.getInt(object, "max_level", 1);
            int minLevel = JsonHelper.getInt(object, "min_level", 1);
            Identifier[] blacklist;
            if (JsonHelper.hasArray(object, "blacklist")) {
                JsonArray array = JsonHelper.getArray(object, "blacklist");
                blacklist = new Identifier[array.size()];
                for (int i = 0; i < blacklist.length; ++i) {
                    if (array.get(i).isJsonPrimitive()) blacklist[i] = Identifier.tryParse(array.get(i).getAsString());
                }
            } else blacklist = new Identifier[]{};
            Event[] events;
            if (JsonHelper.hasArray(object, "events")) {
                JsonArray array = JsonHelper.getArray(object, "events");
                events = new Event[array.size()];
                if (events.length > 0) {
                    for (int i = 0; i < events.length; ++i) {
                        if (array.get(i).isJsonObject()) {
                            JsonObject eventObj = array.get(i).getAsJsonObject();
                            events[i] = Event.getEvent(eventObj, eventObj.get("type").getAsString());
                        }
                    }
                }
            } else events = new Event[]{};
            return new CustomEnchantment(identifier, rarity, target, slots,
                    offeredByLibrarians, randomlySelectable,
                    minPlayerLevel, maxPlayerLevel, maxLevel, minLevel,
                    blacklist, events
            );
        }

        @Override
        public JsonElement serialize(CustomEnchantment src, Type typeOfSrc, JsonSerializationContext context) {
            return null;
        }
    }
}
