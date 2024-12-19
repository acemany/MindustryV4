package mindustryV4.world.blocks.distribution;

import io.anuke.annotations.Annotations.Loc;
import io.anuke.annotations.Annotations.Remote;
import io.anuke.arc.Core;
import io.anuke.arc.collection.ObjectSet;
import mindustryV4.entities.Effects;
import mindustryV4.entities.Effects.Effect;
import io.anuke.arc.graphics.Color;
import io.anuke.arc.graphics.g2d.Draw;
import io.anuke.arc.graphics.g2d.Lines;
import io.anuke.arc.graphics.g2d.TextureRegion;
import io.anuke.arc.math.Angles;
import io.anuke.arc.math.Mathf;
import io.anuke.arc.util.Time;
import io.anuke.arc.util.pooling.Pool.Poolable;
import io.anuke.arc.util.pooling.Pools;
import mindustryV4.content.Bullets;
import mindustryV4.content.Fx;
import mindustryV4.entities.type.Player;
import mindustryV4.entities.type.TileEntity;
import mindustryV4.entities.bullet.Bullet;
import mindustryV4.gen.Call;
import mindustryV4.graphics.Layer;
import mindustryV4.graphics.Pal;
import mindustryV4.type.Item;
import mindustryV4.world.Block;
import mindustryV4.world.Tile;
import mindustryV4.world.consumers.ConsumePower;
import mindustryV4.world.meta.BlockStat;
import mindustryV4.world.meta.StatUnit;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import static mindustryV4.Vars.*;

public class MassDriver extends Block{
    protected float range;
    protected float rotateSpeed = 0.04f;
    protected float translation = 7f;
    protected int minDistribute = 10;
    protected float knockback = 4f;
    protected float reloadTime = 100f;
    protected Effect shootEffect = Fx.shootBig2;
    protected Effect smokeEffect = Fx.shootBigSmoke2;
    protected Effect recieveEffect = Fx.mineBig;
    protected float shake = 3f;
    protected float powerPercentageUsed = 0.95f;
    protected TextureRegion baseRegion;

    public MassDriver(String name){
        super(name);
        update = true;
        solid = true;
        configurable = true;
        hasItems = true;
        layer = Layer.turret;
        hasPower = true;
        consumes.powerBuffered(30f);
        outlineIcon = true;
    }

    @Remote(targets = Loc.both, called = Loc.server, forward = true)
    public static void linkMassDriver(Player player, Tile tile, int position){
        MassDriverEntity entity = tile.entity();
        entity.link = position;
    }

    @Remote(called = Loc.server)
    public static void onMassDriverFire(Tile tile, Tile target){
        //just in case the client has invalid data
        if(!(tile.entity instanceof MassDriverEntity) || !(target.entity instanceof MassDriverEntity)) return;

        MassDriver driver = (MassDriver) tile.block();

        MassDriverEntity entity = tile.entity();
        MassDriverEntity other = target.entity();

        entity.reload = 1f;

        entity.power.satisfaction -= Math.min(entity.power.satisfaction, driver.powerPercentageUsed);

        DriverBulletData data = Pools.obtain(DriverBulletData.class, DriverBulletData::new);
        data.from = entity;
        data.to = other;
        int totalUsed = 0;
        for(int i = 0; i < content.items().size; i++){
            int maxTransfer = Math.min(entity.items.get(content.item(i)), ((MassDriver) tile.block()).itemCapacity - totalUsed);
            data.items[i] = maxTransfer;
            totalUsed += maxTransfer;
        }
        entity.items.clear();

        float angle = tile.angleTo(target);

        other.isRecieving = true;
        Bullet.create(Bullets.driverBolt, entity, entity.getTeam(),
                tile.drawx() + Angles.trnsx(angle, driver.translation), tile.drawy() + Angles.trnsy(angle, driver.translation),
                angle, 1f, 1f, data);

        Effects.effect(driver.shootEffect, tile.drawx() + Angles.trnsx(angle, driver.translation),
                tile.drawy() + Angles.trnsy(angle, driver.translation), angle);

        Effects.effect(driver.smokeEffect, tile.drawx() + Angles.trnsx(angle, driver.translation),
                tile.drawy() + Angles.trnsy(angle, driver.translation), angle);

        Effects.shake(driver.shake, driver.shake, entity);
    }

    @Override
    public TextureRegion[] generateIcons(){
        return new TextureRegion[]{Core.atlas.find(name + "-base"), Core.atlas.find(name + "-turret")};
    }

    @Override
    public void load(){
        super.load();

        baseRegion = Core.atlas.find(name + "-base");
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.add(BlockStat.powerShot, consumes.get(ConsumePower.class).powerCapacity * powerPercentageUsed, StatUnit.powerUnits);
    }

    @Override
    public void init(){
        super.init();
        viewRange = range;
    }

