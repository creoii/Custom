package creoii.custom.eventsystem.condition;

import com.google.gson.JsonObject;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntitySneakingCondition extends Condition {
    private final boolean useTargetPosition;

    public EntitySneakingCondition(boolean useTargetPosition) {
        super(Condition.ENTITY_SNEAKING);
        this.useTargetPosition = useTargetPosition;
    }

    public static Condition getFromJson(JsonObject object) {
        boolean useTargetPosition = JsonHelper.getBoolean(object, "use_target_position", false);
        return new EntitySneakingCondition(useTargetPosition);
    }

    @Override
    public boolean testBlock(World world, BlockState state, BlockPos pos, LivingEntity living, Hand hand) {
        return living.isSneaking();
    }

    @Override
    public boolean testItem(World world, ItemStack stack, BlockPos pos, PlayerEntity player, Hand hand) {
        return player.isSneaking();
    }

    @Override
    public boolean testEntity(Entity entity, PlayerEntity player, Hand hand) {
        return entity.isSneaking();
    }

    @Override
    public boolean testEnchantment(Entity user, Entity target, int level) {
        return useTargetPosition ? target.isSneaking() : user.isSneaking();
    }

    @Override
    public boolean testStatusEffect(StatusEffect statusEffect, LivingEntity entity, int amplifier) {
        return entity.isSneaking();
    }
}
