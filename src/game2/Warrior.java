
package game2;

import SimpleGameLibrarry.*;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;


public class Warrior {
    private static BufferedImage selected = null;
    public static final float WARRIOR_SIZE = 0.1f, WARRIOR_SPEED = .15f;
    private float x = Platform.PLATFORM_BREITE/2-WARRIOR_SIZE/2,y = Platform.PLATFORM_BREITE/2-WARRIOR_SIZE/2;
    protected int dir = 0, moveCounter = 0;//stand, north, eats,south,west
    private boolean player1;
    protected Animation walk, attack, idle;
    protected SmoothAnimationSequence animationsSequenze;
    private Platform platform;
    

    public Warrior(Platform p, boolean player) {
        this.player1 = player;
        platform = p;
        try {
            if(selected == null)
                selected = ImageIO.read(new File("assets/imgs/selected.png"));
        } catch (IOException ex) {
            Logger.getLogger(Game2.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(player1)
            if(Math.random() > .5){
                idle = new Animation("assets/imgs/winter/1/idle", 30, 90, 1, true, null);
                walk = new Animation("assets/imgs/winter/1/walk", 60, 0, 1f, true, null);
                attack = new Animation("assets/imgs/winter/1/attack", 30, 60, .5f, true, null);
            }
            else{
                idle = new Animation("assets/imgs/winter/2/idle", 30, 90, 1, true, null);
                walk = new Animation("assets/imgs/winter/2/walk", 60, 0, 1f, true, null);
                attack = new Animation("assets/imgs/winter/2/attack", 30, 60, .5f, true, null);
            }
        else
            if(Math.random() > .5){
                idle = new Animation("assets/imgs/summer/1/idle", 30, 90, 1, true, null);
                walk = new Animation("assets/imgs/summer/1/walk", 60, 0, 1f, true, null);
                attack = new Animation("assets/imgs/summer/1/attack", 30, 60, .5f, true, null);
            }
            else{
                idle = new Animation("assets/imgs/summer/2/idle", 30, 90, 1, true, null);
                walk = new Animation("assets/imgs/summer/2/summer2", 60, 0, 1f, true, null);
                attack = new Animation("assets/imgs/summer/2/attack", 30, 60, .5f, true, null);
            }
        animationsSequenze = new SmoothAnimationSequence(idle, null, null);
    }
    
    
    public void addToMoveQue(LinkedBlockingDeque<Warrior> w, boolean player1){
        if(this.player1 == player1){
            if(w.isEmpty()){
                getSelected();
            }
            w.add(this);
            moveCounter = 3;
        }
    }
    
    public void switchTurn(Game2 g){
        moveCounter = 0;
    }
    
    public void render(SimpleGameLibrarry l){
        float posx = x + platform.getX()*Platform.PLATFORM_BREITE+ SpielFeld.RANDBREITE_SPIELFELD;
        float posy =y+platform.getY()*Platform.PLATFORM_BREITE+ SpielFeld.RANDBREITE_SPIELFELD;
        l.drawSmoothAnimationSequence(posx, posy, WARRIOR_SIZE, WARRIOR_SIZE, animationsSequenze);
        for (int i = 0; i < moveCounter; i++) {
            l.drawRect(posx + ((float)i / 3)*WARRIOR_SIZE,posy+WARRIOR_SIZE*.9f, (WARRIOR_SIZE/3)*.9f, WARRIOR_SIZE*.1f, new Color(0, 1, 0, .5f));
        }
    }
    public void update(float timePassed, SpielFeld feld, Game2 g,SimpleSoundmanager sound, int dieSound){
        if(dir == 0 && x + timePassed*WARRIOR_SPEED < Platform.PLATFORM_BREITE/2-WARRIOR_SIZE/2){
            x += timePassed*WARRIOR_SPEED;
        }
        else if(dir == 0 && x - timePassed*WARRIOR_SPEED > Platform.PLATFORM_BREITE/2-WARRIOR_SIZE/2){
            x -= timePassed*WARRIOR_SPEED;
        }
        else if(dir == 0){
            x = Platform.PLATFORM_BREITE/2-WARRIOR_SIZE/2;
        }
        if(dir == 0 && y + timePassed*WARRIOR_SPEED < Platform.PLATFORM_BREITE/2-WARRIOR_SIZE/2){
            y += timePassed*WARRIOR_SPEED;
        }
        else if(dir == 0 && y - timePassed*WARRIOR_SPEED > Platform.PLATFORM_BREITE/2-WARRIOR_SIZE/2){
            y -= timePassed*WARRIOR_SPEED;
        }
        else if(dir == 0){
            y = Platform.PLATFORM_BREITE/2-WARRIOR_SIZE/2;
        }
        
        if(dir == 1 && (int)platform.getY() > 0 && platform.canMoveUp() && feld.gibPlatform((int)platform.getX(), (int)platform.getY()-1).canMoveDown()){
            y-= timePassed*WARRIOR_SPEED;
        }
        else if(dir == 1){
            dir = 0;
        }
        else if(dir == 2 && (int)platform.getX() < SpielFeld.SPIELFELDGROESSE-1 && platform.canMoveRight() && feld.gibPlatform((int)platform.getX()+1, (int)platform.getY()).canMoveLeft()){
            x+= timePassed*WARRIOR_SPEED;
        }
        else if(dir == 2){
            dir = 0;
        }
        else if(dir == 3 && (int)platform.getY() < SpielFeld.SPIELFELDGROESSE-1 && platform.canMoveDown() && feld.gibPlatform((int)platform.getX(), (int)platform.getY()+1).canMoveUp()){
            y+= timePassed*WARRIOR_SPEED;
        }
        else if(dir == 3){
            dir = 0;
        }
        else if(dir == 4 && (int)platform.getX() > 0 && platform.canMoveLeft() && feld.gibPlatform((int)platform.getX()-1, (int)platform.getY()).canMoveRight()){
            x-= timePassed*WARRIOR_SPEED;
        }
        else if(dir == 4){
            dir = 0;
        }
        
        
        if(x < 0){
            x+= Platform.PLATFORM_BREITE;
            platform = feld.changePlatFormOfWarrior((int)platform.getX(), (int)platform.getY(), (int)platform.getX()-1, (int)platform.getY(), this,g,sound,dieSound);
            dir = 0;
        }
        else if(x > Platform.PLATFORM_BREITE){
            x-= Platform.PLATFORM_BREITE;
            platform = feld.changePlatFormOfWarrior((int)platform.getX(), (int)platform.getY(), (int)platform.getX()+1, (int)platform.getY(), this,g,sound,dieSound);
            dir = 0;
        }
        else if(y < 0){
            y+= Platform.PLATFORM_BREITE;
            platform = feld.changePlatFormOfWarrior((int)platform.getX(), (int)platform.getY(), (int)platform.getX(), (int)platform.getY()-1, this,g,sound,dieSound);
            dir = 0;
        }
        else if(y > Platform.PLATFORM_BREITE){
            y-= Platform.PLATFORM_BREITE;
            platform = feld.changePlatFormOfWarrior((int)platform.getX(), (int)platform.getY(), (int)platform.getX(), (int)platform.getY()+1, this,g,sound,dieSound);
            dir = 0;
        }
    }
    public boolean setMoveThisDirection(float tox, float toy, SpielFeld feld, Game2 g){
        float posx = platform.getX()*Platform.PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD,
                posy = platform.getY()*Platform.PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD;
        if(tox >= posx  && tox <= posx + Platform.PLATFORM_BREITE &&
                toy >= posy && toy <= y + posy  + Platform.PLATFORM_BREITE){
            moveCounter = 0;
            return true;//stay here!!!
        }
        else if(tox >= posx + Platform.PLATFORM_BREITE && tox <= posx + Platform.PLATFORM_BREITE*2 &&
                toy >= posy && toy <= posy  + Platform.PLATFORM_BREITE){
            dir = 2;
            moveCounter--;
            g.setCounter(1);
            animationsSequenze.forceChange(walk);
            animationsSequenze.setNext(idle);
            return moveCounter <= 0;
        }
        else if(tox >= posx - Platform.PLATFORM_BREITE && tox <= posx &&
                toy >= posy && toy <= posy  + Platform.PLATFORM_BREITE){
            dir = 4;
            moveCounter--;
            g.setCounter(1);
            animationsSequenze.forceChange(walk);
            animationsSequenze.setNext(idle);
            return moveCounter <= 0;
        }
        else if(tox >= posx && tox <= posx + Platform.PLATFORM_BREITE &&
                toy >= posy + Platform.PLATFORM_BREITE && toy <= posy  + Platform.PLATFORM_BREITE*2){
            dir = 3;
            moveCounter--;
            g.setCounter(1);
            animationsSequenze.forceChange(walk);
            animationsSequenze.setNext(idle);
            return moveCounter <= 0;
        }
        else if(tox >= posx && tox <= posx + Platform.PLATFORM_BREITE &&
                toy >= posy - Platform.PLATFORM_BREITE && toy <= posy){
            dir = 1;
            moveCounter--;
            g.setCounter(1);
            animationsSequenze.forceChange(walk);
            animationsSequenze.setNext(idle);
            return moveCounter <= 0;
        }
        return false;
    }
    public void renderSelected(SimpleGameLibrarry l){
        l.drawImage(selected, platform.getX()*Platform.PLATFORM_BREITE -Platform.PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD, platform.getY()*Platform.PLATFORM_BREITE - Platform.PLATFORM_BREITE + SpielFeld.RANDBREITE_SPIELFELD, Platform.PLATFORM_BREITE*3, Platform.PLATFORM_BREITE*3);
    }
   public void getSelected(){
            animationsSequenze.forceChange(attack);
            animationsSequenze.setNext(idle);
   }
    public void die(SimpleSoundmanager sounds, int diesoundindex){
        sounds.playSound(diesoundindex);
        platform.removeWarrior(this);//for now!!
    }

    public boolean isPlayer1() {
        return player1;
    }
    public void movementBoost(){
        moveCounter++;
    }
}