    @Override
    public void update(Tile tile){
        MassDriverEntity entity = tile.entity();

        Tile link = world.tile(entity.link);

        if(entity.isUnloading){
            tryDump(tile);
            if(entity.items.total() <= 0){
                entity.isUnloading = false;
            }
        }

        if(entity.reload > 0f){
            entity.reload = Mathf.clamp(entity.reload - entity.delta() / reloadTime);
        }

        //unload when dest is full
        if(!linkValid(tile) || (link.entity.items.total() >= itemCapacity) && entity.items.total() > 0){
            entity.isUnloading = true;
        }

        if(!entity.isRecieving){

            if(entity.waiting.size > 0){ //accepting takes priority over shooting
                Tile waiter = entity.waiting.first();

                entity.rotation = Mathf.slerpDelta(entity.rotation, tile.angleTo(waiter), rotateSpeed);
            }else if(tile.entity.items.total() >= minDistribute &&
                    linkValid(tile) && //only fire when at 100% power capacity
                    tile.entity.power.satisfaction >= powerPercentageUsed &&
                    link.block().itemCapacity - link.entity.items.total() >= minDistribute && entity.reload <= 0.0001f){

                MassDriverEntity other = link.entity();
                other.waiting.add(tile);

                float target = tile.angleTo(link);

                entity.rotation = Mathf.slerpDelta(entity.rotation, target, rotateSpeed);

                if(Angles.near(entity.rotation, target, 1f) &&
                Angles.near(other.rotation, target + 180f, 1f)){
                    Call.onMassDriverFire(tile, link);
                }
            }
        }

        entity.waiting.clear();
    }

    @Override
    public void draw(Tile tile){
        Draw.rect(baseRegion, tile.drawx(), tile.drawy());
    }

    @Override
    public void drawLayer(Tile tile){
        MassDriverEntity entity = tile.entity();

        Draw.rect(region,
                tile.drawx() + Angles.trnsx(entity.rotation + 180f, entity.reload * knockback),
                tile.drawy() + Angles.trnsy(entity.rotation + 180f, entity.reload * knockback), entity.rotation - 90);
    }

    @Override
    public void drawConfigure(Tile tile){
        float sin = Mathf.absin(Time.time(), 6f, 1f);

        Draw.color(Pal.accent);
        Lines.stroke(1f);
        Lines.poly(tile.drawx(), tile.drawy(), 20, (tile.block().size/2f+1) * tilesize + sin);

        MassDriverEntity entity = tile.entity();

        if(linkValid(tile)){
            Tile target = world.tile(entity.link);

            Draw.color(Pal.place);
            Lines.poly(target.drawx(), target.drawy(), 20, (target.block().size/2f+1) * tilesize + sin);
            Draw.reset();
        }

        Draw.color(Pal.accent);
        Lines.dashCircle(tile.drawx(), tile.drawy(), range);
        Draw.color();
    }

    @Override
    public boolean onConfigureTileTapped(Tile tile, Tile other){
        if(tile == other) return false;

        MassDriverEntity entity = tile.entity();

        if(entity.link == other.pos()){
            Call.linkMassDriver(null, tile, -1);
            return false;
        }else if(other.block() instanceof MassDriver && other.dst(tile) <= range){
            Call.linkMassDriver(null, tile, other.pos());
            return false;
        }

        return true;
    }

    @Override
    public boolean acceptItem(Item item, Tile tile, Tile source){
        return tile.entity.items.total() < itemCapacity;
    }

    @Override
    public TileEntity newEntity(){
        return new MassDriverEntity();
    }

    protected boolean linkValid(Tile tile){
        MassDriverEntity entity = tile.entity();
        if(entity == null || entity.link == -1) return false;
        Tile link = world.tile(entity.link);

        return link != null && link.block() instanceof MassDriver && tile.dst(link) <= range;
    }

    public static class DriverBulletData implements Poolable{
        public MassDriverEntity from, to;
        public int[] items = new int[content.items().size];

        @Override
        public void reset(){
            from = null;
            to = null;
        }
    }

    public class MassDriverEntity extends TileEntity{
        public int link = -1;
        public float rotation = 90;
        //set of tiles that currently want to distribute to this tile
        public ObjectSet<Tile> waiting = new ObjectSet<>();
        //whether this mass driver is waiting for a bullet to hit it and deliver items
        public boolean isRecieving;
        //whether this driver just recieved some items and is now unloading
        public boolean isUnloading = true;

        public float reload = 0f;

        public void handlePayload(Bullet bullet, DriverBulletData data){
            int totalItems = items.total();

            //add all the items possible
            for(int i = 0; i < data.items.length; i++){
                int maxAdd = Math.min(data.items[i], itemCapacity*2 - totalItems);
                items.add(content.item(i), maxAdd);
                data.items[i] -= maxAdd;
                totalItems += maxAdd;

                if(totalItems >= itemCapacity*2){
                    break;
                }
            }

            //drop all items remaining on the ground
            for(int i = 0; i < data.items.length; i++){
                int amountDropped = Mathf.random(0, data.items[i]);
                if(amountDropped > 0){
                    float angle = Mathf.range(180f);
                    Effects.effect(Fx.dropItem, Color.WHITE, bullet.x, bullet.y, angle, content.item(i));
                }
            }

            reload = 1f;
            Effects.shake(shake, shake, this);
            Effects.effect(recieveEffect, bullet);

            isRecieving = false;
            bullet.remove();

            if(!linkValid(tile)){
                isUnloading = true;
            }
        }

        @Override
        public void write(DataOutput stream) throws IOException{
            stream.writeInt(link);
            stream.writeFloat(rotation);
        }

        @Override
        public void read(DataInput stream) throws IOException{
            link = stream.readInt();
            rotation = stream.readFloat();
        }
    }
}