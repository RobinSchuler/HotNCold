/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SimpleGameLibrarry;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

class Sound {
    private boolean streamed, playing = false, actuallyplaying = false;
    private Clip clip;
    private int channel;
    private String path;

    protected Sound(String path, int channel, boolean streamed) {
        this.streamed = streamed;
        this.channel = channel;
        if(streamed){
            this.path = path;
        }
        else{
            try {
                clip = getSoundClipFromURL(new File(path + ".wav").toURI().toURL());
            } catch (IOException ex) {
                Logger.getLogger(Sound.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    protected void play(float[] volumes, float master, boolean[] channelOn, boolean masterOn){
        playing = true;
        if(streamed){
            /*FileInputStream fin;
            AudioStream ain;
            try{
                fin = new FileInputStream(path);
                ain = new AudioStream(fin);
                AudioPlayer.player.start(ain);
            }
            catch (Exception ex) {
                Logger.getLogger(Sound.class.getName()).log(Level.SEVERE, null, ex);
            }*/
        }
        else{
            if(masterOn && channelOn[channel]){
                actuallyplaying = true;
                try {
                    FloatControl gainControl = 
                        (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    gainControl.setValue(master + volumes[channel]);
                } catch (Exception e) {
                    System.out.println("unpossible value!");
                }
                clip.setFramePosition(0);
                clip.start();
            }
        }
    }
    protected void stop(){
        playing = false;
        actuallyplaying = false;
        if(streamed){
            
        }
        else{
            clip.stop();
        }
    }
    protected void volumeOnChange(float[] channlVolumes, boolean[] channelOn,float masterVolume, boolean masterOn){
        if(playing){
            if(streamed){
                
            }
            else{
                if(!masterOn || !channelOn[channel]){
                    clip.stop();
                    actuallyplaying = false;
                }
                else{
                    if(!actuallyplaying){
                        clip.start();
                        actuallyplaying = true;
                    }
                    try {
                        FloatControl gainControl = 
                            (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                        gainControl.setValue(masterVolume + channlVolumes[channel]);
                    } catch (Exception e) {
                        System.out.println("unpossible value!");
                    }
                }
            }
        }
    }
    private Clip getSoundClipFromURL(java.net.URL url){
        Clip thisClip;
        AudioFormat aF;
        AudioInputStream aIS;
        try{
            aIS = AudioSystem.getAudioInputStream(url);
            aF = aIS.getFormat();  
        } catch (IOException e) {
            System.out.println(e.getMessage());
            aIS = null;
            aF = null;
        } catch (UnsupportedAudioFileException e){
            System.out.println("Format not supported: " + e.getMessage());
            aIS = null;
            aF = null;
        }
        DataLine.Info info = new DataLine.Info(Clip.class, aF);
        try{
            thisClip = (Clip)AudioSystem.getLine(info);
            thisClip.open(aIS);
        } catch(LineUnavailableException e){
            e.printStackTrace();
            thisClip = null;
        } catch(IOException e){
            e.printStackTrace();
            thisClip = null;
        }
        return thisClip;
    }
}
