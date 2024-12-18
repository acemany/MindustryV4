package mindustryV4.input;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import mindustryV4.content.blocks.Blocks;
import mindustryV4.core.GameState.State;
import mindustryV4.entities.Player;
import mindustryV4.game.EventType.*;
import mindustryV4.graphics.Palette;
import mindustryV4.input.PlaceUtils.NormalizeDrawResult;
import mindustryV4.input.PlaceUtils.NormalizeResult;
import mindustryV4.net.*;
import mindustryV4.type.*;
import mindustryV4.world.Block;
import mindustryV4.gen.Call;
import mindustryV4.world.Tile;
import io.anuke.annotations.Annotations.*;
import ucore.core.*;
import ucore.core.Inputs.DeviceType;
import ucore.graphics.Draw;
import ucore.graphics.Lines;
import ucore.scene.ui.layout.Unit;
import ucore.util.*;

import static mindustryV4.Vars.*;
import static mindustryV4.input.CursorType.*;
import static mindustryV4.input.PlaceMode.*;

public class DesktopInput extends InputHandler{
    private final String section;
    //controller info
    private float controlx, controly;
    private boolean controlling;
    /**Current cursor type.*/
    private CursorType cursorType = normal;

    /**Position where the player started dragging a line.*/
    private int selectX, selectY;
    /**Whether selecting mode is active.*/
    private PlaceMode mode;
    /**Animation scale for line.*/
    private float selectScale;
    /**Is player about to rotate block right now*/
    private boolean rotate = false;

    public DesktopInput(Player player){
        super(player);
        this.section = "player_" + (player.playerIndex + 1);
    }

    /**Draws a placement icon for a specific block.*/
    void drawPlace(int x, int y, Block block, int rotation){
        if(validPlace(x, y, block, rotation)){
            Draw.color();

            TextureRegion[] regions = block.getBlockIcon();

            for(TextureRegion region : regions){
                Draw.rect(region, x * tilesize + block.offset(), y * tilesize + block.offset(),
                        region.getRegionWidth() * selectScale, region.getRegionHeight() * selectScale, block.rotate ? rotation * 90 : 0);
            }
        }else{
            Draw.color(Palette.removeBack);
            Lines.square(x * tilesize + block.offset(), y * tilesize + block.offset() - 1, block.size * tilesize / 2f);
            Draw.color(Palette.remove);
            Lines.square(x * tilesize + block.offset(), y * tilesize + block.offset(), block.size * tilesize / 2f);
        }
    }

    @Override
    public boolean isDrawing(){
        return mode != none || recipe != null || rotate;
    }

    @Override
    public void drawOutlined(){
        Tile selected = tileAt(Gdx.input.getX(), Gdx.input.getY());
        int cursorX = tileX(Gdx.input.getX());
        int cursorY = tileY(Gdx.input.getY());

        //draw selection(s)
        if(mode == placing && recipe != null){
            NormalizeResult result = PlaceUtils.normalizeArea(selectX, selectY, cursorX, cursorY, rotation, true, maxLength);

            for(int i = 0; i <= result.getLength(); i += recipe.result.size){
                int x = selectX + i * Mathf.sign(cursorX - selectX) * Mathf.bool(result.isX());
                int y = selectY + i * Mathf.sign(cursorY - selectY) * Mathf.bool(!result.isX());

                if(i + recipe.result.size > result.getLength() && recipe.result.rotate){
                    Draw.color(!validPlace(x, y, recipe.result, result.rotation) ? Palette.remove : Palette.placeRotate);
                    Draw.grect("place-arrow", x * tilesize + recipe.result.offset(),
                            y * tilesize + recipe.result.offset(), result.rotation * 90 - 90);
                }

                drawPlace(x, y, recipe.result, result.rotation);
            }

            Draw.reset();
        }else if(mode == breaking){
            NormalizeDrawResult result = PlaceUtils.normalizeDrawArea(Blocks.air, selectX, selectY, cursorX, cursorY, false, maxLength, 1f);
            NormalizeResult dresult = PlaceUtils.normalizeArea(selectX, selectY, cursorX, cursorY, rotation, false, maxLength);

            for(int x = dresult.x; x <= dresult.x2; x++){
                for(int y = dresult.y; y <= dresult.y2; y++){
                    Tile tile = world.tile(x, y);
                    if(tile == null || !validBreak(tile.x, tile.y)) continue;
                    tile = tile.target();

                    Draw.color(Palette.removeBack);
                    Lines.square(tile.drawx(), tile.drawy()-1, tile.block().size * tilesize / 2f - 1);
                    Draw.color(Palette.remove);
                    Lines.square(tile.drawx(), tile.drawy(), tile.block().size * tilesize / 2f - 1);
                }
            }

            Draw.color(Palette.removeBack);
            Lines.rect(result.x, result.y - 1, result.x2 - result.x, result.y2 - result.y);
            Draw.color(Palette.remove);
            Lines.rect(result.x, result.y, result.x2 - result.x, result.y2 - result.y);
        }else if(isPlacing()){
            if(recipe.result.rotate){
                Draw.color(!validPlace(cursorX, cursorY, recipe.result, rotation) ? Palette.remove : Palette.placeRotate);
                Draw.grect("place-arrow", cursorX * tilesize + recipe.result.offset(),
                        cursorY * tilesize + recipe.result.offset(), rotation * 90 - 90);
            }
            drawPlace(cursorX, cursorY, recipe.result, rotation);
            recipe.result.drawPlace(cursorX, cursorY, rotation, validPlace(cursorX, cursorY, recipe.result, rotation));
        }

        if(rotate && !isPlacing()){
            Block block = selected.block();
            if(block != null && block.rotate && block.quickRotate){
                Draw.reset();
                rotation = selected.getRotation();
                Draw.color(Palette.placeRotate);
                Draw.grect("place-arrow", cursorX * tilesize + block.offset(),
                cursorY * tilesize + block.offset(), rotation * 90 - 90);

                Lines.square(selected.drawx(), selected.drawy(), block.size * tilesize / 2f - 1);
                block.drawPlace(cursorX * tilesize, cursorY * tilesize, rotation, true);
            }
        }

        Draw.reset();
    }

