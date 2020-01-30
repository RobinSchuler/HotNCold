
package game2;

import SimpleGameLibrarry.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;


public class Snowflake {
    private static BufferedImage buff =null;
    private float size = (float)Math.random()/4, x = 2*(float)Math.random(), y = (float)Math.random(), rotation = (float)Math.random()*(float)Math.PI, velocityx = (float)Math.random()*2-1, velocytyy = (float)Math.random()*2-1, velocityr = (float)Math.random()*2-1;
    private static float windx = 0, windy = 0;

    public Snowflake() {
        if(buff == null){
            try {
                buff = ImageIO.read(new File("assets/imgs/snowflake.png"));
                windx = (float)Math.random()*2-1;
                windy = (float)Math.random()*2-1;
            } catch (IOException ex) {
                Logger.getLogger(Snowflake.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    
    public void render(SimpleGameLibrarry l){
        if(size > 0)
            l.drawImage(buff, x-size/2, y-size/2, size,size,rotation);
    }
    public boolean update(float passedTime){
        size *= .995f;
        size -= passedTime*0.00001f;
        velocityx += Math.random()*.01f*passedTime;
        velocytyy += Math.random()*.01f*passedTime;
        velocityr += Math.random()*.01f*passedTime;
        rotation += velocityr*passedTime*2;
        x += velocityx*passedTime*.25f + windx*passedTime*.25f;
        y += velocytyy*passedTime*.25f + windy*passedTime*.25f;
        return size > 0;
    }
}
