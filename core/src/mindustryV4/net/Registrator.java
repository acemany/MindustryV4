package mindustryV4.net;

import com.badlogic.gdx.utils.ObjectIntMap;
import mindustryV4.net.Packets.*;
import ucore.function.Supplier;

public class Registrator{
    private static ClassEntry[] classes = {
        new ClassEntry(StreamBegin.class, StreamBegin::new),
        new ClassEntry(StreamChunk.class, StreamChunk::new),
        new ClassEntry(WorldStream.class, WorldStream::new),
        new ClassEntry(ConnectPacket.class, ConnectPacket::new),
        new ClassEntry(InvokePacket.class, InvokePacket::new)
    };
    private static ObjectIntMap<Class> ids = new ObjectIntMap<>();

    static{
        if(classes.length > 127) throw new RuntimeException("Can't have more than 127 registered classes!");
        for(int i = 0; i < classes.length; i++){
            ids.put(classes[i].type, i);
        }
    }

    public static ClassEntry getByID(byte id){
        return classes[id];
    }

    public static byte getID(Class<?> type){
        return (byte) ids.get(type, -1);
    }

    public static ClassEntry[] getClasses(){
        return classes;
    }

    public static class ClassEntry{
        public final Class<?> type;
        public final Supplier<?> constructor;

        public <T extends Packet> ClassEntry(Class<T> type, Supplier<T> constructor){
            this.type = type;
            this.constructor = constructor;
        }
    }
}
