package mindustryV4.world.modules;

import mindustryV4.type.Liquid;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import static mindustryV4.Vars.content;

public class LiquidModule extends BlockModule{
    private float[] liquids = new float[content.liquids().size];
    private float total;
    private Liquid current = content.liquid(0);

    /**Returns total amount of liquids.*/
    public float total(){
        return total;
    }

    /**Last recieved or loaded liquid. Only valid for liquid modules with 1 type of liquid.*/
    public Liquid current(){
        return current;
    }

    public void reset(Liquid liquid, float amount){
        for(int i = 0; i < liquids.length; i++){
            liquids[i] = 0f;
        }
        liquids[liquid.id] = amount;
        total = amount;
        current = liquid;
    }

    public float currentAmount(){
        return liquids[current.id];
    }

    public float get(Liquid liquid){
        return liquids[liquid.id];
    }

    public void add(Liquid liquid, float amount){
        liquids[liquid.id] += amount;
        total += amount;
        current = liquid;
    }

    public void remove(Liquid liquid, float amount){
        add(liquid, -amount);
    }

    public void forEach(LiquidConsumer cons){
        for(int i = 0; i < liquids.length; i++){
            if(liquids[i] > 0){
                cons.accept(content.liquid(i), liquids[i]);
            }
        }
    }

    public float sum(LiquidCalculator calc){
        float sum = 0f;
        for(int i = 0; i < liquids.length; i++){
            if(liquids[i] > 0){
                sum += calc.get(content.liquid(i), liquids[i]);
            }
        }
        return sum;
    }

    @Override
    public void write(DataOutput stream) throws IOException{
        byte amount = 0;
        for(float liquid : liquids){
            if(liquid > 0) amount++;
        }

        stream.writeByte(amount); //amount of liquids

        for(int i = 0; i < liquids.length; i++){
            if(liquids[i] > 0){
                stream.writeByte(i); //liquid ID
                stream.writeFloat(liquids[i]); //item amount
            }
        }
    }

    @Override
    public void read(DataInput stream) throws IOException{
        byte count = stream.readByte();

        for(int j = 0; j < count; j++){
            int liquidid = stream.readByte();
            float amount = stream.readFloat();
            liquids[liquidid] = amount;
            if(amount > 0){
                current = content.liquid(liquidid);
            }
            this.total += amount;
        }
    }

    public interface LiquidConsumer{
        void accept(Liquid liquid, float amount);
    }

    public interface LiquidCalculator{
        float get(Liquid liquid, float amount);
    }
}
