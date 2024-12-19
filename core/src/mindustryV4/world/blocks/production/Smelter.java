package mindustryV4.world.blocks.production;

import mindustryV4.entities.Effects;
import mindustryV4.entities.Effects.Effect;
import io.anuke.arc.graphics.Color;
import io.anuke.arc.graphics.g2d.Draw;
import io.anuke.arc.graphics.g2d.Fill;
import io.anuke.arc.math.Mathf;
import io.anuke.arc.util.Time;
import mindustryV4.content.Fx;
import mindustryV4.entities.type.TileEntity;
import mindustryV4.type.Item;
import mindustryV4.type.ItemStack;
import mindustryV4.world.Block;
import mindustryV4.world.Tile;
import mindustryV4.world.consumers.ConsumeItem;
import mindustryV4.world.consumers.ConsumeItems;
import mindustryV4.world.meta.BlockStat;
import mindustryV4.world.meta.StatUnit;

public class Smelter extends Block{
    protected final int timerDump = timers++;

    protected Item result;

    protected float craftTime = 20f;
    protected float burnDuration = 50f;
    protected Effect craftEffect = Fx.smelt, burnEffect = Fx.fuelburn;
    protected Color flameColor = Color.valueOf("ffb879");

    public Smelter(String name){
        super(name);
        update = true;
        hasItems = true;
        solid = true;

        consumes.require(ConsumeItems.class);
        consumes.require(ConsumeItem.class);
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(BlockStat.fuelBurnTime, burnDuration / 60f, StatUnit.seconds);
        stats.add(BlockStat.outputItem, result);
        stats.add(BlockStat.craftSpeed, 60f / craftTime, StatUnit.itemsSecond);
        stats.add(BlockStat.inputItemCapacity, itemCapacity, StatUnit.items);
        stats.add(BlockStat.outputItemCapacity, itemCapacity, StatUnit.items);
    }

    @Override
    public void init(){
        super.init();
        produces.set(result);
    }

    @Override
    public void update(Tile tile){
        SmelterEntity entity = tile.entity();

        if(entity.timer.get(timerDump, 5) && entity.items.has(result)){
            tryDump(tile, result);
        }

        //add fuel
        if(entity.consumed(ConsumeItem.class) && entity.burnTime <= 0f){
            entity.items.remove(consumes.item(), 1);
            entity.burnTime += burnDuration;
            Effects.effect(burnEffect, entity.x + Mathf.range(2f), entity.y + Mathf.range(2f));
        }

        //decrement burntime
        if(entity.burnTime > 0){
            entity.burnTime -= entity.delta();
            entity.heat = Mathf.lerpDelta(entity.heat, 1f, 0.02f);
        }else{
            entity.heat = Mathf.lerpDelta(entity.heat, 0f, 0.02f);
        }

        //make sure it has all the items
        if(!entity.cons.valid()){
            return;
        }

        entity.craftTime += entity.delta();

        if(entity.items.get(result) >= itemCapacity //output full
                || entity.burnTime <= 0 //not burning
                || entity.craftTime < craftTime){ //not yet time
            return;
        }

        entity.craftTime = 0f;
        for(ItemStack item : consumes.items()){
            entity.items.remove(item.item, item.amount);
        }

        offloadNear(tile, result);
        Effects.effect(craftEffect, flameColor, tile.drawx(), tile.drawy());
    }

    @Override
    public boolean acceptItem(Item item, Tile tile, Tile source){
        boolean isInput = false;

        for(ItemStack req : consumes.items()){
            if(req.item == item){
                isInput = true;
                break;
            }
        }

        return (isInput && tile.entity.items.get(item) < itemCapacity) || (item == consumes.item() && tile.entity.items.get(consumes.item()) < itemCapacity);
    }

    @Override
    public void draw(Tile tile){
        super.draw(tile);

        SmelterEntity entity = tile.entity();

        //draw glowing center
        if(entity.heat > 0f){
            float g = 0.1f;

            Draw.alpha(((1f - g) + Mathf.absin(Time.time(), 8f, g)) * entity.heat);

            Draw.tint(flameColor);
            Fill.circle(tile.drawx(), tile.drawy(), 2f + Mathf.absin(Time.time(), 5f, 0.8f));
            Draw.color(1f, 1f, 1f, entity.heat);
            Fill.circle(tile.drawx(), tile.drawy(), 1f + Mathf.absin(Time.time(), 5f, 0.7f));

            Draw.color();
        }
    }

    @Override
    public TileEntity newEntity(){
        return new SmelterEntity();
    }

    public class SmelterEntity extends TileEntity{
        public float burnTime;
        public float heat;
        public float craftTime;
    }
}