
package game2;

import SimpleGameLibrarry.*;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;


public class Game2 extends SimpleGameLibrarry{ //Entire Game on One Screen.
    private SimpleSoundmanager soundManager = new SimpleSoundmanager(2);
    private final static int KOSTEN_KRIEGER = 15, KOSTEN_MOVEMENT_BOOST = 5, WHALE_ATTACK_KOSTEN = 20;
    SpielFeld spielFeld;
    private boolean spielerA = false;//true == sommer
    private BufferedImage mouse;
    private BufferedImage mWelle;
    private float mouseX, mouseY, counter = 2;
    private zug_status zugStatus = zug_status.spielerUebergang;
    private int geld_player1 = 100, geld_player2 = 100, selected = 0;
    private LinkedBlockingDeque<Warrior> currentlyMovingQue = new LinkedBlockingDeque<>();
    private Animation summer_turn = new Animation("assets/imgs/summer_turn", 1, 0, 2, true, null);
    private Animation winter_turn = new Animation("assets/imgs/winter_turn", 1, 0, 2, true, null);
    private Animation summer_win = new Animation("assets/imgs/summer_win", 1, 0, 2, true, null);
    private Animation winter_win = new Animation("assets/imgs/winter_win", 1, 0, 2, true, null);
    private Client c;
    private Platform[] toSelect = new Platform[3];
    boolean mainMenue = true,soundMenue = false,grafikMenue = false,howToMenue = false,colorMenue = false,colorMenue2 = false; //new color menue
    Color myColor = Color.RED;
    private Animation winter1 = new Animation("assets/imgs/winter/1/idle", 30, 90, 1, true, null);
    private Animation winter2 = new Animation("assets/imgs/winter/2/idle", 30, 90, 1, true, null);
    private Animation summer1 = new Animation("assets/imgs/summer/1/idle", 30, 90, 1, true, null);
    private Animation summer2 = new Animation("assets/imgs/summer/2/idle", 30, 90, 1, true, null);
    public Random r;
    private final int click, death, newPlayer, whale, coin;
    
    public enum zug_status {
        spieler1Platform,
        spieler1Movement,
        spielerUebergang,
        spieler2Platform,
        spieler2Movement,
        winter_wins,
        summer_wins,
    }
    
