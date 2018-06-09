package com.chocohead.spipes;

import static com.chocohead.spipes.StubbornPipes.MODID;

import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.transport.pipe.PipeApiClient;
import buildcraft.api.transport.pipe.PipeDefinition;

import buildcraft.lib.registry.CreativeTabManager;
import buildcraft.lib.registry.CreativeTabManager.CreativeTabBC;
import buildcraft.lib.registry.RegistrationHelper;
import buildcraft.lib.registry.RegistryConfig;
import buildcraft.transport.BCTransportItems;
import buildcraft.transport.item.ItemPipeHolder;

import com.chocohead.spipes.block.BlockMJTester;
import com.chocohead.spipes.block.BlockMJTester.Type;
import com.chocohead.spipes.item.ItemMJTester;
import com.chocohead.spipes.logic.PipeBehaviourIron;
import com.chocohead.spipes.logic.PipeBehaviourPowerInput;
import com.chocohead.spipes.logic.PipeBehaviourPowerLimit;
import com.chocohead.spipes.logic.PipeBehaviourSandstone;
import com.chocohead.spipes.logic.PipeFlowFUMJ;
import com.chocohead.spipes.logic.RenderPool;
import com.chocohead.spipes.pretty.PipeFlowPrettyFU;
import com.chocohead.spipes.tile.TileEntityFUTester;
import com.chocohead.spipes.tile.TileEntityMJTester;

@EventBusSubscriber
@Mod(modid=MODID, name="Stubborn Pipes", dependencies="required-after:buildcrafttransport;required:buildcraftenergy", version="@VERSION@")
public final class StubbornPipes {
	public static final String MODID = "stubborn_pipes";
	public static final Thread RENDER_POOL = new Thread(new RenderPool(), "Stubborn Rendering");

	public static CreativeTabBC tab = CreativeTabManager.createTab(MODID);
	//public static PipeDefinition woodPower, stonePower, cobblePower, quartzPower, goldPower, sandstonePower, ironPower, diamondPower, emeraldPower;
	public static Item woodPower, stonePower, cobblePower, quartzPower, goldPower, sandstonePower, ironPower, diamondPower, emeraldPower;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		RegistryConfig.setRegistryConfig(MODID, event.getSuggestedConfigurationFile());

		DefinitionBuilder builder = new DefinitionBuilder(); //A la BCTransportPipes
		RegistrationHelper helper = new RegistrationHelper();

		builder.enableColouring(); //We want coloured pipes too

		builder.logic(PipeBehaviourPowerInput::wood, PipeBehaviourPowerInput::wood).texSuffixes("_clear", "_filled");
		woodPower = register(helper, builder.idTexPrefix("wood_power").define());

		builder.logic(PipeBehaviourPowerLimit::stone, PipeBehaviourPowerLimit::stone);
		stonePower = register(helper, builder.idTex("stone_power").define());

		builder.logic(PipeBehaviourPowerLimit::cobble, PipeBehaviourPowerLimit::cobble);
		cobblePower = register(helper, builder.idTex("cobblestone_power").define());

		builder.logic(PipeBehaviourPowerLimit::quartz, PipeBehaviourPowerLimit::quartz);
		quartzPower = register(helper, builder.idTex("quartz_power").define());

		builder.logic(PipeBehaviourPowerLimit::gold, PipeBehaviourPowerLimit::gold);
		tab.setItem(goldPower = register(helper, builder.idTex("gold_power").define()));

		builder.logic(PipeBehaviourSandstone::new, PipeBehaviourSandstone::new);
		sandstonePower = register(helper, builder.idTex("sandstone_power").define());

		builder.logic(PipeBehaviourIron::new, PipeBehaviourIron::new).texSuffixes("_clear", "_filled");
		ironPower = register(helper, builder.idTexPrefix("iron_power").define());

		builder.logic(PipeBehaviourPowerLimit::diamond, PipeBehaviourPowerLimit::diamond);
		diamondPower = register(helper, builder.idTex("diamond_power").define());

		builder.logic(PipeBehaviourPowerInput::emerald, PipeBehaviourPowerInput::emerald).texSuffixes("_clear", "_filled");
		emeraldPower = register(helper, builder.idTexPrefix("diamond_wood_power").define());

