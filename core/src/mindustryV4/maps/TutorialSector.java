package mindustryV4.maps;

import io.anuke.arc.collection.Array;
import mindustryV4.content.Items;
import mindustryV4.content.Blocks;
import mindustryV4.maps.missions.BlockMission;
import mindustryV4.maps.missions.ItemMission;
import mindustryV4.maps.missions.Mission;
import mindustryV4.maps.missions.WaveMission;
import mindustryV4.world.Block;

import static mindustryV4.Vars.*;

/**Just a class for returning the list of tutorial missions.*/
public class TutorialSector{
    private static int droneIndex;

    public static Array<Mission> getMissions(){
/*
        Array<Mission> missions = Array.with(
            new ItemMission(Items.copper, 60).setMessage("$tutorial.begin"),

            new BlockMission(ProductionBlocks.mechanicalDrill).setMessage("$tutorial.drill"),

            new BlockMission(DistributionBlocks.conveyor).setShowComplete(false).setMessage("$tutorial.conveyor"),

            new ItemMission(Items.copper, 100).setMessage("$tutorial.morecopper"),

            new BlockMission(TurretBlocks.duo).setMessage("$tutorial.turret"),
            /
            //new BlockMission(ProductionBlocks.mechanicalDrill).setMessage("$tutorial.drillturret"),

            // Create a wave mission which spawns the core at 60, 60 rather than in the center of the map
            new WaveMission(2, 60, 60).setMessage("$tutorial.waves"),

            new ItemMission(Items.lead, 150).setMessage("$tutorial.lead"),
            new ItemMission(Items.copper, 250).setMessage("$tutorial.morecopper"),

            new BlockMission(CraftingBlocks.smelter).setMessage("$tutorial.smelter"),

            //drills for smelter
            new BlockMission(ProductionBlocks.mechanicalDrill),
            new BlockMission(ProductionBlocks.mechanicalDrill),
            new BlockMission(ProductionBlocks.mechanicalDrill),

            new ItemMission(Items.denseAlloy, 20).setMessage("$tutorial.densealloy"),

            new MarkerBlockMission(CraftingBlocks.siliconsmelter).setMessage("$tutorial.siliconsmelter"),

            //coal line
            new BlockMission(ProductionBlocks.mechanicalDrill).setMessage("$tutorial.silicondrill"),

            //sand line
            new BlockMission(ProductionBlocks.mechanicalDrill),
            new BlockMission(ProductionBlocks.mechanicalDrill),


            new BlockMission(PowerBlocks.combustionGenerator).setMessage("$tutorial.generator"),
            new BlockMission(ProductionBlocks.mechanicalDrill).setMessage("$tutorial.generatordrill"),
            new BlockMission(PowerBlocks.powerNode).setMessage("$tutorial.node"),
            //TODO fix positions
            new ConditionMission(Bundles.get("mission.linknode"), () -> world.tile(54, 52).entity != null && world.tile(54, 52).entity.power != null && world.tile(54, 52).entity.power.amount >= 0.01f)
                .setMessage("$tutorial.nodelink"),

            new ItemMission(Items.silicon, 70).setMessage("$tutorial.silicon"),

            new BlockMission(UnitBlocks.daggerFactory).setMessage("$tutorial.daggerfactory"),

            //power for dagger factory
            new BlockMission(PowerBlocks.powerNode),
            new BlockMission(PowerBlocks.powerNode),

            new UnitMission(UnitTypes.dagger).setMessage("$tutorial.dagger"),
            new ActionMission(TutorialSector::generateBase),
            new BattleMission(){
                public void generate(Generation gen){} //no
            }.setMessage("$tutorial.battle")
        );

        //find drone marker mission
        for(int i = 0; i < missions.size; i++){
            if(missions.get(i) instanceof MarkerBlockMission){
                droneIndex = i;
                break;
            }
        }*/

        return Array.with(
            //intentionally unlocalized
            new ItemMission(Items.copper, 50).setMessage("An updated tutorial will return next build.\nFor now, you'll have to deal with... this."),

            new BlockMission(Blocks.mechanicalDrill),

            new ItemMission(Items.copper, 100),
            new ItemMission(Items.lead, 50),

            new BlockMission(Blocks.smelter),
            new ItemMission(Items.denseAlloy, 10),
            new WaveMission(5)
        );
    }

}