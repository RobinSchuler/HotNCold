
package  game2;


public class LocalMultiplayer extends Client{

    public LocalMultiplayer() {
            seed = (byte)(Math.random()*Byte.MAX_VALUE);
    }
    

    @Override
    public boolean lanMultiplayer() {
        return false; //To change body of generated methods, choose Tools | Templates.
    }
    
}
