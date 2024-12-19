package mindustryV4.type;

import io.anuke.arc.Core;
import io.anuke.arc.collection.ObjectSet;
import io.anuke.arc.function.Supplier;
import io.anuke.arc.graphics.g2d.TextureRegion;
import io.anuke.arc.scene.ui.layout.Table;
import io.anuke.arc.util.Log;
import io.anuke.arc.util.Strings;
import mindustryV4.content.Items;
import mindustryV4.entities.traits.TypeTrait;
import mindustryV4.entities.type.BaseUnit;
import mindustryV4.game.Team;
import mindustryV4.game.UnlockableContent;
import mindustryV4.ui.ContentDisplay;

public class UnitType extends UnlockableContent{
    protected final Supplier<? extends BaseUnit> constructor;
    public float health = 60;
    public float hitsize = 7f;
    public float hitsizeTile = 4f;
    public float speed = 0.4f;
    public float range = 0, attackLength = 150f;
    public float rotatespeed = 0.2f;
    public float baseRotateSpeed = 0.1f;
    public float shootCone = 15f;
    public float mass = 1f;
    public boolean isFlying;
    public boolean targetAir = true;
    public boolean rotateWeapon = false;
    public float drag = 0.1f;
    public float maxVelocity = 5f;
    public float retreatPercent = 0.2f;
    public int itemCapacity = 30;
    public ObjectSet<Item> toMine = ObjectSet.with(Items.lead, Items.copper);
    public float buildPower = 0.3f, minePower = 0.7f;
    public Weapon weapon;
    public float weaponOffsetX, weaponOffsetY, engineOffset = 6f, engineSize = 2f;
    public ObjectSet<StatusEffect> immunities = new ObjectSet<>();

    public TextureRegion iconRegion, legRegion, baseRegion, region;

    public <T extends BaseUnit> UnitType(String name, Class<T> type, Supplier<T> mainConstructor){
        super(name);
        this.constructor = mainConstructor;
        this.description = Core.bundle.getOrNull("unit." + name + ".description");

        TypeTrait.registerType(type, mainConstructor);

        if(!Core.bundle.has("unit." + this.name + ".name")){
            Log.err("Warning: unit '" + name + "' is missing a localized name. Add the follow to bundle.properties:");
            Log.err("unit." + this.name + ".name=" + Strings.capitalize(name.replace('-', '_')));
        }
    }

    @Override
    public void displayInfo(Table table){
        ContentDisplay.displayUnit(table, this);
    }

    @Override
    public String localizedName(){
        return Core.bundle.get("unit." + name + ".name");
    }

    @Override
    public TextureRegion getContentIcon(){
        return iconRegion;
    }

    @Override
    public void load(){
        weapon.load();
        iconRegion = Core.atlas.find("unit-icon-" + name, Core.atlas.find(name));
        region = Core.atlas.find(name);

        if(!isFlying){
            legRegion = Core.atlas.find(name + "-leg");
            baseRegion = Core.atlas.find(name + "-base");
        }
    }

    @Override
    public ContentType getContentType(){
        return ContentType.unit;
    }

    public BaseUnit create(Team team){
        BaseUnit unit = constructor.get();
        unit.init(this, team);
        return unit;
    }
}