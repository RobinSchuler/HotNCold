
package game2;

import SimpleGameLibrarry.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;


public class Platform {
    public static final float PLATFORM_BREITE = .9f/SpielFeld.SPIELFELDGROESSE;
    public static final float MOVEMENT_SPEED = PLATFORM_BREITE*.05f;
    public static final int NUMBER_DIFFERENT_PLATFORMS = 18;
    
    private final Animation coinAnimation = new Animation("assets/imgs/coin/coin", 3, 0, 3, true, null);
    private static final Animation connection_h = new Animation("assets/imgs/platforms/connection_h", 1, 0, 1, true, null);
    private static final Animation connection_v = new Animation("assets/imgs/platforms/connection_v", 1, 0, 1, true, null);
    private boolean coin = false;
    private static BufferedImage[] bilder = null;
    private final LinkedBlockingDeque<Warrior> krieger = new LinkedBlockingDeque<>();
    
    private float x, y, toMoveX, toMoveY, offsetx = 0, offsety = 0, velocityx = 0, velocityy = 0, rotation = 0, velocityr = 0;
    private final int typ;
    private final boolean[][] openPaths = {
        {false,false,false,false},
        {true,false,false,false},
        {false,true,false,false},
        {false,false,true,false},
        {false,false,false,true},
        {true,true,false,false},
        {true,false,true,false},
        {true,false,false,true},
        {false,true,true,false},
        {false,true,false,true},
        {false,false,true,true},
        {true,true,true,true},
        {false,true,true,true},
        {true,false,true,true},
        {true,true,false,true},
        {true,true,true,false},
        {false,false,true,true},
        {true,true,false,false},
    };// links, oben, rechts, unten