    @Override
    public void update(){
        if(Net.active() && Inputs.keyTap("player_list")){
            ui.listfrag.toggle();
        }

        if(Inputs.keyRelease(section, "select")){
            player.isShooting = false;
        }

        if(state.is(State.menu) || ui.hasDialog()) return;

        boolean controller = KeyBinds.getSection(section).device.type == DeviceType.controller;

        //zoom and rotate things
        if(Inputs.getAxisActive("zoom") && (Inputs.keyDown(section, "zoom_hold") || controller)){
            renderer.scaleCamera((int) Inputs.getAxisTapped(section, "zoom"));
        }

        renderer.minimap.zoomBy(-(int) Inputs.getAxisTapped(section, "zoom_minimap"));

        if(player.isDead()) return;

        pollInput();

        //deselect if not placing
        if(!isPlacing() && mode == placing){
            mode = none;
        }

        if(player.isShooting && !canShoot()){
            player.isShooting = false;
        }

        if(isPlacing()){
            cursorType = hand;
            selectScale = Mathf.lerpDelta(selectScale, 1f, 0.2f);
        }else{
            selectScale = 0f;
        }

        rotation = Mathf.mod(rotation + (int) Inputs.getAxisTapped(section, "rotate"), 4);

        Tile cursor = tileAt(Gdx.input.getX(), Gdx.input.getY());

        if(player.isDead()){
            cursorType = normal;
        }else if(cursor != null){
            cursor = cursor.target();

            cursorType = cursor.block().getCursor(cursor);

            if(isPlacing()){
                cursorType = hand;
            }

            if(!isPlacing() && canMine(cursor)){
                cursorType = drill;
            }

            if(canTapPlayer(Graphics.mouseWorld().x, Graphics.mouseWorld().y)){
                cursorType = unload;
            }
        }

        if(!ui.hasMouse()){
            cursorType.set();
        }

        cursorType = normal;
    }

