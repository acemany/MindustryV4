package mindustryV4.ui.dialogs;

import mindustryV4.game.UnlockableContent;
import io.anuke.arc.scene.ui.ScrollPane;
import io.anuke.arc.scene.ui.layout.Table;

public class ContentInfoDialog extends FloatingDialog{

    public ContentInfoDialog(){
        super("$info.title");

        addCloseButton();
    }

    public void show(UnlockableContent content){
        cont.clear();

        Table table = new Table();
        table.margin(10);

        content.displayInfo(table);

        ScrollPane pane = new ScrollPane(table);
        cont.add(pane);

        show();
    }
}
