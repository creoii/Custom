package creoii.custom.eventsystem.effect;

import com.google.gson.JsonObject;
import creoii.custom.util.math.ValueHolder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DropItemEffect extends Effect {
    private final Item item;
    private final ValueHolder amount;

    private ItemEntity itemEntity;

    public DropItemEffect(Item item, ValueHolder amount) {
        super(Effect.DROP_ITEM);
        this.item = item;
        this.amount = amount;
    }

    public static Effect getFromJson(JsonObject object) {
        Item item = JsonHelper.getItem(object, "item", Items.AIR);
        ValueHolder amount = ValueHolder.getFromJson(object, "amount");
        return new DropItemEffect(item, amount);
    }

    @Override
    public void runBlock(World world, BlockState state, BlockPos pos, LivingEntity living, Hand hand) {
        itemEntity = new ItemEntity(world, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, new ItemStack(item, (int) amount.getValue()));
        itemEntity.setToDefaultPickupDelay();
        world.spawnEntity(itemEntity);
    }

    @Override
    public void runItem(World world, ItemStack stack, BlockPos pos, PlayerEntity player, Hand hand) {
        itemEntity = new ItemEntity(world, pos.getX() + .5f, pos.getY() + .5f, pos.getZ() + .5f, new ItemStack(item, (int) amount.getValue()));
        itemEntity.setToDefaultPickupDelay();
        world.spawnEntity(itemEntity);
    }

    @Override
    public void runEntity(Entity entity, PlayerEntity player, Hand hand) {
        itemEntity = new ItemEntity(entity.world, entity.getX() + .5f, entity.getY() + .5f, entity.getZ() + .5f, new ItemStack(item, (int) amount.getValue()));
        itemEntity.setToDefaultPickupDelay();
        entity.world.spawnEntity(itemEntity);
    }

    @Override
    public void runEnchantment(Entity user, Entity target, int level) {
        itemEntity = new ItemEntity(target.world, target.getX() + .5f, target.getY() + .5f, target.getZ() + .5f, new ItemStack(item, (int) amount.getValue()));
        itemEntity.setToDefaultPickupDelay();
        target.world.spawnEntity(itemEntity);
    }

    @Override
    public void runStatusEffect(StatusEffect statusEffect, LivingEntity entity, int amplifier) {
        itemEntity = new ItemEntity(entity.world, entity.getX() + .5f, entity.getY() + .5f, entity.getZ() + .5f, new ItemStack(item, (int) amount.getValue()));
        itemEntity.setToDefaultPickupDelay();
        entity.world.spawnEntity(itemEntity);
    }

    @Override
    public void runWorld(World world) { }
}
