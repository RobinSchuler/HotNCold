package SimpleGameLibrarry;


import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

public class Animation {
    private final int amountFrames, indexImgs;
    private int currFrame = 0;
    private float currTime = 0;
    private final float lengthPerFrame;
    private static final ArrayList<BufferedImage[]> images = new ArrayList<>();
    private static final ArrayList<String> strings = new ArrayList<>();
    private AnimationDoneListener listner;
    private boolean loop;
    private boolean running = true;

    public Animation(String name, int amountFrames, int startCountingAt, float length, boolean loop, AnimationDoneListener listener) {
        this.listner = listener;
        this.amountFrames = amountFrames;
        this.loop = loop;
        this.lengthPerFrame = length / amountFrames;
        int j = 0;
        synchronized(images){
            for (; j < strings.size(); j++) {
                if(strings.get(j).equals(name)){
                    indexImgs = j;
                    return;
                }
            }
            strings.add(name);
            images.add(new BufferedImage[amountFrames]);
            for (int i = 0; i < amountFrames; i++) {
                try {
                    images.get(j)[i] = ImageIO.read(new File(name + "." + ( (i+startCountingAt)/1000)%10 + "" + ( (i+startCountingAt)/100)%10 + "" + ( (i+startCountingAt)/10)%10 + "" + (i+startCountingAt)%10 + ".png"));
                } catch (IOException ex) {
                    Logger.getLogger(Animation.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            indexImgs = j;
        }
    } 
    
    public Animation setTime(float time){
        currTime = time;
        currFrame = (int)(currTime / lengthPerFrame);
        currTime %= lengthPerFrame;
        if(currFrame >= amountFrames){
            if(loop)
                currFrame %= amountFrames;
            else{
                running = false;
                currFrame = amountFrames -1;
            }
            if(listner != null)
                listner.animationDone(this);
        }
        return this;
    }
        
    protected BufferedImage draw(float time){
        if(running){
            currTime += time;
            currFrame += (int)(currTime / lengthPerFrame);
            currTime %= lengthPerFrame;
            if(currFrame >= amountFrames){
                if(loop)
                    currFrame %= amountFrames;
                else{
                    running = false;
                    currFrame = amountFrames -1;
                }
                if(listner != null)
                    listner.animationDone(this);
            }
        }
        synchronized(images){
            return images.get(indexImgs)[currFrame];
        }
    }
    
    public void run(){
        running = true;
    }
    
    public void stop(){
        running = false;
    }
    
    public void reset(){
        setTime(0);
        run();
    }

    protected void setListner(AnimationDoneListener listner) {
        this.listner = listner;
    }

    protected void setLoop(boolean loop) {
        this.loop = loop;
    }
}
