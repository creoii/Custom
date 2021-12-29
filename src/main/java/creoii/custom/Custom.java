package creoii.custom;

import creoii.custom.custom.CustomBlock;
import creoii.custom.custom.CustomItemGroup;
import creoii.custom.custom.CustomPainting;
import creoii.custom.custom.CustomTrade;
import creoii.custom.json.BlocksManager;
import creoii.custom.json.ItemGroupsManager;
import creoii.custom.json.PaintingsManager;
import creoii.custom.json.TradesManager;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.Blocks;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.tag.RequiredTagListRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffers;

import java.util.Arrays;

/**
 * custom block shapes, stickiness
 * custom items
 * custom trade serialization
 * move new tags from creo to here
 */
public class Custom implements ModInitializer, ClientModInitializer {
    public static final String MOD_ID = "custom";
    public static final BlocksManager BLOCKS_MANAGER = new BlocksManager();
    public static final ItemGroupsManager ITEM_GROUPS_MANAGER = new ItemGroupsManager();
    public static final PaintingsManager PAINTINGS_MANAGER = new PaintingsManager();
    public static final TradesManager TRADES_MANAGER = new TradesManager();

    @Override
    public void onInitialize() {
        for (CustomTrade trade : TRADES_MANAGER.values.values()) {
            Int2ObjectMap<TradeOffers.Factory[]> temp1 = TradesManager.professionToTradeMap(trade.getProfession().toString());
            TradeOffers.Factory[] temp2 = TradesManager.professionToTradeMap(trade.getProfession().toString()).remove(trade.getTradeLevel()).clone();
            TradeOffers.Factory[] trades = Arrays.copyOf(temp2, temp2.length + 1);
            trades[temp2.length] = trade.get();
            temp1.put(trade.getTradeLevel(), trades);
            TradeOffers.PROFESSION_TO_LEVELED_TRADE.put(
                    trade.getProfession(), temp1
            );
        }
    }

    @Override
    public void onInitializeClient() {
        for (CustomBlock block : BLOCKS_MANAGER.values.values()) {
            BlockRenderLayerMap.INSTANCE.putBlock(block, block.getRenderLayer());
        }
    }
}
