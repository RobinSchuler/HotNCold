/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SimpleGameLibrarry;

import java.awt.image.BufferedImage;


public class SmoothAnimationSequence implements AnimationDoneListener{
    private volatile Animation curr;
    private volatile Animation next;
    private final AnimationDoneListener listener;

    /**
     * overrites he listener of the animations!
     */
    public SmoothAnimationSequence(Animation curr, Animation next, AnimationDoneListener listener) {
        this.curr = curr;
        curr.setListner(this);
        this.next = next;
        if(next != null)
            next.setListner(this);
        this.listener = listener;
    }
    
    protected BufferedImage draw(float time){
        return curr.draw(time);
    }
    
    @Override
    public void animationDone(Animation a) {
        if(next != null)
            curr = next;
        next = null;
        curr.reset();
        if(listener != null)
            listener.animationDone(a);
    }

    public void setNext(Animation next) {
        this.next = next;
        if(next != null)
            next.setListner(this);
    }
    
    public void forceChange(Animation next){
        if(this.curr != next){
            this.curr = next;
            this.curr.reset();
            this.curr.setListner(this);
            this.next = null;
        }
    }
    
}
