package mindustryV4.world.blocks.distribution;

import io.anuke.arc.Core;
import io.anuke.arc.graphics.g2d.TextureRegion;
import mindustryV4.type.Liquid;
import mindustryV4.world.Tile;
import mindustryV4.world.blocks.LiquidBlock;
import mindustryV4.world.meta.BlockStat;
import io.anuke.arc.graphics.g2d.Draw;

public class LiquidJunction extends LiquidBlock{

    public LiquidJunction(String name){
        super(name);
        hasLiquids = true;
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.remove(BlockStat.liquidCapacity);
    }

    @Override
    public void draw(Tile tile){
        Draw.rect(name, tile.worldx(), tile.worldy());
    }

    @Override
    public TextureRegion[] generateIcons(){
        return new TextureRegion[]{Core.atlas.find(name)};
    }

    @Override
    public void handleLiquid(Tile tile, Tile source, Liquid liquid, float amount){
        int dir = source.relativeTo(tile.x, tile.y);
        dir = (dir + 4) % 4;
        Tile to = tile.getNearby(dir).target();

        if(to.block().hasLiquids && to.block().acceptLiquid(to, tile, liquid, Math.min(to.block().liquidCapacity - to.entity.liquids.get(liquid) - 0.00001f, amount))){
            to.block().handleLiquid(to, tile, liquid, Math.min(to.block().liquidCapacity - to.entity.liquids.get(liquid) - 0.00001f, amount));
        }
    }

    @Override
    public boolean acceptLiquid(Tile dest, Tile source, Liquid liquid, float amount){
        int dir = source.relativeTo(dest.x, dest.y);
        dir = (dir + 4) % 4;
        Tile to = dest.getNearby(dir);
        if(to == null) return false;
        to = to.target();
        return to != null && to.entity != null && to.block().hasLiquids && to.block().acceptLiquid(to, dest, liquid, Math.min(to.block().liquidCapacity - to.entity.liquids.get(liquid) - 0.00001f, amount));
    }
}