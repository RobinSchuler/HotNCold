
package game2;

import SimpleGameLibrarry.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import static game2.Platform.PLATFORM_BREITE;


public class SpielFeld {
    public static final float RANDBREITE_SPIELFELD = 0.05f;
    public static final int SPIELFELDGROESSE = 8;
    private final Platform[][] platformen = new Platform[SPIELFELDGROESSE][SPIELFELDGROESSE];
    private LinkedBlockingDeque<Snowflake> snow = new LinkedBlockingDeque<>();
    private static BufferedImage insertTile_up,insertTile_down,insertTile_left,insertTile_right;
    private final Animation waves = new Animation("assets/imgs/water/waves", 1, 0, 15f, true, null);
    private final Animation whale = new Animation("assets/imgs/whale/underwater", 45, 0, 2, true, null);
    private final Animation whalePopUp = new Animation("assets/imgs/whale/pop_up", 45, 0, 2, true, null);
    private final SmoothAnimationSequence whaleAnim = new SmoothAnimationSequence(whale, null, null);
    private float movex = 0, velocityx = 0, forcex = 0;
    private int whalex, whaley;
    private Platform herausgeschoben = null;
    private Random r;
    private boolean dosnow, moveBg, tileoffset = true;

    public SpielFeld(boolean dosnow, boolean moveBg, Random r) {
        this.r = r;
        this.dosnow = dosnow;
        this.moveBg = moveBg;
        /*for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                waves[i][j] = new Animation("assets/imgs/water/waves", 101, 0, 15f, true, null);
            }
        }*/
        whalex = r.nextInt(SPIELFELDGROESSE-2)+1;
        whaley = r.nextInt(SPIELFELDGROESSE-2)+1;
        for (int i = 0; i < 10 && dosnow; i++) {
            snow.add(new Snowflake());
        }
        for (int i = 0; i < SPIELFELDGROESSE; i++) {
            for (int j = 0; j < SPIELFELDGROESSE; j++) {
//                platformen[i][j] = new Platform(i, j, 11 );
                int num =  r.nextInt(Platform.NUMBER_DIFFERENT_PLATFORMS-2);
                int num2 = r.nextInt(6);
                if(num2 > num && r.nextBoolean())
                    num = num2;
                platformen[i][j] = new Platform(i, j, num );
            }
        }
        try {
            insertTile_up = ImageIO.read(new File("assets/imgs/insert_tile_up.png"));
            insertTile_down = ImageIO.read(new File("assets/imgs/insert_tile_down.png"));
            insertTile_right = ImageIO.read(new File("assets/imgs/insert_tile_right.png"));
            insertTile_left = ImageIO.read(new File("assets/imgs/insert_tile_left.png"));
        } catch (IOException ex) {
            Logger.getLogger(SpielFeld.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void addCoin(){
        int x,y ;
        x = r.nextInt(SPIELFELDGROESSE);
        y = r.nextInt(SPIELFELDGROESSE);
        if( platformen[x][y].noWarrior()){
            platformen[x][y].addCoin();
        }
    }
    
    public void schiebeInReihe(int i, Platform neu, boolean links){
        if(i <= 0 || i >= SPIELFELDGROESSE-1)
            return;
        if(links){
            herausgeschoben = platformen[SPIELFELDGROESSE-1][i];
            herausgeschoben.schiebeNachRechtsAnimation();
            for (int j = SPIELFELDGROESSE-1; j > 0; j--) {
                platformen[j][i] = platformen[j-1][i];
                platformen[j][i].schiebeNachRechtsAnimation();
            }
            platformen[0][i] = neu;
            neu.schiebeNachRechtsAnimation();
        }
        else{
            herausgeschoben = platformen[0][i];
            herausgeschoben.schiebeNachLinksAnimation();
            for (int j = 0; j < SPIELFELDGROESSE-1; j++) {
                platformen[j][i] = platformen[j+1][i];
                platformen[j][i].schiebeNachLinksAnimation();
            }
            platformen[SPIELFELDGROESSE-1][i] = neu;
            neu.schiebeNachLinksAnimation();
        }
    }
    
    public void schiebeInSpalte(int i, Platform neu, boolean oben){
        if(i <= 0 || i >= SPIELFELDGROESSE-1)
            return;
        if(oben){
            herausgeschoben = platformen[i][SPIELFELDGROESSE-1];
            herausgeschoben.schiebeNachUntenAnimation();
            for (int j = SPIELFELDGROESSE-1; j > 0; j--) {
                platformen[i][j] = platformen[i][j-1];
                platformen[i][j].schiebeNachUntenAnimation();
            }
            platformen[i][0] = neu;
            neu.schiebeNachUntenAnimation();
        }
        else{
            herausgeschoben = platformen[i][0];
            herausgeschoben.schiebeNachObenAnimation();
            for (int j = 0; j < SPIELFELDGROESSE-1; j++) {
                platformen[i][j] = platformen[i][j+1];
                platformen[i][j].schiebeNachObenAnimation();
            }
            platformen[i][SPIELFELDGROESSE-1] = neu;
            neu.schiebeNachObenAnimation();
        }
    }
    
    public void render(Game2 l){
        float breite = 2;
        for (int i = -1; i < 6; i++) {
            for (int j = -1; j < 6; j++) {
                l.drawAnimation(i*.2f*breite,.2f*j+movex,breite*.201f,.201f, waves);
            }
        }
        l.drawSmoothAnimationSequence( whalex*PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD+PLATFORM_BREITE*.01f, whaley*PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD+PLATFORM_BREITE*.01f, PLATFORM_BREITE*.98f*2, PLATFORM_BREITE*.98f*2, whaleAnim);
        if(l.getZugStatus() == Game2.zug_status.spieler1Platform || l.getZugStatus() == Game2.zug_status.spieler2Platform){
            for (int i = 1; i < SPIELFELDGROESSE-1; i++) {
                l.drawImage(insertTile_right, 0, i*PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD, SpielFeld.RANDBREITE_SPIELFELD, PLATFORM_BREITE);
                l.drawImage(insertTile_left, (SPIELFELDGROESSE)*PLATFORM_BREITE+ SpielFeld.RANDBREITE_SPIELFELD, i*PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD, SpielFeld.RANDBREITE_SPIELFELD, PLATFORM_BREITE);
                l.drawImage(insertTile_down, i*PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD, 0, PLATFORM_BREITE, SpielFeld.RANDBREITE_SPIELFELD);
                l.drawImage(insertTile_up, i*PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD, (SPIELFELDGROESSE)*PLATFORM_BREITE+ SpielFeld.RANDBREITE_SPIELFELD, PLATFORM_BREITE, SpielFeld.RANDBREITE_SPIELFELD);
            }
        }
        for (int i = 0; i < SPIELFELDGROESSE; i++) {
            for (int j = 0; j < SPIELFELDGROESSE; j++) {
                platformen[i][j].render(l,this);
            }
        }
        if(herausgeschoben != null){
            herausgeschoben.render(l,this);
        }
        for (int i = 0; i < SPIELFELDGROESSE; i++) {
            for (int j = 0; j < SPIELFELDGROESSE; j++) {
                platformen[i][j].renderEntitys(l);
            }
        }
        if(herausgeschoben != null){
            herausgeschoben.renderEntitys(l);
        }
        for (Iterator<Snowflake> iterator = snow.iterator(); iterator.hasNext();) {
            iterator.next().render(l);
        }
    }
    public void update(float passedTime, Game2 g, SimpleSoundmanager sound, int dieSound){
        if(Math.random() < passedTime*30 && dosnow)
            snow.add(new Snowflake());
        for (Iterator<Snowflake> iterator = snow.iterator(); iterator.hasNext();) {
            if(!iterator.next().update(passedTime)){
                iterator.remove();
            }
        }
        forcex += (float)(Math.random()*2-1)*passedTime*.000001f;
        if(forcex > .00001f){
            forcex = .00001f;
        }
        if(forcex < -.00001f){
            forcex = -.00001f;
        }
        velocityx += forcex;
        if(velocityx > .0001f){
            velocityx = .0001f;
        }
        if(velocityx < -.0001f){
            velocityx = -.0001f;
        }
        if(moveBg)
            movex += velocityx;
        movex %= .2f;
        for (int i = 0; i < SPIELFELDGROESSE; i++) {
            for (int j = 0; j < SPIELFELDGROESSE; j++) {
                platformen[i][j].update(passedTime,i,j,this,g,sound,dieSound);
            }
        }
        if(herausgeschoben != null)
            herausgeschoben.update(passedTime,-1,-1,this,g,sound,dieSound);
    }
    private int speicherI = -1;
    private boolean speicherRichtung = false, speicherReihe = false;
    public boolean tryMovePlatformButtons(float x, float y, int selected){
        for (int i = 1; i < SPIELFELDGROESSE-1; i++) {
            if(x >= 0 && y >= i*PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD 
                    && x <= SpielFeld.RANDBREITE_SPIELFELD  && y <= i*PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD + PLATFORM_BREITE){
                if(speicherI == i && speicherRichtung == true && speicherReihe == true)
                    return false;
                else{
                    speicherI = i;
                    speicherRichtung = false;
                    speicherReihe = true;
                }        
                schiebeInReihe(i, new Platform(-1, i, selected), true);
                return true;
            }
            if(x >= (SPIELFELDGROESSE)*PLATFORM_BREITE+ SpielFeld.RANDBREITE_SPIELFELD   && y >= i*PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD 
                    && x <= (SPIELFELDGROESSE)*PLATFORM_BREITE+ SpielFeld.RANDBREITE_SPIELFELD + SpielFeld.RANDBREITE_SPIELFELD && y <= i*PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD + PLATFORM_BREITE){
                if(speicherI == i && speicherRichtung == false && speicherReihe == true)
                    return false;
                else{
                    speicherI = i;
                    speicherRichtung = true;
                    speicherReihe = true;
                }        
                schiebeInReihe(i, new Platform(SPIELFELDGROESSE, i, selected), false);
                return true;
            }
            if(x >= i*PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD && y >= 0
                    && x <= i*PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD + PLATFORM_BREITE && y <= SpielFeld.RANDBREITE_SPIELFELD){
                if(speicherI == i && speicherRichtung == true && speicherReihe == false)
                    return false;
                else{
                    speicherI = i;
                    speicherRichtung = false;
                    speicherReihe = false;
                }        
                schiebeInSpalte(i, new Platform(i, -1, selected), true);   
                return true;
            }
            if(x >= i*PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD && y >= (SPIELFELDGROESSE)*PLATFORM_BREITE+ SpielFeld.RANDBREITE_SPIELFELD
                    && y <= (SPIELFELDGROESSE)*PLATFORM_BREITE+ SpielFeld.RANDBREITE_SPIELFELD + SpielFeld.RANDBREITE_SPIELFELD && x <= i*PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD + PLATFORM_BREITE){
                if(speicherI == i && speicherRichtung == false && speicherReihe == false)
                    return false;
                else{
                    speicherI = i;
                    speicherRichtung = true;
                    speicherReihe = false;
                }        
                schiebeInSpalte(i, new Platform(i, SPIELFELDGROESSE, selected), false);
                return true;
            }
        }
        return false;
    }
    
    public boolean moveWarrior(float x, float y, Warrior currentlySelected, Game2 g){
        return currentlySelected.setMoveThisDirection(x, y, this,g);
    }
    
    public Platform changePlatFormOfWarrior(int fromx,int fromy, int tox, int toy, Warrior w, Game2 g, SimpleSoundmanager sound, int dieSound){
        platformen[fromx][fromy].removeWarrior(w);
        platformen[tox][toy].addWarrior(w,g,sound,dieSound);
        return platformen[tox][toy];
    }
    public Platform gibPlatform(int x, int y){
        return platformen[x][y];
    }
    
    public void addMovableKrieger(LinkedBlockingDeque<Warrior> que, boolean spieler1){
        for (int i = 0; i < SPIELFELDGROESSE; i++) {
            for (int j = 0; j < SPIELFELDGROESSE; j++) {
                platformen[i][j].addMovableKrieger(que, spieler1);
            }
        }
    }
    
    public void whalemovement(boolean right, boolean up){
        if(right)
            whalex++;
        else
            whalex--;
        if(up)
            whaley--;
        else
            whaley++;
        if(whalex <= 0)
            whalex = 1;
        if(whalex >= SPIELFELDGROESSE-2)
            whalex = SPIELFELDGROESSE-3;
        if(whaley <= 0)
            whaley = 1;
        if(whaley >= SPIELFELDGROESSE-2)
            whaley = SPIELFELDGROESSE-3;
    }
    
    public void switchTurn(Game2 g, SimpleSoundmanager sound, int dieSound){
        for (int i = 0; i < SPIELFELDGROESSE; i++) {
            for (int j = 0; j < SPIELFELDGROESSE; j++) {
                platformen[i][j].switchTurn(g,sound,dieSound);
            }
        }
    }
    
    public void whaleAttack(){
        whaleAnim.forceChange(whalePopUp);
        whaleAnim.setNext(whale);
        platformen[whalex][whaley] = new Platform(whalex, whaley, 0);
        platformen[whalex+1][whaley] = new Platform(whalex+1, whaley, 0);
        platformen[whalex][whaley+1] = new Platform(whalex, whaley+1, 0);
        platformen[whalex+1][whaley+1] = new Platform(whalex+1, whaley+1, 0);
    }
    public void toogleSnow(){
        dosnow = !dosnow;
    }
    public void toogleMoveBg(){
        moveBg = !moveBg;
    }
    public void toggleTielOffset(){
        tileoffset = !tileoffset;
    }

    public boolean isTileoffset() {
        return tileoffset;
    }
    
}