    public Platform(float x, float y, int bildTyp) {
        if(x == 0 && y == 0){
            bildTyp = NUMBER_DIFFERENT_PLATFORMS-2;
            krieger.add(new Warrior(this, true));
        }
        else if(x == SpielFeld.SPIELFELDGROESSE-1 && y == SpielFeld.SPIELFELDGROESSE-1){
            bildTyp = NUMBER_DIFFERENT_PLATFORMS-1;
            krieger.add(new Warrior(this, false));
        }
        this.x = x;
        this.y = y;
        this.toMoveX = x;
        this.toMoveY = y;
        this.typ = bildTyp;
        if(bilder == null){
            bilder = new BufferedImage[NUMBER_DIFFERENT_PLATFORMS];
            try {
                bilder[NUMBER_DIFFERENT_PLATFORMS-1] = ImageIO.read(new File("assets/imgs/platforms/base_summer.png"));
                bilder[NUMBER_DIFFERENT_PLATFORMS-2] = ImageIO.read(new File("assets/imgs/platforms/base_winter.png"));
            } catch (IOException ex) {
                Logger.getLogger(Platform.class.getName()).log(Level.SEVERE, null, ex);
            }
            for (int i = 0; i < NUMBER_DIFFERENT_PLATFORMS-2; i++) {
                try {
                    String s = "";
                    if(openPaths[i][1])
                        s+="_up";
                    if(openPaths[i][2])
                        s+="_right";
                    if(openPaths[i][3])
                        s+="_down";
                    if(openPaths[i][0])
                        s+="_left";
                    bilder[i] = ImageIO.read(new File("assets/imgs/platforms/p" +s+".png"));
                } catch (IOException ex) {
                    Logger.getLogger(Platform.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public void schiebeNachRechtsAnimation(){
        toMoveX = x + 1;
    }
    public void schiebeNachLinksAnimation(){
        toMoveX = x - 1;
    }
    public void schiebeNachObenAnimation(){
        toMoveY = y - 1;
    }
    public void schiebeNachUntenAnimation(){
        toMoveY = y + 1;
    }
    public void render(SimpleGameLibrarry l, SpielFeld f){
        if(!f.isTileoffset()){
            rotation = 0;
        }
            
        l.drawImage(bilder[typ], getXOffset(f.isTileoffset())*PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD+PLATFORM_BREITE*.1f, getYOffset(f.isTileoffset())*PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD+PLATFORM_BREITE*.1f, PLATFORM_BREITE*.8f, PLATFORM_BREITE*.8f,rotation);
        
        if(y > 0 && (int)x >= 0 && x < SpielFeld.SPIELFELDGROESSE && y < SpielFeld.SPIELFELDGROESSE && toMoveY == y && toMoveX == x && canMoveUp() && f.gibPlatform((int)x, (int)y-1).canMoveDown())
            l.drawAnimation(getXOffset(f.isTileoffset())*PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD+PLATFORM_BREITE*.25f, getYOffset(f.isTileoffset())*PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD-+PLATFORM_BREITE*.2f, PLATFORM_BREITE*.5f, PLATFORM_BREITE*.4f, connection_h);
        if(x > 0 && (int)y >= 0 && x < SpielFeld.SPIELFELDGROESSE && y < SpielFeld.SPIELFELDGROESSE && toMoveX == x && toMoveY == y && canMoveLeft() && f.gibPlatform((int)x-1, (int)y).canMoveRight())
            l.drawAnimation(getXOffset(f.isTileoffset())*PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD-+PLATFORM_BREITE*.2f, getYOffset(f.isTileoffset())*PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD+PLATFORM_BREITE*.25f, PLATFORM_BREITE*.4f, PLATFORM_BREITE*.5f, connection_v);
        
        if(coin){
            l.drawAnimation(getX()*PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD + PLATFORM_BREITE*.2f, getY()*PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD + PLATFORM_BREITE*.2f, PLATFORM_BREITE*.6f, PLATFORM_BREITE*.6f, coinAnimation);
        }
    }
    public BufferedImage getImage(){
        return bilder[typ];
    }
    public void renderEntitys(SimpleGameLibrarry l){
        for (Iterator<Warrior> iterator = krieger.iterator(); iterator.hasNext();) {
            iterator.next().render(l);
        }
    }
    
    public void switchTurn(Game2 g, SimpleSoundmanager sound, int dieSound){
        for (Iterator<Warrior> iterator = krieger.iterator(); iterator.hasNext();) {
            iterator.next().switchTurn(g);
        }
        if(g.r.nextFloat() < .005)
            addWarrior(new reaperSnowman(this, false), g, sound,dieSound);
    }
    
    public void update(float timePassed, int posx, int posy,SpielFeld feld, Game2 g, SimpleSoundmanager sound, int dieSound){
        velocityx += (float)(Math.random()*2-1)*0.01f*timePassed;
        velocityy += (float)(Math.random()*2-1)*0.01f*timePassed;
        velocityr += (float)(Math.random()*2-1)*0.01f*timePassed;
        if(velocityr > .02f)
            velocityr = .02f;
        if(velocityr < -.02f)
            velocityr = -.02f;
        if(velocityx > .02f)
            velocityx = .02f;
        if(velocityx < -.02f)
            velocityx = -.02f;
        if(velocityy > .02f)
            velocityy = .02f;
        if(velocityy < -.02f)
            velocityy = -.02f;
        offsetx += velocityx*timePassed;
        offsety += velocityy*timePassed;
        rotation += velocityr*timePassed;
        if(offsetx > .05f){
            offsetx = .05f;
            velocityx =0;
        }
        if(offsetx < -.1f){
            offsetx = -.1f;
            velocityx =0;
        }
        if(offsety > .1f){
            offsety = .1f;
            velocityy =0;
        }
        if(offsety < -.1f){
            offsety = -.1f;
            velocityy =0;
        }
        if(rotation > .1f){
            rotation = .1f;
            velocityr =0;
        }
        if(rotation < -.1f){
            rotation = -.1f;
            velocityr =0;
        }
        if(x + MOVEMENT_SPEED <= toMoveX){
            x += MOVEMENT_SPEED;
        }
        else if(x - MOVEMENT_SPEED >= toMoveX){
            x -= MOVEMENT_SPEED;
        }
        else{
            x = toMoveX;
        }
        
        if(y + MOVEMENT_SPEED <= toMoveY){
            y += MOVEMENT_SPEED;
        }
        else if(y - MOVEMENT_SPEED >= toMoveY){
            y -= MOVEMENT_SPEED;
        }
        else{
            y = toMoveY;
        }
        for (Iterator<Warrior> iterator = krieger.iterator(); iterator.hasNext();) {
            iterator.next().update(timePassed,feld,g,sound,dieSound);
        }
    }
    public boolean canMoveUp(){
        return openPaths[typ][1];
    }
    public boolean canMoveDown(){
        return openPaths[typ][3];
    }
    public boolean canMoveLeft(){
        return openPaths[typ][0];
    }
    public boolean canMoveRight(){
        return openPaths[typ][2];
    }
    public void addWarrior(Warrior w, Game2 g, SimpleSoundmanager sound, int dieSound){
        krieger.add(w);
        checkForFight(w,sound,dieSound);
        if(coin){
            coin = false;
            if(w.isPlayer1())
                g.addCoinPlayer1();
            else
                g.addCoinPlayer2();
        }
        if((int)getX() == 0 && (int)getY() == 0 && !w.isPlayer1() && !(w instanceof reaperSnowman)){
            g.spieler1Gewinnt();
        }
        if((int)getX() == SpielFeld.SPIELFELDGROESSE-1 && (int)getY() == SpielFeld.SPIELFELDGROESSE-1 && w.isPlayer1() && !(w instanceof reaperSnowman)){
            g.spieler2Gewinnt();
        }
    }
    public void removeWarrior(Warrior w){
        krieger.remove(w);
    }
    
    public void addMovableKrieger(LinkedBlockingDeque<Warrior> que, boolean spieler1){
        for (Iterator<Warrior> iterator = krieger.iterator(); iterator.hasNext();) {
            iterator.next().addToMoveQue(que, spieler1);
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getXOffset(boolean tileoffset) {
        if(!tileoffset)
            return x;
        return x +offsetx;
    }

    public float getYOffset(boolean tileoffset) {
        if(!tileoffset)
            return y;
        return y + offsety;
    }
    
    public void checkForFight(Warrior w, SimpleSoundmanager sound, int dieSound){
        for (Iterator<Warrior> iterator = krieger.iterator(); iterator.hasNext();) {
            Warrior next = iterator.next();
            if(next.isPlayer1() != w.isPlayer1() || (w instanceof reaperSnowman != next instanceof reaperSnowman)){
                next.die(sound, dieSound);
                break;
            }
        }
        for (Iterator<Warrior> iterator = krieger.iterator(); iterator.hasNext();) {
            Warrior next = iterator.next();
            if(next.isPlayer1() != w.isPlayer1() || (w instanceof reaperSnowman != next instanceof reaperSnowman)){
                w.die(sound, dieSound);
                break;
            }
        }
    }
    public boolean noWarrior(){
        return krieger.isEmpty();
    }
    public void addCoin(){
        coin = true;
    }

    public int getTyp() {
        return typ;
    }
    
}
