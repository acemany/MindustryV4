package mindustryV4.entities.impl;

import io.anuke.arc.graphics.Color;
import io.anuke.arc.util.pooling.Pool.Poolable;
import io.anuke.arc.util.pooling.Pools;
import mindustryV4.entities.Effects;
import mindustryV4.entities.Effects.Effect;
import mindustryV4.entities.traits.DrawTrait;
import mindustryV4.entities.traits.Entity;

public class EffectEntity extends TimedEntity implements Poolable, DrawTrait{
    public Effect effect;
    public Color color = new Color(Color.WHITE);
    public Object data;
    public float rotation = 0f;

    public Entity parent;
    public float poffsetx, poffsety;

    /** For pooling use only! */
    public EffectEntity(){
    }

    public void setParent(Entity parent){
        this.parent = parent;
        this.poffsetx = x - parent.getX();
        this.poffsety = y - parent.getY();
    }

    @Override
    public float lifetime(){
        return effect.lifetime;
    }

    @Override
    public float drawSize(){
        return effect.size;
    }

    @Override
    public void update(){
        if(effect == null){
            remove();
            return;
        }

        super.update();
        if(parent != null){
            x = parent.getX() + poffsetx;
            y = parent.getY() + poffsety;
        }
    }

    @Override
    public void reset(){
        effect = null;
        color.set(Color.WHITE);
        rotation = time = poffsetx = poffsety = 0f;
        parent = null;
        data = null;
    }

    @Override
    public void draw(){
        Effects.renderEffect(id, effect, color, time, rotation, x, y, data);
    }

    @Override
    public void removed(){
        Pools.free(this);
    }
}