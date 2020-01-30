
package game2;

import SimpleGameLibrarry.Animation;
import SimpleGameLibrarry.SmoothAnimationSequence;
import java.util.concurrent.LinkedBlockingDeque;


public class reaperSnowman extends Warrior{

    public reaperSnowman(Platform p, boolean player) {
        super(p, player);
        idle = new Animation("assets/imgs/reaperSnowman/idle", 60, 90, 1, true, null);
        walk = new Animation("assets/imgs/reaperSnowman/walk", 90, 0, 1f, true, null);
        animationsSequenze = new SmoothAnimationSequence(idle, null, null);
    }

    @Override
    public void addToMoveQue(LinkedBlockingDeque<Warrior> w, boolean player1) {
    }

    @Override
    public void switchTurn(Game2 g) {
        dir = g.r.nextInt(5);
    }
    
    
    
}
