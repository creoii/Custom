package creoii.custom.eventsystem.event;

import com.google.gson.JsonObject;
import creoii.custom.eventsystem.condition.Condition;
import creoii.custom.eventsystem.effect.Effect;
import creoii.custom.util.StringToObject;
import net.minecraft.util.ActionResult;
import net.minecraft.util.JsonHelper;

public class LeftClickEvent extends Event {
    public LeftClickEvent(Condition[] conditions, Effect[] effects) {
        super(Event.LEFT_CLICK, conditions, effects);
    }

    public static Event getFromJson(JsonObject object) {
        Condition[] conditions = Event.getConditions(object);
        Effect[] effects = Event.getEffects(object);
        return new LeftClickEvent(conditions, effects);
    }
}
