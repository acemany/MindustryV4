package mindustryV4.graphics;

import io.anuke.arc.Core;
import io.anuke.arc.graphics.Color;
import io.anuke.arc.graphics.g2d.TextureRegion;
import io.anuke.arc.graphics.glutils.Shader;
import io.anuke.arc.scene.ui.layout.Unit;
import io.anuke.arc.util.Time;

public class Shaders{
    public static Shadow shadow;
    public static BlockBuild blockbuild;
    public static Shield shield;
    public static UnitBuild build;
    public static FogShader fog;
    public static MenuShader menu;

    public static void init(){
        shadow = new Shadow();
        blockbuild = new BlockBuild();
        shield = new Shield();
        build = new UnitBuild();
        fog = new FogShader();
        menu = new MenuShader();
    }

    public static class MenuShader extends LoadShader{
        float time = 0f;

        public MenuShader(){
            super("menu", "default");
        }

        @Override
        public void apply(){
            time = time % 158;

            setUniformf("u_resolution", Core.graphics.getWidth(), Core.graphics.getHeight());
            setUniformi("u_time", (int)(time += Core.graphics.getDeltaTime() * 60f));
            setUniformf("u_uv", Core.atlas.white().getU(), Core.atlas.white().getV());
            setUniformf("u_scl", Unit.dp.scl(1f));
            setUniformf("u_uv2", Core.atlas.white().getU2(), Core.atlas.white().getV2());
        }
    }

    public static class FogShader extends LoadShader{
        public FogShader(){
            super("fog", "default");
        }
    }

    public static class UnitBuild extends LoadShader{
        public float progress, time;
        public Color color = new Color();
        public TextureRegion region;

        public UnitBuild(){
            super("unitbuild", "default");
        }

        @Override
        public void apply(){
            setUniformf("u_time", time);
            setUniformf("u_color", color);
            setUniformf("u_progress", progress);
            setUniformf("u_uv", region.getU(), region.getV());
            setUniformf("u_uv2", region.getU2(), region.getV2());
            setUniformf("u_texsize", region.getTexture().getWidth(), region.getTexture().getHeight());
        }
    }

    public static class Shadow extends LoadShader{
        public Color color = new Color();
        public TextureRegion region = new TextureRegion();
        public float scl;

        public Shadow(){
            super("shadow", "default");
        }

        @Override
        public void apply(){
            setUniformf("u_color", color);
            setUniformf("u_scl", scl);
            setUniformf("u_texsize", region.getTexture().getWidth(), region.getTexture().getHeight());
        }
    }

    public static class BlockBuild extends LoadShader{
        public Color color = new Color();
        public float progress;
        public TextureRegion region = new TextureRegion();

        public BlockBuild(){
            super("blockbuild", "default");
        }

        @Override
        public void apply(){
            setUniformf("u_progress", progress);
            setUniformf("u_color", color);
            setUniformf("u_uv", region.getU(), region.getV());
            setUniformf("u_uv2", region.getU2(), region.getV2());
            setUniformf("u_time", Time.time());
            setUniformf("u_texsize", region.getTexture().getWidth(), region.getTexture().getHeight());
        }
    }

    public static class Shield extends LoadShader{

        public Shield(){
            super("shield", "default");
        }

        @Override
        public void apply(){
            setUniformf("u_dp", Unit.dp.scl(1f));
            setUniformf("u_time", Time.time() / Unit.dp.scl(1f));
            setUniformf("u_offset",
            Core.camera.position.x - Core.camera.width / 2 ,
            Core.camera.position.y - Core.camera.height / 2 );
            setUniformf("u_texsize", Core.camera.width ,
            Core.camera.height );
        }
    }

    public static class LoadShader extends Shader{
        public LoadShader(String frag, String vert){
            super(Core.files.internal("shaders/" + vert + ".vertex"), Core.files.internal("shaders/" + frag + ".fragment"));
        }
    }
}
