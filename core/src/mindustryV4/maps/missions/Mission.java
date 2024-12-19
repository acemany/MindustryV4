package mindustryV4.maps.missions;

import io.anuke.arc.math.geom.Point2;
import io.anuke.arc.collection.Array;
import mindustryV4.content.Blocks;
import mindustryV4.game.SpawnGroup;
import mindustryV4.game.UnlockableContent;
import mindustryV4.maps.Sector;
import mindustryV4.maps.generators.Generation;
import io.anuke.arc.util.Time;
import io.anuke.arc.scene.ui.layout.Table;
import io.anuke.arc.Core;

import static mindustryV4.Vars.*;

public abstract class Mission{
    private String extraMessage;
    private boolean showComplete = true;

    public abstract boolean isComplete();

    /**Returns the string that is displayed in-game near the menu.*/
    public abstract String displayString();

    /**Returns the info string displayed in the sector dialog (menu)*/
    public String menuDisplayString(){
        return displayString();
    }

    public String getIcon(){
        return "icon-mission-defense";
    }

    /**Sets the message displayed on mission begin. Returns this mission for chaining.*/
    public Mission setMessage(String message){
        this.extraMessage = message;
        return this;
    }

    public Mission setShowComplete(boolean complete){
        this.showComplete = complete;
        return this;
    }

    /**Called when a specified piece of content is 'used' by a block.*/
    public void onContentUsed(UnlockableContent content){

    }

    /**Draw mission overlay.*/
    public void drawOverlay(){

    }

    public void update(){

    }

    public void reset(){

    }

    /**Shows the unique sector message.*/
    public void showMessage(){
        if(!headless && extraMessage != null){

            ui.hudfrag.showTextDialog(extraMessage);
        }
    }

    public boolean hasMessage(){
        return extraMessage != null;
    }

    public void onBegin(){
        Time.runTask(60f, this::showMessage);
    }

    public void onComplete(){
        if(showComplete && !headless){
            ui.hudfrag.showToast("[LIGHT_GRAY]"+menuDisplayString() + ":\n" + Core.bundle.get("mission.complete"));
        }
    }

    public void display(Table table){
        table.add(displayString());
    }

    public Array<SpawnGroup> getWaves(Sector sector){
        return new Array<>();
    }

    public Array<Point2> getSpawnPoints(Generation gen){
        return Array.with();
    }

    public void generate(Generation gen){}
}