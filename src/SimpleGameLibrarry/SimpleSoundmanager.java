/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SimpleGameLibrarry;

import java.util.ArrayList;


public class SimpleSoundmanager {
    private float masterVolume;
    private boolean masterOn = true;
    private final float[] channelVolumes;
    private final boolean[] channelOn;
    private final ArrayList<Sound> sounds = new ArrayList<>();

    public SimpleSoundmanager(int chanels) {
        this.masterVolume = 0;
        channelVolumes = new float[chanels];
        channelOn = new boolean[chanels];
        for (int i = 0; i < chanels; i++) {
            channelVolumes[i] = 0;
            channelOn[i] = true;
        }
    }
    
    public void changeChanelVolume(int index, float value){
        channelVolumes[index] = value;
        for (Sound sound : sounds) {
            sound.volumeOnChange(channelVolumes, channelOn, masterVolume, masterOn);
        }
    }
    public void changeMasterVolume(float value){
        masterVolume = value;
        for (Sound sound : sounds) {
            sound.volumeOnChange(channelVolumes, channelOn, masterVolume, masterOn);
        }
    }
    public float getMasterVolume(){
        return masterVolume;
    }
    public float getChannelVolume(int index){
        return channelVolumes[index];
    }
    public boolean isMasterVolumeOn() {
        return masterOn;
    }
    public void setMasterVolumeOn(boolean masterOn) {
        this.masterOn = masterOn;
        for (Sound sound : sounds) {
            sound.volumeOnChange(channelVolumes, channelOn, masterVolume, masterOn);
        }
    }
    public boolean isChannelVolumeOn(int index){
        return channelOn[index];
    }
    public void setChannelVolumeOn(int index, boolean channelOn){
        this.channelOn[index] = channelOn;
        for (Sound sound : sounds) {
            sound.volumeOnChange(channelVolumes, this.channelOn, masterVolume, masterOn);
        }
    }
    /**
     * 
     * @param channel the channel to rgister the sound in.
     * @param name path of the soundfile
     * @param streamed true: don't preload the sound, false: preload the sound
     * @return the index of the sound
     */
    public int registerSound(int channel, String name, boolean streamed){
        if(channel >= this.channelVolumes.length)
            throw new IllegalArgumentException("channel must be small than the maxChannels!");
        int pos = sounds.size();
        sounds.add(new Sound(name, channel, streamed));
        return pos;
    }
    public void playSound(int index){
        if(index < 0 || index >= sounds.size())
            throw new IllegalArgumentException("no Sound for that index registered");
        sounds.get(index).play(channelVolumes, masterVolume, channelOn, masterOn);
    }
    public void stopSound(int index){
        if(index < 0 || index >= sounds.size())
            throw new IllegalArgumentException("no Sound for that index registered");
        sounds.get(index).stop();
    }
}