    public Game2(int resolutionX, int resolutionY, int maxFps, int maxTps, boolean windowed, boolean movingBackground, boolean snow, float music, float sound, float master, boolean musicB, boolean soundB, boolean masterB, Client c, boolean tileOffset) {
        this.c = c;
        new Animation("assets/imgs/reaperSnowman/idle", 60, 90, 1, true, null);//preload
        new Animation("assets/imgs/reaperSnowman/walk", 90, 0, 1f, true, null);//preload
        r = new Random(c.getSeed());
        int blup = soundManager.registerSound(0, "assets/sound/blupXD", false);
        click = soundManager.registerSound(1, "assets/sound/click", false);
        death = soundManager.registerSound(1, "assets/sound/death", false);
        newPlayer = soundManager.registerSound(1, "assets/sound/newPlayer", false);
        whale = soundManager.registerSound(1, "assets/sound/whale", false);
        coin = soundManager.registerSound(1, "assets/sound/coin", false);
        soundManager.setMasterVolumeOn(masterB);
        soundManager.setChannelVolumeOn(0, musicB);
        soundManager.setChannelVolumeOn(1, soundB);
        soundManager.changeMasterVolume(master);
        soundManager.changeChanelVolume(0, music);
        soundManager.changeChanelVolume(1, sound);
        spielFeld = new SpielFeld(snow,movingBackground, r);
        if(!tileOffset)
            spielFeld.toggleTielOffset();
        toSelect[0] = new Platform(-1, -1, r.nextInt(Platform.NUMBER_DIFFERENT_PLATFORMS-2));
        toSelect[1] = new Platform(-1, -1, r.nextInt(Platform.NUMBER_DIFFERENT_PLATFORMS-2));
        toSelect[2] = new Platform(-1, -1, r.nextInt(Platform.NUMBER_DIFFERENT_PLATFORMS-2));
        try {
            mouse = ImageIO.read(new File("assets/imgs/mouse.png"));
        } catch (IOException ex) {
            Logger.getLogger(Game2.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            mWelle = ImageIO.read(new File("assets/imgs/welle.png"));
        } catch (IOException ex) {
            Logger.getLogger(Game2.class.getName()).log(Level.SEVERE, null, ex);
        }
        start(resolutionX, resolutionY, maxFps, maxTps, "TITLE", windowed, false);
        setIcon("assets/imgs/icon.png");
        while (true) {          
            soundManager.playSound(blup);
            try {
                Thread.sleep(60_500);
            } catch (InterruptedException ex) {
                Logger.getLogger(Game2.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void setCounter(float c){
        counter = c;
    }
    /*
    public static void main(String[] args) {
        new Game(800, 600, 120, 120, false);
//        new Game(1920, 1080, 120, 120, false);
    }*/

    @Override
    public void onRender() {
        //drawRect(0, 0, getResolutionX()/(float)getResolutionY(), 1, new Color(58, 33, 207));
        spielFeld.render(this);
        //mouseX >= 1.1f && mouseX <= 1.2f && mouseY >= .25f && mouseY <= .26f
        /*drawRect( 1.095f + selected*.15f, .245f, .11f, .11f, Color.red);
        drawImage(toSelect[0].getImage(), 1.1f, .25f,.1f,.1f);
        drawImage(toSelect[1].getImage(), 1.25f, .25f,.1f,.1f);
        drawImage(toSelect[2].getImage(), 1.4f, .25f,.1f,.1f);*/
    }

    public zug_status getZugStatus() {
        return zugStatus;
    }
    
    

    @Override
    public void onShutdown() {
    }

    @Override
    public void onRenderLight() {
    }

    @Override
    public void onRenderGui() {
        /*if(zugStatus == zug_status.summer_wins || zugStatus == zug_status.winter_wins){
            if(zugStatus == zug_status.summer_wins)
                drawString("Summer Wins.", .5f, .5f);
            else
                drawString("Winter Wins.", .5f, .5f);
            return;
        }
        if(!currentlyMovingQue.isEmpty() && counter <= 0)
            currentlyMovingQue.peek().renderSelected(this);
        drawString("fps: " + getCurrFps(), .1f, .925f, Color.GREEN);
        drawString("tps: " + getCurrTps(), .1f, .95f, Color.GREEN);
        drawString("Coins Winter: " + geld_player1, 1.1f, .1f, Color.red);
        drawString("Coins Summer: " + geld_player2, 1.1f, .125f, Color.red);
        drawAnimation(1.1f, .15f, .1f, .05f, new_budden);
        drawAnimation(1.25f, .15f, .1f, .05f, skip_turn);
        drawAnimation(1.4f, .15f, .1f, .05f, whale_attack);
        drawAnimation(1.55f, .15f, .1f, .05f, movement_boost);
        if(counter <= 0 && (!c.lanMultiplayer() || (c.isSummer() == spielerA)))
            drawImage(mouse, mouseX, mouseY,0.06f,0.06f);
        if(zugStatus == zug_status.spielerUebergang && counter < 2){
            if(spielerA){//winter
                drawAnimation(.1f, .1f, .8f, .8f, winter_turn);
            }
            else{
                drawAnimation(.1f, .1f, .8f, .8f, summer_turn);
            }
        }*/
        if(!currentlyMovingQue.isEmpty())
            currentlyMovingQue.peek().renderSelected(this);
        
        drawRect(1.05f, .0f, 1f, 1.1f, myColor);
        drawRect(1.05f, .0f, 0.02f, 1.1f, Color.black);
        drawRect(1.05f, 0.7f, 1f, .005f, Color.black);
        drawRectOutline(1.05f, 0.7f, 1f, 0.4f, Color.black);
        drawRectOutline(1.05f, 0.7f, 1f, 0.1f, Color.black);
        drawRectOutline(1.05f, 0.75f, 1f, 0.1f, Color.black);
        //drawRectOutline(1f, 0.8f, 1f, 0.1f, Color.black); //
        drawRectOutline(1.05f, 0.85f, 1f, 0.1f, Color.black);
        drawRectOutline(1.05f, 0.9f, 1f, 0.1f, Color.black);
        drawString("fps: " + getCurrFps(), 1.075f, .9775f, Color.black);
        drawString("tps: " + getCurrTps(), 1.075f, .99f, Color.black);
        drawString("Coins Winter: " + geld_player1, 1.075f, .05f, Color.black);
        drawString("Coins Summer: " + geld_player2, 1.075f, .1f, Color.black);
        drawRectOutline(1.05f, .15f, 1f, .05f, Color.black);
        //drawRectOutline(1f, .2f, 1f, .05f, Color.black);//
        drawRectOutline(1.05f, .25f, 1f, .05f, Color.black);
        drawRectOutline(1.05f, .3f, 1f, .05f, Color.black);
        drawRect(1.05f, .35f, 1f, .005f, Color.black);
        //drawAnimation(1.6f, .15f, .1f, .05f, new_budden);
        drawString("new warrior: 15 Coins", 1.075f, .175f, Color.black);
        //drawAnimation(1.6f, .2f, .1f, .05f, skip_turn);    
        drawString("skip", 1.075f, .225f, Color.black);
        //drawAnimation(1.6f, .25f, .1f, .05f, whale_attack);
        drawString("whale attack: 20 Coins", 1.075f, .275f, Color.black);
        //drawAnimation(1.6f, .3f, .1f, .05f, movement_boost);   
        drawString("movement boost: 5 Coins", 1.075f, .325f, Color.black);
        drawImage(mWelle, 0.825f, -.05f, 0.3f, .6f);
        
        if (mainMenue == true){
        drawString("Click below to choose Menue:", 1.075f, 0.725f, Color.black);
        drawString("Soundmenue", 1.075f, 0.775f, Color.black);
        drawString("Grafikmenue", 1.075f, 0.825f, Color.black);
        drawString("How to...?", 1.075f, 0.875f, Color.black);
        drawString("press ESC to leave", 1.075f, 0.925f, Color.black);
        }
        else if (soundMenue == true)
        {
           drawString("main sound on/off", 1.075f, 0.775f, Color.black);
              drawString("backgroundmusik on/off", 1.075f, 0.825f, Color.black);
              drawString("soundeffects on/off", 1.075f, 0.875f, Color.black);
              drawString("back to Menue", 1.075f, 0.925f, Color.black);
        }
        else if (grafikMenue == true)
        {
         drawString("snowflakes on/off", 1.075f, 0.775f, Color.black);
         drawString("moving water on/off", 1.075f, 0.825f, Color.black);
         drawString("moving ice on/off", 1.075f, 0.875f, Color.black);
         // color neu
         drawString("set menue color", 1.075f, 0.725f, Color.black);
         drawString("back to Menue", 1.075f, 0.925f, Color.black);
        }
        //color color2 new
        else if (colorMenue == true){
        	 drawString("green", 1.075f, 0.725f, Color.black);
             drawString("cyan", 1.075f, 0.775f, Color.black);
             drawString("orange", 1.075f, 0.825f, Color.black);
             drawString("gray", 1.075f, 0.875f, Color.black);
             drawString("more colors/back to Menue", 1.075f, 0.925f, Color.black);
        }
        else if (colorMenue2 == true)
        {
        	 drawString("pink", 1.075f, 0.725f, Color.black);
             drawString("red", 1.075f, 0.775f, Color.black);
             drawString("yellow", 1.075f, 0.825f, Color.black);
             drawString("white", 1.075f, 0.875f, Color.black);
             drawString("back to Grafik Menue", 1.075f, 0.925f, Color.black);
             
    
        }
        
        else if (howToMenue == true)
        {
         drawString("back to Menue", 1.075f, 0.925f, Color.black);
        
        }
        
        
        
        if(zugStatus == zug_status.spielerUebergang && counter < 2){
            if(spielerA){//winter
                drawAnimation(0, 0, 1, 1, winter_turn);
                drawAnimation(.1f, .5f, .4f, .4f, winter1);
                drawAnimation(.6f, .5f, .4f, .4f, winter2);
            }
            else{
                drawAnimation(0, 0, 1, 1, summer_turn);
                drawAnimation(.1f, .5f, .4f, .4f, summer1);
                drawAnimation(.6f, .5f, .4f, .4f, summer2);
            }
        }
        
        drawImage(toSelect[0].getImage(), 1.075f, .36f,.1f,.1f);
        drawImage(toSelect[1].getImage(), 1.075f, .475f,.1f,.1f);
        drawImage(toSelect[2].getImage(), 1.075f, .59f,.1f,.1f);
      drawRectOutline( 1.075f , .36f + selected*.115f, .1f, .1f, Color.black);
         drawString("select Platform", 1.185f, 0.525f, Color.black);
    
        if(zugStatus == zug_status.summer_wins || zugStatus == zug_status.winter_wins){
            if(zugStatus == zug_status.summer_wins){
                drawAnimation(0, 0, 1, 1, summer_win);
                drawAnimation(.1f, .5f, .4f, .4f, summer1);
                drawAnimation(.6f, .5f, .4f, .4f, summer2);
            }
            else{
                drawAnimation(0, 0, 1, 1, winter_win);
                drawAnimation(.1f, .5f, .4f, .4f, winter1);
                drawAnimation(.6f, .5f, .4f, .4f, winter2);
            }
        }
        if(counter <= 0 && (!c.lanMultiplayer() || (c.isSummer() == spielerA)))
            drawImage(mouse, mouseX, mouseY,0.06f,0.06f);
    }

    @Override
    public void onUpdate(float passedTime) {
        if(zugStatus == zug_status.summer_wins || zugStatus == zug_status.winter_wins)
            return;
        spielFeld.update(passedTime, this,soundManager,death);
        if(counter >= 0){
            counter -= passedTime;
            if(counter <= 0 && zugStatus == zug_status.spielerUebergang)
                nextPart();
        }
        if(c.lanMultiplayer() && (c.isSummer() != spielerA)){
            if(c.isMouseAvailiable()){
                mouseX = c.waitForMouse();
                mouseY = c.waitForMouse();
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        mouseExecute(mouseX, mouseY);
                    }
                }).start();
            }
        }
    }

    @Override
    public void onKey(int keyId, boolean pressed) {
        if(keyId == KeyEvent.VK_ESCAPE)
            shutdown();
    }

    @Override
    public void onMouse(int Id, boolean pressed, int x, int y) {
        if(zugStatus == zug_status.summer_wins || zugStatus == zug_status.winter_wins)
            return;
        if(counter > 0)
            return;
        mouseX = x/(float)getResolutionX()*(float)getResolutionX()/(float)getResolutionY();
        mouseY = y/(float)getResolutionY();
        if(c.lanMultiplayer() && (c.isSummer() != spielerA))
            return;
        if(!pressed){
            if(c.lanMultiplayer()){
                if(mouseX < 1)
                    c.sendMouseKlick(mouseX, mouseY);
            }
            //soundManager.playSound(click);
            mouseExecute(mouseX, mouseY);
        }
    }
    
    private void mouseExecute(float mouseX, float mouseY){
        /*if( (zugStatus == zug_status.spieler1Platform || zugStatus == zug_status.spieler2Platform) && spielFeld.tryMovePlatformButtons(mouseX, mouseY, toSelect[selected].getTyp())){
            //done
            counter = 1.5f;
            nextPart();
            toSelect[0] = new Platform(-1, -1, r.nextInt(Platform.NUMBER_DIFFERENT_PLATFORMS-2));
            toSelect[1] = new Platform(-1, -1, r.nextInt(Platform.NUMBER_DIFFERENT_PLATFORMS-2));
            toSelect[2] = new Platform(-1, -1, r.nextInt(Platform.NUMBER_DIFFERENT_PLATFORMS-2));
            selected = 0;
        }
        else if(mouseX >= 1.1f && mouseX <= 1.2f && mouseY >= .15f && mouseY <= .2f){
            newWarrior();
        }
        else if(mouseX >= 1.1f && mouseX <= 1.2f && mouseY >= .25f && mouseY <= .35f){
            selected = 0;
        }
        else if(mouseX >= 1.25f && mouseX <= 1.35f && mouseY >= .25f && mouseY <= .35f){
            selected = 1;
        }
        else if(mouseX >= 1.4f && mouseX <= 1.5f && mouseY >= .25f && mouseY <= .35f){
            selected = 2;
        }
        else if(mouseX >= 1.25f && mouseX <= 1.35f && mouseY >= .15f && mouseY <= .2f){
            nextPart();
        }
        else if(mouseX >= 1.4f && mouseX <= 1.5f && mouseY >= .15f && mouseY <= .2f){
            whaleattack();
        }
        else if(mouseX >= 1.55f && mouseX <= 1.6f && mouseY >= .15f && mouseY <= .2f){
            movementBoost();
        }
        else if(!currentlyMovingQue.isEmpty() && spielFeld.moveWarrior(mouseX, mouseY, currentlyMovingQue.peek(),this)){
            currentlyMovingQue.poll();
            if(currentlyMovingQue.isEmpty())
                nextPart();
        }*/ 
        if( (zugStatus == zug_status.spieler1Platform || zugStatus == zug_status.spieler2Platform) && spielFeld.tryMovePlatformButtons(mouseX, mouseY, toSelect[selected].getTyp())){
                //done
                counter = 1.5f;
                nextPart();
                toSelect[0] = new Platform(-1, -1, r.nextInt(Platform.NUMBER_DIFFERENT_PLATFORMS-2));
                toSelect[1] = new Platform(-1, -1, r.nextInt(Platform.NUMBER_DIFFERENT_PLATFORMS-2));
                toSelect[2] = new Platform(-1, -1, r.nextInt(Platform.NUMBER_DIFFERENT_PLATFORMS-2));
                selected = 0;
        }     
            
        else if(mouseX >= 1f && mouseY >= .15f && mouseY <= .2f){
            newWarrior();
        }
        /*drawImage(toSelect[0].getImage(), 1.05f, .36f,.1f,.1f);
        drawImage(toSelect[1].getImage(), 1.05f, .475f,.1f,.1f);
        drawImage(toSelect[2].getImage(), 1.05f, .59f,.1f,.1f);*/
        else if(mouseX >= 1 && mouseY >= .36f && mouseY <= .46f){
            selected = 0;
            if(c.lanMultiplayer())
                c.sendMouseKlick(mouseX, mouseY);
        }
        else if(mouseX >= 1 && mouseY >= .475f && mouseY <= .575f){
            selected = 1;
            if(c.lanMultiplayer())
                c.sendMouseKlick(mouseX, mouseY);
        }
        else if(mouseX >= 1 && mouseY >= .59f && mouseY <= .69f){
            selected = 2;
            if(c.lanMultiplayer())
                c.sendMouseKlick(mouseX, mouseY);
        }
        // drunter neu
        else if (mouseX >= 1f && mouseY>= 0.7f && mouseY <= 0.7499f && grafikMenue == true){
        	grafikMenue = false;
        	colorMenue = true;
        }

        else if(mouseX >= 1f && mouseY >= 0.75f && mouseY <= 0.8f && mainMenue == true){
            mainMenue = false;
            soundMenue = true;
        }
        else if(mouseX >= 1f && mouseY >= 0.75f && mouseY <= 0.8f && grafikMenue == true){
            spielFeld.toogleSnow();
        }
        else if(mouseX >= 1f && mouseY >= 0.75f && mouseY <= 0.8f && soundMenue == true){
            soundManager.setMasterVolumeOn(!soundManager.isMasterVolumeOn());
        }
        else if(mouseX >= 1f && mouseY >= 0.8001f && mouseY <= 0.85f && mainMenue == true){
            mainMenue = false;
               grafikMenue = true; 
        }
        else if(mouseX >= 1f && mouseY >= 0.8001f && mouseY <= 0.85f && grafikMenue == true){
            spielFeld.toogleMoveBg();
        }
        else if(mouseX >= 1f && mouseY >= 0.8001f && mouseY <= 0.85f && soundMenue == true){
            soundManager.setChannelVolumeOn(0, !soundManager.isChannelVolumeOn(0));
        }
        else if(mouseX >= 1f && mouseY >= 0.85001f && mouseY <= 0.9f && mainMenue == true){
            mainMenue = false;
               howToMenue = true; 
        }
        else if(mouseX >= 1f && mouseY >= 0.85001f && mouseY <= 0.9f && grafikMenue == true){
            spielFeld.toggleTielOffset();
        }
        else if(mouseX >= 1f && mouseY >= 0.85001f && mouseY <= 0.9f && soundMenue == true){
            soundManager.setChannelVolumeOn(1, !soundManager.isChannelVolumeOn(1));
        }
        else if(mouseX >= 1f && mouseY >= 0.9001f && mouseY <= 0.95f && mainMenue == true){
        //mainMenue = false;
        }
        else if(mouseX >= 1f && mouseY >= 0.9001f && mouseY <= 0.95f && mainMenue == false && colorMenue == false && colorMenue2 == false){
            mainMenue = true;
            soundMenue = false;
            grafikMenue = false;
            howToMenue = false;

        }
        //new color1 true color 2 true und farben
        else if(mouseX >= 1f && mouseY >= 0.9001f && mouseY <= 0.95f && mainMenue == false && colorMenue == true){
        	colorMenue = false;
        	colorMenue2 = true;
        }
        else if(mouseX >= 1f && mouseY >= 0.9001f && mouseY <= 0.95f && mainMenue == false && colorMenue2 == true){
        	colorMenue2 = false;
        	grafikMenue = true;
        }
      
        else if (mouseX >= 1f && mouseY>= 0.7f && mouseY <= 0.7499f && colorMenue == true){
        	myColor = Color.green;
        }
        else if (mouseX >= 1f && mouseY>= 0.7f && mouseY <= 0.7499f && colorMenue2 == true){
        	myColor = Color.PINK;
        }
        else if (mouseX >= 1f && mouseY>= 0.75f && mouseY <= 0.7999f && colorMenue == true){
        	myColor = Color.cyan;
        }
        else if (mouseX >= 1f && mouseY>= 0.75f && mouseY <= 0.7999f && colorMenue2 == true){
        	myColor = Color.RED;
        }
        else if (mouseX >= 1f && mouseY>= 0.8f && mouseY <= 0.8499f && colorMenue == true){
        	myColor = Color.orange;
        }
        else if (mouseX >= 1f && mouseY>= 0.8f && mouseY <= 0.8499f && colorMenue2 == true){
        	myColor = Color.yellow;
        }
        else if (mouseX >= 1f && mouseY>= 0.85f && mouseY <= 0.8999f && colorMenue == true){
        	myColor = Color.gray;
        }
        else if (mouseX >= 1f && mouseY>= 0.85f && mouseY <= 0.8999f && colorMenue2 == true){
        	myColor = Color.white;
        }
        
 
      
        
        else if(mouseX >= 1f && mouseY >= .2f && mouseY <= .25f){
            nextPart();
            if(c.lanMultiplayer())
                c.sendMouseKlick(mouseX, mouseY);
        }
        else if(mouseX >= 1f && mouseY >= .25f && mouseY <= .3f){
            whaleattack();
            if(c.lanMultiplayer())
                c.sendMouseKlick(mouseX, mouseY);
        }
        else if(mouseX >= 1f && mouseY >= .3f && mouseY <= .35f){
            movementBoost();
            if(c.lanMultiplayer())
                c.sendMouseKlick(mouseX, mouseY);
        }
        else if(!currentlyMovingQue.isEmpty() && spielFeld.moveWarrior(mouseX, mouseY, currentlyMovingQue.peek(),this)){
            currentlyMovingQue.poll();
            if(!currentlyMovingQue.isEmpty())
                currentlyMovingQue.peek().getSelected();
            if(currentlyMovingQue.isEmpty())
                nextPart();
        }
        
    }
    
    public void nextPart(){
        if(zugStatus == zug_status.spieler1Platform){
            zugStatus = zug_status.spieler1Movement;
            spielFeld.addCoin();
            spielFeld.addMovableKrieger(currentlyMovingQue, false);
        }
        
        else if(zugStatus == zug_status.spieler1Movement){
            zugStatus = zug_status.spielerUebergang;
            currentlyMovingQue.clear();
            spielFeld.switchTurn(this,soundManager,death);
            counter = 3;
        }
        
        else if(zugStatus == zug_status.spielerUebergang){
            if(!spielerA)
                zugStatus = zug_status.spieler1Platform;
            else
                zugStatus = zug_status.spieler2Platform;
            geld_player1++;
            geld_player2++;
            spielerA = !spielerA;
            spielFeld.whalemovement(r.nextBoolean(), r.nextBoolean());
        }
        
        else if(zugStatus == zug_status.spieler2Platform){
            zugStatus = zug_status.spieler2Movement;
            spielFeld.addCoin();
            spielFeld.addMovableKrieger(currentlyMovingQue, true);
        }
        
        else if(zugStatus == zug_status.spieler2Movement){
            zugStatus = zug_status.spielerUebergang;
            currentlyMovingQue.clear();
            spielFeld.switchTurn(this,soundManager,death);
            counter = 3;
        }
    }
    
    public void whaleattack(){
        if(!spielerA){
            if(geld_player1 >= WHALE_ATTACK_KOSTEN){
                geld_player1-= WHALE_ATTACK_KOSTEN;
                spielFeld.whaleAttack();
                soundManager.playSound(whale);
            }
        }
        else{
            if(geld_player2 >= WHALE_ATTACK_KOSTEN){
                geld_player2-= WHALE_ATTACK_KOSTEN;
                spielFeld.whaleAttack();
                soundManager.playSound(whale);
            }
        }
        counter = 2;
    }
    
    public void movementBoost(){
        if( (zugStatus != zug_status.spieler1Movement && zugStatus != zug_status.spieler2Movement) || currentlyMovingQue.isEmpty())
            return;
        if(!spielerA){
            if(geld_player1 >= KOSTEN_MOVEMENT_BOOST){
                geld_player1-=KOSTEN_MOVEMENT_BOOST;
                currentlyMovingQue.peek().movementBoost();
            }
        }
        else{
            if(geld_player2 >= KOSTEN_MOVEMENT_BOOST){
                geld_player2-=KOSTEN_MOVEMENT_BOOST;
                currentlyMovingQue.peek().movementBoost();
            }
        }
    }

    @Override
    public void onMouseMove(int x, int y, int movex, int movey) {
        mouseX = x/(float)getResolutionX()*(float)getResolutionX()/(float)getResolutionY();
        mouseY = y/(float)getResolutionY();
    }

    @Override
    public float light() {
        return 1;
    }
    
    public void turnOver(){
        spielerA = !spielerA;
    }
    
    public void addCoinPlayer1(){
        soundManager.playSound(coin);
        geld_player1 += 10;
    }
    
    public void addCoinPlayer2(){
        soundManager.playSound(coin);
        geld_player2 += 10;
    }
    public void newWarrior(){
        if(!spielerA){
            if(geld_player1 >= KOSTEN_KRIEGER){
                geld_player1-=KOSTEN_KRIEGER;
                soundManager.playSound(newPlayer);
                Warrior w =new Warrior(spielFeld.gibPlatform(0, 0), !spielerA);
                spielFeld.gibPlatform(0, 0).addWarrior(w, this,soundManager,death);
                if(zugStatus != zug_status.spieler2Platform && zugStatus != zug_status.spieler1Platform )
                    w.addToMoveQue(currentlyMovingQue, !spielerA);
            }
        }
        else{
            if(geld_player2 >= KOSTEN_KRIEGER){
                geld_player2-=KOSTEN_KRIEGER;
                soundManager.playSound(newPlayer);
                Warrior w = new Warrior(spielFeld.gibPlatform(SpielFeld.SPIELFELDGROESSE-1, SpielFeld.SPIELFELDGROESSE-1), !spielerA);
                spielFeld.gibPlatform(SpielFeld.SPIELFELDGROESSE-1, SpielFeld.SPIELFELDGROESSE-1).addWarrior(w, this,soundManager,death);
                if(zugStatus != zug_status.spieler2Platform && zugStatus != zug_status.spieler1Platform )
                    w.addToMoveQue(currentlyMovingQue, !spielerA);
            }
        }
    }
    public void spieler1Gewinnt(){
        zugStatus = zug_status.summer_wins;
        summer1 = new Animation("assets/imgs/summer/1/attack", 30, 60, .5f, true, null);
        summer2 = new Animation("assets/imgs/summer/2/attack", 30, 60, .5f, true, null);
    }
    public void spieler2Gewinnt(){
        zugStatus = zug_status.winter_wins;
        winter1 = new Animation("assets/imgs/winter/1/attack", 30, 60, .5f, true, null);
        winter2 = new Animation("assets/imgs/winter/2/attack", 30, 60, .5f, true, null);
    }
}
