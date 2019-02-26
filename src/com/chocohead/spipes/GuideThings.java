package com.chocohead.spipes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterables;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;

import net.minecraftforge.common.crafting.IShapedRecipe;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.loader.XmlPageLoader;
import buildcraft.lib.client.guide.loader.entry.PageEntryType;
import buildcraft.lib.client.guide.parts.GuideText;
import buildcraft.lib.client.guide.parts.recipe.GuideCraftingFactoryDirect;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.recipe.ChangingItemStack;

public class GuideThings {
	public static class PipeEntryType extends PageEntryType<String> {
		public static final String ID = StubbornPipes.MODID + ":pipe";
		public static final PipeEntryType INSTANCE = new PipeEntryType();

		@Override
		public String deserialise(String source) {
			return source;
		}

		@Override
		public List<String> getTooltip(String value) {
			return Collections.singletonList(value);
		}

		@Override
		public ISimpleDrawable createDrawable(String value) {
			return new ISimpleDrawable() {
				private final Iterator<Item> it = Iterables.cycle(StubbornPipes.woodPower, StubbornPipes.stonePower, StubbornPipes.cobblePower, StubbornPipes.quartzPower,
						StubbornPipes.goldPower, StubbornPipes.sandstonePower, StubbornPipes.ironPower, StubbornPipes.diamondPower, StubbornPipes.emeraldPower).iterator();
				private ItemStack head = new ItemStack(it.next());
				private int tick = 0;

				@Override
				public void drawAt(double x, double y) {
					GlStateManager.color(1, 1, 1);
					RenderHelper.enableGUIStandardItemLighting();

					if (tick++ > 40) {
						tick = 0;
						head = new ItemStack(it.next());
					}
					Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(head, (int) x, (int) y);

					RenderHelper.disableStandardItemLighting();
				}
			};
		}
	}

	@SideOnly(Side.CLIENT)
	public static void addTags() {
		PageEntryType.register(PipeEntryType.ID, PipeEntryType.INSTANCE);

		XmlPageLoader.TAG_FACTORIES.put("pipeLink", tag -> {
			ItemStack stack = XmlPageLoader.loadItemStack(tag);

			PageLine line;
			if (stack == null || stack.isEmpty()) {
				line = new PageLine(1, "Missing item: " + tag, false);
			} else {
				ISimpleDrawable icon = new GuiStack(stack);
				line = new PageLine(icon, icon, 1, stack.getDisplayName(), true);
			}

			return Collections.singletonList(gui -> new GuideText(gui, line) {
				@Override
				public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index, int mouseX, int mouseY) {
					if (line.link && (wasHovered || wasIconHovered)) {
						gui.openPage(GuideManager.INSTANCE.getPageFor(stack).createNew(gui));
					}

					return renderLine(current, text, x, y, width, height, -1);
				}
			});
		});

		XmlPageLoader.TAG_FACTORIES.put("pipeColouring", tag -> {
			ItemStack stack = XmlPageLoader.loadItemStack(tag);
			List<Pair<Ingredient[][], ItemStack>> found = new ArrayList<>();

			for (IRecipe recipe : ForgeRegistries.RECIPES) {
				ItemStack output = recipe.getRecipeOutput();

				if (output.getItem() == stack.getItem() && output.getItemDamage() >= 1 && output.getItemDamage() <= 16) {
					NonNullList<Ingredient> input = recipe.getIngredients();
					Ingredient[][] matrix = new Ingredient[3][3];

					int maxX = recipe instanceof IShapedRecipe ? ((IShapedRecipe) recipe).getRecipeWidth() : 3;
					int maxY = recipe instanceof IShapedRecipe ? ((IShapedRecipe) recipe).getRecipeHeight() : 3;
					int offsetX = maxX == 1 ? 1 : 0;
					int offsetY = maxY == 1 ? 1 : 0;

					for (int y = 0; y < 3; y++) {
						for (int x = 0; x < 3; x++) {
							if (x < offsetX || y < offsetY) {
								matrix[x][y] = Ingredient.EMPTY;
								continue;
							}
							int i = x - offsetX + (y - offsetY) * maxX;
							if (i >= input.size() || x - offsetX >= maxX) {
								matrix[x][y] = Ingredient.EMPTY;
							} else {
								matrix[x][y] = input.get(i);
							}
						}
					}

					found.add(Pair.of(matrix, output));
				}
			}

			@SuppressWarnings("unchecked")
			NonNullList<ItemStack>[][] inputs = new NonNullList[3][3];
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 3; x++) {
					inputs[x][y] = NonNullList.create();
				}
			}
			NonNullList<ItemStack> outputs = NonNullList.create();

			for (Pair<Ingredient[][], ItemStack> recipe : found) {
				Ingredient[][] matrix = recipe.getLeft();

				for (int y = 0; y < 3; y++) {
					for (int x = 0; x < 3; x++) {
						ItemStack inputStacks[] = matrix[x][y].getMatchingStacks();

						if (inputStacks.length == 0) {
							inputs[x][y].add(ItemStack.EMPTY);
						} else {
							for (ItemStack inputStack : inputStacks) {
								inputs[x][y].add(inputStack);
							}
						}
					}
				}

				outputs.add(recipe.getRight());
			}

			return Collections.singletonList(new GuideCraftingFactoryDirect(Arrays.stream(inputs).map(inner -> Arrays.stream(inner).map(ChangingItemStack::new).toArray(ChangingItemStack[]::new)).toArray(ChangingItemStack[][]::new),
					new ChangingItemStack(outputs)));
		});
	}
}