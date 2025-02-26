package gregtech.api.util;

import gregtech.api.worldgen.config.OreDepositDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Loader;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static gregtech.api.GTValues.MODID_AR;

/**
 * Package for various helper methods used in Gregtech JEI integration
 */
public class GTJEIUtility {

    private static final int FONT_HEIGHT = Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;

    /**
     * Creates a list of biomes whose weight, as described by the passed Function, differs from the passed original weight
     * For use in the JEI Ore Spawn Page and JEI Fluid Spawn Page
     *
     * @param biomeFunction A Function describing the modified weights of biomes
     * @param originalWeight The original weight of the biome
     *
     * @return A List containing all the modified biomes and their value they are modified by
     */
    public static List<String> createSpawnPageBiomeTooltip(Function<Biome, Integer> biomeFunction, int originalWeight) {

        List<String> tooltip = new ArrayList<>();

        if (biomeFunction == OreDepositDefinition.NO_BIOME_INFLUENCE) {
            return tooltip;
        }

        Iterator<Biome> biomeIterator = Biome.REGISTRY.iterator();
        int biomeWeight;
        Map<Biome, Integer> modifiedBiomeMap = new HashMap<>();

        //Tests biomes against all registered biomes to find which biomes have had their weights modified
        while (biomeIterator.hasNext()) {

            Biome biome = biomeIterator.next();

            //Gives the Biome Weight
            biomeWeight = biomeFunction.apply(biome);
            //Check if the biomeWeight is modified
            if (biomeWeight != originalWeight) {
                modifiedBiomeMap.put(biome, originalWeight + biomeWeight);
            }
        }

        if(!modifiedBiomeMap.isEmpty()) {
            tooltip.add("§\n");
            tooltip.add(I18n.format("gregtech.jei.ore.biome_weighting_title"));
        }
        for (Map.Entry<Biome, Integer> entry : modifiedBiomeMap.entrySet()) {
            //Don't show non changed weights, to save room
            if (!(entry.getValue() == originalWeight)) {
                //Cannot Spawn
                if (entry.getValue() <= 0) {
                    tooltip.add(I18n.format("gregtech.jei.ore.biome_weighting_no_spawn", entry.getKey().getBiomeName()));
                } else {
                    tooltip.add(I18n.format("gregtech.jei.ore.biome_weighting", entry.getKey().getBiomeName(), entry.getValue()));
                }
            }
        }

        return tooltip;
    }

    /**
     * Attempts to draw a comma separated list of Dimension IDs, adding dimension names to specified dimension ids
     * Will create a new line if the list would overflow the page
     *
     * @param namedDimensions A Map containing the special dimensions that should have their names added to the display
     * @param dimensionIDs A List of all dimension IDs to draw
     * @param dimStartYPos The Starting Y Position of the displayed dimension names
     * @param dimDisplayPosX The Starting X Position of the displayed dimension names
     */
    public static void drawMultiLineCommaSeparatedDimensionList(Map<Integer, String> namedDimensions, List<Integer> dimensionIDs, FontRenderer fontRenderer, int dimStartXPos, int dimStartYPos, int dimDisplayPosX) {

        String dimName;
        String fullDimName;
        int dimDisplayLength;

        for (int i = 0; i < dimensionIDs.size(); i++) {

            boolean isLastDimension = i == dimensionIDs.size() - 1;

            //If the dimension name is included, append it to the dimension number
            if (namedDimensions.containsKey(dimensionIDs.get(i))) {
                dimName = namedDimensions.get(dimensionIDs.get(i));
                fullDimName = isLastDimension ?
                        dimensionIDs.get(i) + " (" + dimName + ")" :
                        dimensionIDs.get(i) + " (" + dimName + "), ";
            }
            //If the dimension name is not included, just add the dimension number
            else {
                fullDimName = isLastDimension ?
                        Integer.toString(dimensionIDs.get(i)) :
                        dimensionIDs.get(i) + ", ";
            }

            //Find the length of the dimension name string
            dimDisplayLength = fontRenderer.getStringWidth(fullDimName);

            //If the length of the string would go off the edge of screen, instead increment the y position
            if (dimDisplayLength > (176 - dimDisplayPosX)) {
                dimStartYPos = dimStartYPos + FONT_HEIGHT;
                dimDisplayPosX = dimStartXPos;
            }

            fontRenderer.drawString(fullDimName, dimDisplayPosX, dimStartYPos, 0x111111);

            //Increment the dimension name display position
            dimDisplayPosX = dimDisplayPosX + dimDisplayLength;
        }
    }

    /**
     * Cleans up the dimension list present on some JEI pages by removing specific dimensions that are not applicable
     *
     * @param dimensions A list of all dimensions
     */
    public static void cleanupDimensionList(Supplier<List<Integer>> dimensions) {

        if (Loader.isModLoaded(MODID_AR)) {
            try {
                int[] spaceDims = DimensionManager.getDimensions(DimensionType.byName("space"));

                //Remove Space from the dimension list
                for (int i = 0; i < spaceDims.length; i++) {
                    if (dimensions.get().contains(spaceDims[i])) {
                        dimensions.get().remove(spaceDims[i]);
                    }
                }
            } catch (IllegalArgumentException e) {
                GTLog.logger.error("Something went wrong with AR JEI integration, No DimensionType found");
                GTLog.logger.error(e);
            }
        }
    }
}
