package creoii.custom.eventsystem.event;

import com.google.gson.JsonObject;
import creoii.custom.eventsystem.condition.Condition;
import creoii.custom.eventsystem.effect.Effect;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.concurrent.atomic.AtomicBoolean;

public class TargetDamagedEvent extends Event {
    public TargetDamagedEvent(Condition[] conditions, Effect[] effects) {
        super(Event.TARGET_DAMAGED, conditions, effects);
    }

    public static Event getFromJson(JsonObject object) {
        Condition[] conditions = Event.getConditions(object);
        Effect[] effects = Event.getEffects(object);
        return new TargetDamagedEvent(conditions, effects);
    }
}
