package mindustryV4.world.blocks.power;

import mindustryV4.content.fx.BlockFx;
import mindustryV4.entities.TileEntity;
import mindustryV4.type.Liquid;
import mindustryV4.world.Tile;
import mindustryV4.world.blocks.power.ItemGenerator.ItemGeneratorEntity;
import mindustryV4.world.consumers.ConsumeLiquidFilter;
import ucore.core.Effects;
import ucore.core.Effects.Effect;
import ucore.graphics.Draw;
import ucore.util.Mathf;

public abstract class LiquidGenerator extends PowerGenerator{
    protected float minEfficiency = 0.2f;
    protected float powerPerLiquid;
    /**Maximum liquid used per frame.*/
    protected float maxLiquidGenerate;
    protected Effect generateEffect = BlockFx.generatespark;

    public LiquidGenerator(String name){
        super(name);
        liquidCapacity = 30f;
        hasLiquids = true;
    }

    @Override
    public void setStats(){
        consumes.add(new ConsumeLiquidFilter(liquid -> getEfficiency(liquid) >= minEfficiency, maxLiquidGenerate)).update(false);
        super.setStats();
    }

    @Override
    public void draw(Tile tile){
        super.draw(tile);

        TileEntity entity = tile.entity();

        Draw.color(entity.liquids.current().color);
        Draw.alpha(entity.liquids.total() / liquidCapacity);
        drawLiquidCenter(tile);
        Draw.color();
    }

    public void drawLiquidCenter(Tile tile){
        Draw.rect("blank", tile.drawx(), tile.drawy(), 2, 2);
    }

    @Override
    public void update(Tile tile){
        TileEntity entity = tile.entity();

        if(entity.liquids.get(entity.liquids.current()) >= 0.001f){
            float powerPerLiquid = getEfficiency(entity.liquids.current()) * this.powerPerLiquid;
            float used = Math.min(entity.liquids.currentAmount(), maxLiquidGenerate * entity.delta());
            used = Math.min(used, (powerCapacity - entity.power.amount) / powerPerLiquid);

            entity.liquids.remove(entity.liquids.current(), used);
            entity.power.amount += used * powerPerLiquid;

            if(used > 0.001f && Mathf.chance(0.05 * entity.delta())){
                Effects.effect(generateEffect, tile.drawx() + Mathf.range(3f), tile.drawy() + Mathf.range(3f));
            }
        }

        tile.entity.power.graph.update();
    }

    @Override
    public boolean acceptLiquid(Tile tile, Tile source, Liquid liquid, float amount){
        return getEfficiency(liquid) >= minEfficiency && super.acceptLiquid(tile, source, liquid, amount);
    }

    @Override
    public TileEntity newEntity(){
        return new ItemGeneratorEntity();
    }

    /**
     * Returns an efficiency value for the specified liquid.
     * Greater efficiency means more power generation.
     * If a liquid's efficiency is below {@link #minEfficiency}, it is not accepted.
     */
    protected abstract float getEfficiency(Liquid liquid);
}
