package mindustryV4.maps.generators;

import mindustryV4.content.Blocks;
import mindustryV4.game.Team;
import mindustryV4.world.Block;
import mindustryV4.world.Tile;

public abstract class RandomGenerator extends Generator{
    protected Block floor;
    protected Block block;

    public RandomGenerator(int width, int height){
        super(width, height);
    }

    @Override
    public void generate(Tile[][] tiles){
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                floor = Blocks.air;
                block = Blocks.air;
                generate(x, y);
                tiles[x][y] = new Tile(x, y, floor.id, block.id);
            }
        }

        tiles[width/2][height/2].setBlock(Blocks.coreShard, Team.blue);
        tiles[width/2][height/2 - 6].setBlock(Blocks.launchPad, Team.blue);
    }

    /**Sets {@link #floor} and {@link #block} to the correct values as output.
     * Before this method is called, both are set to {@link Blocks#air} as defaults.*/
    public abstract void generate(int x, int y);
}
