package creoii.custom.custom;

import com.google.gson.*;
import creoii.custom.data.CustomObject;
import creoii.custom.eventsystem.event.EntityLandsEvent;
import creoii.custom.eventsystem.event.Event;
import creoii.custom.eventsystem.event.NeighborUpdateEvent;
import creoii.custom.eventsystem.event.RightClickEvent;
import creoii.custom.util.BlockUtil;
import creoii.custom.util.CustomJsonHelper;
import creoii.custom.util.StringToObject;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Random;

public class CustomBlock extends Block implements CustomObject, Waterloggable {
    private final Identifier identifier;
    private final AbstractBlock.Settings blockSettings;
    private final Item.Settings itemSettings;
    private final boolean placeableOnLiquid;
    private final boolean waterloggable;
    private final int redstonePower;
    private final int droppedXp;
    private final int fuelPower;
    private final float fallDamageMultiplier;
    private final float bounceVelocity;
    private final float slideVelocity;
    private final RenderLayer renderLayer;
    private final PathNodeType pathNodeType;
    private final OffsetType offsetType;
    private final FallingBlockData gravity;
    private final Shape shape;
    private final int flammability;
    private final int fireSpread;
    private final float compostChance;
    private final Event[] events;

    public CustomBlock(
            Identifier identifier, Settings blockSettings, Item.Settings itemSettings,
            boolean placeableOnLiquid, boolean waterloggable,
            int redstonePower, int droppedXp, int fuelPower,
            float fallDamageMultiplier, float bounceVelocity, float slideVelocity,
            RenderLayer renderLayer, PathNodeType pathNodeType, OffsetType offsetType,
            FallingBlockData gravity, Shape shape,
            int flammability, int fireSpread, float compostChance,
            Event[] events
    ) {
        super(blockSettings);
        setDefaultState(getDefaultState().with(Properties.WATERLOGGED, false));

        this.identifier = identifier;
        this.blockSettings = blockSettings;
        this.itemSettings = itemSettings;
        this.placeableOnLiquid = placeableOnLiquid;
        this.waterloggable = waterloggable;
        this.redstonePower = redstonePower;
        this.droppedXp = droppedXp;
        this.fuelPower = fuelPower;
        this.fallDamageMultiplier = fallDamageMultiplier;
        this.bounceVelocity = bounceVelocity;
        this.slideVelocity = slideVelocity;
        this.renderLayer = renderLayer;
        this.pathNodeType = pathNodeType;
        this.offsetType = offsetType;
        this.gravity = gravity;
        this.shape = shape;
        this.flammability = flammability;
        this.fireSpread = fireSpread;
        this.compostChance = compostChance;
        this.events = events;

        Registry.register(Registry.BLOCK, this.getIdentifier(), this);
        Registry.register(Registry.ITEM, this.getIdentifier(), new BlockItem(this, this.getItemSettings()));
        FuelRegistry.INSTANCE.add(this, this.getFuelPower());
        ((FireBlock) Blocks.FIRE).registerFlammableBlock(this, this.getFlammability(), this.getFireSpread());
        ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.put(this, this.getCompostChance());
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public Settings getBlockSettings() {
        return blockSettings;
    }

    public Item.Settings getItemSettings() {
        return itemSettings;
    }

    public boolean isPlaceableOnLiquid() {
        return placeableOnLiquid;
    }

    public int getRedstonePower() {
        return redstonePower;
    }

    public int getDroppedXp() {
        return droppedXp;
    }

    public int getFuelPower() {
        return fuelPower;
    }

    public RenderLayer getRenderLayer() {
        return renderLayer;
    }

    public PathNodeType getPathNodeType() {
        return pathNodeType;
    }

    @Override
    public OffsetType getOffsetType() {
        return offsetType;
    }

    public int getFlammability() {
        return flammability;
    }

    public int getFireSpread() {
        return fireSpread;
    }

    public float getCompostChance() {
        return compostChance;
    }

    public Event[] getEvents() {
        return events;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean emitsRedstonePower(BlockState state) {
        return getRedstonePower() > 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return getRedstonePower();
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (shape != null) return VoxelShapes.cuboid(shape.minX, shape.minY, shape.minZ, shape.maxX, shape.maxY, shape.maxZ);
        else return super.getOutlineShape(state, world, pos, context);
    }

    private VoxelShape unionAll(Shape[] shapes) {
        VoxelShape[] voxelShapes = new VoxelShape[shapes.length];
        for (int i = 0; i < shapes.length; ++i) {
            float x = shapes[i].minX;
            float y = shapes[i].minY;
            float z = shapes[i].minZ;
            float x1 = shapes[i].maxX;
            float y1 = shapes[i].maxY;
            float z1 = shapes[i].maxZ;
            voxelShapes[i] = VoxelShapes.cuboid(x, y, z, x1, y1, z1);
        }

        VoxelShape ret = null;
        int prev = 0;
        for (int i = 1; i < voxelShapes.length; ++i, ++prev) {
            ret = voxelShapes[prev];
            ret = VoxelShapes.union(ret, voxelShapes[i]);
        }
        return ret;
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        Event event = Event.findEvent(events, Event.ENTITY_LANDS);
        if (event != null) {
            ((EntityLandsEvent) event).setEntity(entity);
            event.applyBlockEvent(world, state, pos, null, null);
        }
        entity.handleFallDamage(fallDistance, this.fallDamageMultiplier, DamageSource.FALL);
    }

    @Override
    public void onEntityLand(BlockView world, Entity entity) {
        if (entity.bypassesLandingEffects()) {
            super.onEntityLand(world, entity);
        } else {
            BlockUtil.bounce(entity, this.bounceVelocity);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (this.gravity.affectedByGravity) world.createAndScheduleBlockTick(pos, this, this.gravity.delay);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (this.gravity.affectedByGravity) world.createAndScheduleBlockTick(pos, this, this.gravity.delay);
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (this.gravity.affectedByGravity) {
            if (!canFallThrough(world.getBlockState(pos.down())) || pos.getY() < world.getBottomY()) {
                return;
            }
            FallingBlockEntity fallingBlockEntity = new FallingBlockEntity(world, (double) pos.getX() + 0.5, pos.getY(), (double) pos.getZ() + 0.5, world.getBlockState(pos));
            world.spawnEntity(fallingBlockEntity);
        }
    }

    public static boolean canFallThrough(BlockState state) {
        Material material = state.getMaterial();
        return state.isAir() || state.isIn(BlockTags.FIRE) || material.isLiquid() || material.isReplaceable();
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (this.gravity.affectedByGravity) {
            if (random.nextInt(16) == 0 && FallingBlock.canFallThrough(world.getBlockState(pos.down()))) {
                double d = (double) pos.getX() + random.nextDouble();
                double e = (double) pos.getY() - 0.05;
                double f = (double) pos.getZ() + random.nextDouble();
                world.addParticle(new BlockStateParticleEffect(ParticleTypes.FALLING_DUST, state), d, e, f, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    public boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        return waterloggable && (!state.get(Properties.WATERLOGGED) && fluid == Fluids.WATER);
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (waterloggable) {
            if (!state.get(Properties.WATERLOGGED) && fluidState.getFluid() == Fluids.WATER) {
                if (!world.isClient()) {
                    world.setBlockState(pos, state.with(Properties.WATERLOGGED, true), Block.NOTIFY_ALL);
                    world.createAndScheduleFluidTick(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack tryDrainFluid(WorldAccess world, BlockPos pos, BlockState state) {
        if (waterloggable) {
            if (state.get(Properties.WATERLOGGED)) {
                world.setBlockState(pos, state.with(Properties.WATERLOGGED, false), Block.NOTIFY_ALL);
                if (!state.canPlaceAt(world, pos)) {
                    world.breakBlock(pos, true);
                }
                return new ItemStack(Items.WATER_BUCKET);
            }
            return ItemStack.EMPTY;
        }
        else return new ItemStack(Items.BUCKET);
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        Event event = Event.findEvent(events, Event.RIGHT_CLICK);
        if (event != null) {
            if (event.applyBlockEvent(world, state, pos, player, hand)) {
                return ((RightClickEvent) event).getActionResult();
            }
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        Event event = Event.findEvent(events, Event.STEPPED_ON);
        if (event != null && entity instanceof LivingEntity living) {
            event.applyBlockEvent(world, state, pos, living, living.getActiveHand());
        }
        super.onSteppedOn(world, pos, state, entity);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        Event event = Event.findEvent(events, Event.PROJECTILE_HIT);
        if (event != null && projectile.getOwner() instanceof LivingEntity living) {
            event.applyBlockEvent(world, state, hit.getBlockPos(), living, living.getActiveHand());
        }
        super.onProjectileHit(world, state, hit, projectile);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        Event event = Event.findEvent(events, Event.LEFT_CLICK);
        if (event != null) {
            event.applyBlockEvent(world, state, pos, player, player.getActiveHand());
        }
        super.onBlockBreakStart(state, world, pos, player);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        Event event = Event.findEvent(events, Event.NEIGHBOR_UPDATE);
        if (event != null) {
            NeighborUpdateEvent neighborUpdateEvent = (NeighborUpdateEvent) event;
            neighborUpdateEvent.setNeighborState(block.getDefaultState());
            neighborUpdateEvent.setNeighborPos(fromPos);
            neighborUpdateEvent.applyBlockEvent(world, state, pos, null, null);
        }
        super.neighborUpdate(state, world, pos, block, fromPos, notify);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
        Event event = Event.findEvent(events, Event.PLACE_BLOCK);
        if (event != null) {
            event.applyBlockEvent(world, state, pos, placer, placer.getActiveHand());
        }
        super.onPlaced(world, pos, state, placer, itemStack);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        Event event = Event.findEvent(events, Event.BREAK_BLOCK);
        if (event != null) {
            event.applyBlockEvent(world, state, pos, player, player.getActiveHand());
        }
        super.onBreak(world, pos, state, player);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        Event event = Event.findEvent(events, Event.ENTITY_COLLISION);
        if (event != null && entity instanceof LivingEntity living) {
            event.applyBlockEvent(world, state, pos, living, living.getActiveHand());
        }
        super.onEntityCollision(state, world, pos, entity);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.WATERLOGGED);
    }

    public static class Serializer implements JsonDeserializer<CustomBlock>, JsonSerializer<CustomBlock> {
        @Override
        public CustomBlock deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = JsonHelper.asObject(json, "block");
            AbstractBlock.Settings blockSettings;
            if (object.has("block_settings")) {
                blockSettings = CustomJsonHelper.getBlockSettings(JsonHelper.getObject(object, "block_settings"), "block settings");
            } else blockSettings = FabricBlockSettings.copy(Blocks.STONE);

            Item.Settings itemSettings;
            if (object.has("item_settings")) {
                itemSettings = CustomJsonHelper.getItemSettings(JsonHelper.getObject(object, "item_settings"), "item settings");
            } else itemSettings = new FabricItemSettings();

            boolean placeableOnLiquid = JsonHelper.getBoolean(object, "placeable_on_liquid", false);
            boolean waterloggable = JsonHelper.getBoolean(object, "waterloggable", false);
            int redstonePower = JsonHelper.getInt(object, "redstone_power", 0);
            int droppedXp = JsonHelper.getInt(object, "dropped_xp", 0);
            int fuelPower = JsonHelper.getInt(object, "fuel_power", 0);
            float fallDamageMultiplier = JsonHelper.getFloat(object, "fall_damage_multiplier", 1f);
            float bounceVelocity = JsonHelper.getFloat(object, "bounce_velocity_multiplier", 1f);
            float slideVelocity = JsonHelper.getFloat(object, "slide_velocity_multiplier", 1f);
            RenderLayer renderLayer = StringToObject.renderLayer(JsonHelper.getString(object, "render_layer", "solid"));
            PathNodeType pathNodeType = StringToObject.pathNodeType(JsonHelper.getString(object, "pathing_type", "walkable"));
            OffsetType offsetType = StringToObject.offsetType(JsonHelper.getString(object, "offset_type", "none"));
            FallingBlockData gravity = CustomJsonHelper.getFallingBlockData(object, "gravity");
            Shape shape = Shape.get(object, "shape");
            int flammability = JsonHelper.getInt(object, "flammability", 0);
            int fireSpread = JsonHelper.getInt(object, "fire_spread", 0);
            float compostChance = JsonHelper.getFloat(object, "compost_chance", 0f);
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
            return new CustomBlock(
                    Identifier.tryParse(JsonHelper.getString(object, "identifier")), blockSettings, itemSettings,
                    placeableOnLiquid, waterloggable,
                    redstonePower, droppedXp, fuelPower,
                    fallDamageMultiplier, bounceVelocity, slideVelocity,
                    renderLayer, pathNodeType, offsetType, gravity, shape,
                    flammability, fireSpread, compostChance,
                    events
            );
        }

        @Override
        public JsonElement serialize(CustomBlock src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = new JsonObject();
            object.addProperty("identifier", src.getIdentifier().toString());
            object.add("block_settings", context.serialize(src.getBlockSettings()));
            object.add("item_settings", context.serialize(src.getItemSettings()));
            object.addProperty("placeable_on_liquid", src.isPlaceableOnLiquid());
            object.addProperty("redstone_powder", src.getRedstonePower());
            object.addProperty("dropped_xp", src.getDroppedXp());
            object.addProperty("fuel_power", src.getFuelPower());
            object.add("render_layer", context.serialize(src.getRenderLayer()));
            object.add("pathing_type", context.serialize(src.getPathNodeType()));
            object.add("offset_type", context.serialize(src.getOffsetType()));
            object.addProperty("flammability", src.getFlammability());
            object.addProperty("compost_chance", src.getCompostChance());
            object.add("events", context.serialize(src.getEvents()));
            return object;
        }
    }

    public static class FallingBlockData {
        public boolean affectedByGravity;
        public int delay;
        public int dustColor;

        public FallingBlockData(boolean affectedByGravity, int delay, int dustColor) {
            this.affectedByGravity = affectedByGravity;
            this.delay = delay;
            this.dustColor = dustColor;
        }
    }

    public static class Shape {
        public float minX;
        public float minY;
        public float minZ;
        public float maxX;
        public float maxY;
        public float maxZ;

        public Shape(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }

        public static Shape get(JsonElement element, String name) {
            if (element.isJsonObject()) {
                JsonObject object = element.getAsJsonObject();
                float minX = JsonHelper.getFloat(object, "min_x", 0f);
                float minY = JsonHelper.getFloat(object, "min_y", 0f);
                float minZ = JsonHelper.getFloat(object, "min_z", 0f);
                float maxX = JsonHelper.getFloat(object, "max_x", 16f);
                float maxY = JsonHelper.getFloat(object, "max_y", 16f);
                float maxZ = JsonHelper.getFloat(object, "max_z", 16f);
                return new Shape(minX, minY, minZ, maxX, maxY, maxZ);
            }
            throw new JsonSyntaxException(name);
        }
    }
}
