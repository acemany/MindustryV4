package mindustryV4.ui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import mindustryV4.graphics.Palette;
import ucore.graphics.Draw;
import ucore.graphics.Lines;
import ucore.scene.ui.Image;
import ucore.scene.ui.layout.Unit;

public class BorderImage extends Image{
    private float thickness = 3f;

    public BorderImage(){

    }

    public BorderImage(Texture texture){
        super(texture);
    }

    public BorderImage(Texture texture, float thick){
        super(texture);
        thickness = thick;
    }

    public BorderImage(TextureRegion region, float thick){
        super(region);
        thickness = thick;
    }

    @Override
    public void draw(Batch batch, float alpha){
        super.draw(batch, alpha);

        float scaleX = getScaleX();
        float scaleY = getScaleY();

        Draw.color(Palette.accent);
        Lines.stroke(Unit.dp.scl(thickness));
        Lines.rect(x + imageX, y + imageY, imageWidth * scaleX, imageHeight * scaleY);
        Draw.reset();
    }
}