		if (event.getSide().isClient()) {
			MinecraftForge.EVENT_BUS.register(new Object() {
				@SubscribeEvent
				@SideOnly(Side.CLIENT)
				public void onTooltip(ItemTooltipEvent event) {
					if (event.getItemStack().getItem() == emeraldPower) {
						event.getToolTip().add(I18n.format("item.pipe.stubborn_pipes.diamond_wood_power.tooltip"));
					}
				}
			});

			//PipeApiClient.registry.registerRenderer(PipeFlowFU.class, PipeFlowPrettyFU.INSTANCE);
			PipeApiClient.registry.registerRenderer(PipeFlowFUMJ.class, PipeFlowPrettyFU.INSTANCE);
		}
	}

	private static Item register(RegistrationHelper helper, PipeDefinition pipe) {
		ItemPipeHolder item = ItemPipeHolder.create(pipe);

		item.setRegistryName(pipe.identifier.toString());
		item.setUnlocalizedName("pipe." + pipe.identifier.toString().replace(':', '.'));
		item.registerWithPipeApi();
		item.setCreativeTab(tab);

		return helper.addItem(item);
	}

	@SubscribeEvent
	public static void register(Register<Block> event) {
		event.getRegistry().register(new BlockMJTester().setRegistryName(new ResourceLocation(MODID, "mj_tester")).setUnlocalizedName(MODID + ".mj_tester"));

		GameRegistry.registerTileEntity(TileEntityMJTester.class, MODID + ":mj_tester");
		GameRegistry.registerTileEntity(TileEntityFUTester.class, MODID + ":fu_tester");
	}

	@SubscribeEvent
	public static void registerMore(Register<Item> event) {
		Item item = new ItemMJTester(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(MODID, "mj_tester")));
		event.getRegistry().register(item.setRegistryName(new ResourceLocation(MODID, "mj_tester")).setUnlocalizedName(MODID + ".mj_tester"));
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void register(ModelRegistryEvent event) {
		ResourceLocation loc = new ResourceLocation(MODID, "mj_tester");
		Block block = ForgeRegistries.BLOCKS.getValue(loc);
		Item item = ForgeRegistries.ITEMS.getValue(loc);

		Map<IBlockState, ModelResourceLocation> map = new IdentityHashMap<>();
		map.put(block.getDefaultState().withProperty(BlockMJTester.TYPE, Type.MJ), new ModelResourceLocation(new ResourceLocation("stone"), null));
		map.put(block.getDefaultState().withProperty(BlockMJTester.TYPE, Type.FU), new ModelResourceLocation(new ResourceLocation("stonebrick"), null));
		ModelLoader.setCustomStateMapper(block, b -> map);

		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(new ResourceLocation("stone"), "inventory"));
		ModelLoader.setCustomModelResourceLocation(item, 1, new ModelResourceLocation(new ResourceLocation("stonebrick"), "inventory"));
	}

	@SubscribeEvent
	public static void recipes(Register<IRecipe> event) {
		addShapelessPipeRecipes(BCTransportItems.pipeItemWood, woodPower);
		addShapelessPipeRecipes(BCTransportItems.pipeItemStone, stonePower);
		addShapelessPipeRecipes(BCTransportItems.pipeItemCobble, cobblePower);
		addShapelessPipeRecipes(BCTransportItems.pipeItemQuartz, quartzPower);
		addShapelessPipeRecipes(BCTransportItems.pipeItemGold, goldPower);
		addShapelessPipeRecipes(BCTransportItems.pipeItemSandstone, sandstonePower);
		addShapelessPipeRecipes(BCTransportItems.pipeItemIron, ironPower);
		addShapelessPipeRecipes(BCTransportItems.pipeItemDiamond, diamondPower);
		addShapelessPipeRecipes(BCTransportItems.pipeItemDiaWood, emeraldPower);
	}

	public static void addShapelessPipeRecipes(Item from, Item to) {
		String name = pipeName(to);

		addShapelessPipeRecipe(name, new ItemStack(from), new ItemStack(to));
		for (EnumDyeColor colour : EnumDyeColor.values()) {
			ItemStack f = new ItemStack(from, 1, colour.getMetadata() + 1);
			ItemStack t = new ItemStack(to, 1, colour.getMetadata() + 1);

			addShapelessPipeRecipe(name + '_' + colour.getName(), f, t);
		}
	}

	private static void addShapelessPipeRecipe(String name, ItemStack from, ItemStack to) {
		//Undo recipe
		addShapelessRecipe(name + "_undo", from, Ingredient.fromStacks(to));

		//Recipe for normal pipes
		addShapelessRecipe(name, to, Ingredient.fromStacks(from), Ingredient.fromItem(Items.REDSTONE), Ingredient.fromItem(Items.STICK));
	}

	private static void addShapelessRecipe(String name, ItemStack output, Ingredient... ingredients) {
		GameRegistry.addShapelessRecipe(new ResourceLocation(MODID, name), null, output, ingredients);
	}

	private static String pipeName(Item item) {
		return item.getRegistryName().toString().replace(':', '_');
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		RENDER_POOL.setDaemon(true);
		RENDER_POOL.start();

		if (event.getSide().isClient()) GuideThings.addTags();
	}
}