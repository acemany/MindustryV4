package mindustryV4.entities.traits;

import io.anuke.arc.math.geom.Vector2;
import io.anuke.arc.util.Time;

public interface VelocityTrait extends MoveTrait{

    Vector2 velocity();

    default void applyImpulse(float x, float y){
        velocity().x += x / mass();
        velocity().y += y / mass();
    }

    default float maxVelocity(){
        return Float.MAX_VALUE;
    }

    default float mass(){
        return 1f;
    }

    default float drag(){
        return 0f;
    }

    default void updateVelocity(){
        velocity().scl(1f - drag() * Time.delta());

        if(this instanceof SolidTrait){
            ((SolidTrait) this).move(velocity().x * Time.delta(), velocity().y * Time.delta());
        }else{
            moveBy(velocity().x * Time.delta(), velocity().y * Time.delta());
        }
    }
}