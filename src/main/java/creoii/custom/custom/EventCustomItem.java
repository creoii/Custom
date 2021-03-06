package creoii.custom.custom;

import creoii.custom.eventsystem.event.Event;
import creoii.custom.eventsystem.event.RightClickEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;

public class EventCustomItem extends CustomItem {
    private final Event[] events;

    public EventCustomItem(Identifier identifier, Settings settings, FoodComponent food, Text tooltipText, Event[] events) {
        super(identifier, settings, food, tooltipText);
        this.events = events;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        Event event = Event.findEvent(events, Event.RIGHT_CLICK);
        if (event != null) {
            if (event.applyItemEvent(context.getWorld(), context.getStack(), context.getBlockPos(), context.getPlayer(), context.getHand())) {
                return ((RightClickEvent) event).getActionResult();
            }
        }
        return super.useOnBlock(context);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        Event event = Event.findEvent(events, Event.RIGHT_CLICK);
        if (event != null) {
            if (event.applyItemEvent(user.world, stack, entity.getBlockPos(), user, hand)) {
                return ((RightClickEvent) event).getActionResult();
            }
        }
        return super.useOnEntity(stack, user, entity, hand);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        Event event = Event.findEvent(events, Event.STOPPED_USING);
        if (event != null && user instanceof PlayerEntity player) {
            event.applyItemEvent(user.world, stack, player.getBlockPos(), player, player.getActiveHand());
        }
        super.onStoppedUsing(stack, world, user, remainingUseTicks);
    }

    @Override
    public void onCraft(ItemStack stack, World world, PlayerEntity player) {
        Event event = Event.findEvent(events, Event.CRAFTED);
        if (event != null) {
            event.applyItemEvent(world, stack, player.getBlockPos(), player, player.getActiveHand());
        }
        super.onCraft(stack, world, player);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        Event event = Event.findEvent(events, Event.RIGHT_CLICK);
        if (event != null) {
            event.applyItemEvent(user.world, user.getStackInHand(hand), user.getBlockPos(), user, hand);
        }
        return super.use(world, user, hand);
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        Event event = Event.findEvent(events, Event.LEFT_CLICK);
        if (event != null) {
            event.applyItemEvent(player.world, stack, player.getBlockPos(), player, player.getActiveHand());
        }
        return super.onClicked(stack, otherStack, slot, clickType, player, cursorStackReference);
    }
}