    void pollInput(){
        Tile selected = tileAt(Gdx.input.getX(), Gdx.input.getY());
        int cursorX = tileX(Gdx.input.getX());
        int cursorY = tileY(Gdx.input.getY());

        if(Inputs.keyTap(section, "deselect")){
            player.setMineTile(null);
        }

        if(Inputs.keyTap(section, "select") && !ui.hasMouse()){
            if(isPlacing()){
                selectX = cursorX;
                selectY = cursorY;
                mode = placing;
            }else if(selected != null){
                //only begin shooting if there's no cursor event
                if (!tileTapped(selected) && !tryTapPlayer(Graphics.mouseWorld().x, Graphics.mouseWorld().y) && player.getPlaceQueue().size == 0 && !droppingItem &&
                        !tryBeginMine(selected) && player.getMineTile() == null) {
                    player.isShooting = true;
                }
            }else{ //if it's out of bounds, shooting is just fine
                player.isShooting = true;
            }
        }else if(Inputs.keyTap(section, "deselect") && (recipe != null || mode != none || player.isBuilding()) &&
        !(player.getCurrentRequest() != null && player.getCurrentRequest().breaking && KeyBinds.get(section, "deselect") == KeyBinds.get(section, "break"))){
            if(recipe == null){
                player.clearBuilding();
            }

            recipe = null;
            mode = none;
        }else if(Inputs.keyTap(section, "break") && !ui.hasMouse()){
            //is recalculated because setting the mode to breaking removes potential multiblock cursor offset
            mode = breaking;
            selectX = tileX(Gdx.input.getX());
            selectY = tileY(Gdx.input.getY());
        }

        if(Inputs.keyTap(section, "respawn") && !player.isDead()){
            Call.respawnPlayer(player);
        }

        if(Inputs.keyTap(section, "pick_select")){
            if(selected != null && selected.block() != null && (selected.discovered() | Net.active())){
                Recipe picked_recipe = Recipe.getByResult(selected.target().block());
                if(!control.unlocks.isUnlocked(picked_recipe)) return;
                rotation = selected.getRotation();
                recipe = picked_recipe;
            }
        }

        rotate = Inputs.keyDown(section, "rotate_placed");
        if(rotate){
            if(selected != null && selected.block() != null && Math.abs(Inputs.getAxisTapped("rotate")) > 0 && selected.block().rotate && selected.block().quickRotate)
                Call.rotateBlock(player, selected, Inputs.getAxisTapped(section, "rotate") > 0);
        }

        if(Inputs.keyTap(section, "clear_building") && !player.isDead()){
            player.clearBuilding();
        }

        if(Inputs.keyRelease(section, "break") || Inputs.keyRelease(section, "select")){

            if(mode == placing){ //touch up while placing, place everything in selection
                NormalizeResult result = PlaceUtils.normalizeArea(selectX, selectY, cursorX, cursorY, rotation, true, maxLength);

                for(int i = 0; i <= result.getLength(); i += recipe.result.size){
                    int x = selectX + i * Mathf.sign(cursorX - selectX) * Mathf.bool(result.isX());
                    int y = selectY + i * Mathf.sign(cursorY - selectY) * Mathf.bool(!result.isX());

                    rotation = result.rotation;

                    tryPlaceBlock(x, y);
                }
            }else if(mode == breaking){ //touch up while breaking, break everything in selection
                NormalizeResult result = PlaceUtils.normalizeArea(selectX, selectY, cursorX, cursorY, rotation, false, maxLength);
                for(int x = 0; x <= Math.abs(result.x2 - result.x); x++){
                    for(int y = 0; y <= Math.abs(result.y2 - result.y); y++){
                        int wx = selectX + x * Mathf.sign(cursorX - selectX);
                        int wy = selectY + y * Mathf.sign(cursorY - selectY);

                        tryBreakBlock(wx, wy);
                    }
                }
            }

            if(selected != null){
                tryDropItems(selected.target(), Graphics.mouseWorld().x, Graphics.mouseWorld().y);
            }

            mode = none;
        }

    }

    @Override
    public boolean selectedBlock(){
        return isPlacing() && mode != breaking;
    }

    @Override
    public float getMouseX(){
        return !controlling ? Gdx.input.getX() : controlx;
    }

    @Override
    public float getMouseY(){
        return !controlling ? Gdx.input.getY() : controly;
    }

    @Override
    public boolean isCursorVisible(){
        return controlling;
    }

    @Override
    public void updateController(){
        //TODO no controller support
        //TODO move controller input to new class, ControllerInput
        boolean mousemove = Gdx.input.getDeltaX() > 1 || Gdx.input.getDeltaY() > 1;

        if(state.is(State.menu)){
            droppingItem = false;
        }

        if(KeyBinds.getSection(section).device.type == DeviceType.controller && (!mousemove || player.playerIndex > 0)){
            if(player.playerIndex > 0){
                controlling = true;
            }

            float xa = Inputs.getAxis(section, "cursor_x");
            float ya = Inputs.getAxis(section, "cursor_y");

            if(Math.abs(xa) > controllerMin || Math.abs(ya) > controllerMin){
                float scl = Settings.getInt("sensitivity", 100) / 100f * Unit.dp.scl(1f);
                controlx += xa * baseControllerSpeed * scl;
                controly -= ya * baseControllerSpeed * scl;
                controlling = true;

                if(player.playerIndex == 0){
                    Gdx.input.setCursorCatched(true);
                }

                Inputs.getProcessor().touchDragged((int) getMouseX(), (int) getMouseY(), player.playerIndex);
            }

            controlx = Mathf.clamp(controlx, 0, Gdx.graphics.getWidth());
            controly = Mathf.clamp(controly, 0, Gdx.graphics.getHeight());
        }else{
            controlling = false;
            Gdx.input.setCursorCatched(false);
        }

        if(!controlling){
            controlx = Gdx.input.getX();
            controly = Gdx.input.getY();
        }
    }

}
