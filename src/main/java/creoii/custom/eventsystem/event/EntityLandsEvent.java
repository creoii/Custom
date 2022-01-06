package creoii.custom.eventsystem.event;

import com.google.gson.JsonObject;
import creoii.custom.eventsystem.condition.Condition;
import creoii.custom.eventsystem.effect.Effect;
import net.minecraft.entity.Entity;

public class EntityLandsEvent extends Event {
    private Entity entity;

    public EntityLandsEvent(Condition[] conditions, Effect[] effects) {
        super(Event.ENTITY_LANDS, conditions, effects);
    }

    public static Event getFromJson(JsonObject object) {
        Condition[] conditions = Event.getConditions(object);
        Effect[] effects = Event.getEffects(object);
        return new EntityLandsEvent(conditions, effects);
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}
