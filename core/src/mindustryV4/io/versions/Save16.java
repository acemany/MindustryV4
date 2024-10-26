package mindustryV4.io.versions;

import com.badlogic.gdx.utils.TimeUtils;
import mindustryV4.game.Difficulty;
import mindustryV4.game.GameMode;
import mindustryV4.game.Version;
import mindustryV4.io.SaveFileVersion;
import mindustryV4.maps.Map;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static mindustryV4.Vars.*;

public class Save16 extends SaveFileVersion{

    public Save16(){
        super(16);
    }

    @Override
    public void read(DataInputStream stream) throws IOException{
        stream.readLong(); //time
        stream.readLong(); //total playtime
        stream.readInt(); //build
        int sector = stream.readInt(); //sector ID

        //general state
        byte mode = stream.readByte();
        String mapname = stream.readUTF();
        Map map = world.maps.getByName(mapname);
        world.setMap(map);

        world.setSector(world.sectors.get(sector));

        int wave = stream.readInt();
        byte difficulty = stream.readByte();
        float wavetime = stream.readFloat();

        state.difficulty = Difficulty.values()[difficulty];
        state.mode = GameMode.values()[mode];
        state.wave = wave;
        state.wavetime = wavetime;

        content.setTemporaryMapper(readContentHeader(stream));

        world.spawner.read(stream);

        readEntities(stream);

        readMap(stream);
    }

    @Override
    public void write(DataOutputStream stream) throws IOException{
        //--META--
        stream.writeInt(version); //version id
        stream.writeLong(TimeUtils.millis()); //last saved
        stream.writeLong(headless ? 0 : control.saves.getTotalPlaytime()); //playtime
        stream.writeInt(Version.build); //build
        stream.writeInt(world.getSector() == null ? invalidSector : world.getSector().packedPosition()); //sector ID

        //--GENERAL STATE--
        stream.writeByte(state.mode.ordinal()); //gamemode
        stream.writeUTF(world.getMap().name); //map ID

        stream.writeInt(state.wave); //wave
        stream.writeByte(state.difficulty.ordinal()); //difficulty ordinal
        stream.writeFloat(state.wavetime); //wave countdown

        writeContentHeader(stream);

        world.spawner.write(stream); //spawnes

        //--ENTITIES--

        writeEntities(stream);

        writeMap(stream);
    }
}
