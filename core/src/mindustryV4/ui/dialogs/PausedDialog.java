package mindustryV4.ui.dialogs;

import io.anuke.arc.Core;
import io.anuke.arc.input.KeyCode;
import mindustryV4.core.GameState.State;
import mindustryV4.net.Net;

import static mindustryV4.Vars.*;

public class PausedDialog extends FloatingDialog{
    private SaveDialog save = new SaveDialog();
    private LoadDialog load = new LoadDialog();

    public PausedDialog(){
        super("$menu");
        shouldPause = true;

        shown(this::rebuild);

        keyDown(key -> {
            if(key == KeyCode.ESCAPE || key == KeyCode.BACK) {
                hide();
            }
        });
    }

    void rebuild(){
        cont.clear();

        update(() -> {
            if(state.is(State.menu) && isShown()){
                hide();
            }
        });

        if(!mobile){
            float dw = 210f;
            cont.defaults().width(dw).height(50).pad(5f);

            cont.addButton("$back", this::hide).colspan(2).width(dw*2 + 20f);
            cont.row();
            cont.addButton("$techtree", ui.tech::show);
            //cont.addButton("$database", ui.database::show);
            cont.addButton("$settings", ui.settings::show);

            cont.row();
            cont.addButton("$savegame", save::show);
            cont.addButton("$loadgame", load::show).disabled(b -> Net.active());

            cont.row();

            cont.addButton("$hostserver", ui.host::show).disabled(b -> Net.active()).colspan(2).width(dw*2 + 20f);

            cont.row();

            cont.addButton("$quit", () -> {
                ui.showConfirm("$confirm", "$quit.confirm", () -> {
                    if(Net.client()) netClient.disconnectQuietly();
                    runExitSave();
                    hide();
                });
            }).colspan(2).width(dw + 10f);

        }else{
            cont.defaults().size(120f).pad(5);
            float isize = 14f * 4;

            cont.addRowImageTextButton("$back", "icon-play-2", isize, this::hide);
            cont.addRowImageTextButton("$settings", "icon-tools", isize, ui.settings::show);

            cont.addRowImageTextButton("$save", "icon-save", isize, save::show);

            cont.row();

            cont.addRowImageTextButton("$load", "icon-load", isize, load::show).disabled(b -> Net.active());

            cont.addRowImageTextButton("$hostserver.mobile", "icon-host", isize, ui.host::show).disabled(b -> Net.active());
            cont.addRowImageTextButton("$quit", "icon-quit", isize, () -> {
                ui.showConfirm("$confirm", "$quit.confirm", () -> {
                    if(Net.client()) netClient.disconnectQuietly();
                    runExitSave();
                    hide();
                });
            });
        }
    }

    public void runExitSave(){
        if(control.saves.getCurrent() == null ||
                !control.saves.getCurrent().isAutosave()){
            state.set(State.menu);
            return;
        }

        ui.loadAnd("$saveload", () -> {
            try{
                control.saves.getCurrent().save();
            }catch(Throwable e){
                e.printStackTrace();
                ui.showError("[accent]" + Core.bundle.get("savefail"));
            }
            state.set(State.menu);
        });
    }
}