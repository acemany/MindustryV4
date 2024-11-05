package mindustryV4.world.blocks.defense;

import io.anuke.arc.Core;
import io.anuke.arc.collection.IntSet;
import io.anuke.arc.graphics.Blending;
import io.anuke.arc.graphics.Color;
import io.anuke.arc.graphics.g2d.Draw;
import io.anuke.arc.graphics.g2d.Lines;
import io.anuke.arc.graphics.g2d.TextureRegion;
import io.anuke.arc.math.Mathf;
import io.anuke.arc.util.Time;
import mindustryV4.entities.type.TileEntity;
import mindustryV4.world.Block;
import mindustryV4.world.Tile;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static mindustryV4.Vars.tilesize;
import static mindustryV4.Vars.world;

public class OverdriveProjector extends Block{
    private static Color color = Color.valueOf("feb380");
    private static Color phase = Color.valueOf("ffd59e");
    private static IntSet healed = new IntSet();

    protected int timerUse = timers ++;

    protected TextureRegion topRegion;
    protected float reload = 60f;
    protected float range = 80f;
    protected float speedBoost = 1.5f;
    protected float speedBoostPhase = 0.75f;
    protected float useTime = 400f;
    protected float phaseRangeBoost = 20f;

    public OverdriveProjector(String name){
        super(name);
        solid = true;
        update = true;
        hasPower = true;
        hasItems = true;
        canOverdrive = false;
    }

    @Override
    public void load(){
        super.load();
        topRegion = Core.atlas.find(name + "-top");
    }

    @Override
    public void update(Tile tile){
        OverdriveEntity entity = tile.entity();
        entity.heat = Mathf.lerpDelta(entity.heat, entity.cons.valid() ? 1f : 0f, 0.08f);
        entity.charge += entity.heat * Time.delta();

        entity.phaseHeat = Mathf.lerpDelta(entity.phaseHeat, (float)entity.items.get(consumes.item()) / itemCapacity, 0.1f);

        if(entity.timer.get(timerUse, useTime) && entity.items.total() > 0){
            entity.items.remove(consumes.item(), 1);
        }

        if(entity.charge >= reload){
            float realRange = range + entity.phaseHeat * phaseRangeBoost;
            float realBoost = (speedBoost + entity.phaseHeat*speedBoostPhase) * entity.power.satisfaction;

            entity.charge = 0f;

            int tileRange = (int)(realRange / tilesize);
            healed.clear();

            for(int x = -tileRange + tile.x; x <= tileRange + tile.x; x++){
                for(int y = -tileRange + tile.y; y <= tileRange + tile.y; y++){
                    if(Mathf.dst(x, y, tile.x, tile.y) > realRange) continue;

                    Tile other = world.tile(x, y);

                    if(other == null) continue;
                    other = other.target();

                    if(other.getTeamID() == tile.getTeamID() && !healed.contains(other.pos()) && other.entity != null){
                        other.entity.timeScaleDuration = Math.max(other.entity.timeScaleDuration, reload + 1f);
                        other.entity.timeScale = Math.max(other.entity.timeScale, realBoost);
                       healed.add(other.pos());
                    }
                }
            }
        }
    }

    @Override
    public void drawSelect(Tile tile){
        OverdriveEntity entity = tile.entity();
        float realRange = range + entity.phaseHeat * phaseRangeBoost;

        Draw.color(color);
        Lines.dashCircle(tile.drawx(), tile.drawy(), realRange);
        Draw.color();
    }

    @Override
    public void draw(Tile tile){
        super.draw(tile);

        OverdriveEntity entity = tile.entity();
        float f = 1f - (Time.time() / 100f) % 1f;

        Draw.color(color, phase, entity.phaseHeat);
        Draw.alpha(entity.heat * Mathf.absin(Time.time(), 10f, 1f) * 0.5f);
        Draw.blend(Blending.additive);
        Draw.rect(topRegion, tile.drawx(), tile.drawy());
        Draw.blend();
        Draw.alpha(1f);
        Lines.stroke((2f  * f + 0.2f)* entity.heat);
        Lines.circle(tile.drawx(), tile.drawy(), (1f-f) * 9f);

        Draw.reset();
    }

    @Override
    public TileEntity newEntity(){
        return new OverdriveEntity();
    }

    class OverdriveEntity extends TileEntity{
        float heat;
        float charge;
        float phaseHeat;

        @Override
        public void write(DataOutput stream) throws IOException{
            stream.writeFloat(heat);
            stream.writeFloat(phaseHeat);
        }

        @Override
        public void read(DataInput stream) throws IOException{
            heat = stream.readFloat();
            phaseHeat = stream.readFloat();
        }
    }
}