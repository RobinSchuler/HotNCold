
package game2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.DocFlavor;


public class Client {
    Socket client;
    OutputStream out;
    InputStream in;
    byte seed;

    public Client() {
    }
    
    public boolean lanMultiplayer(){
        return true;
    }

    public Client(int port, String ip) {
        try {
            client = new Socket(ip, port);
            client.setKeepAlive(true);
            out = client.getOutputStream();
            in = client.getInputStream();
            seed = (byte)in.read();
            
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendMouseKlick(float x, float y){
        try {
            byte[] bout = ByteBuffer.allocate(8).putFloat(x).putFloat(y).array();
            out.write(bout);
            System.out.println("click:" + x);
            System.out.println("click:" + y);
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public boolean isMouseAvailiable(){
        try {
            return in.available() > 0;
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
    
    public float waitForMouse(){
        try {
            byte[] bytes = new byte[4];
            for (int i = 0; i < 4; i++) {
                bytes[i] = (byte)in.read();
            }
            float f = ByteBuffer.wrap(bytes).getFloat();
            System.out.println("click:" + f);
            return f;
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }
    
    public boolean isSummer(){
        return true;
    }

    public long getSeed() {
        return seed;
    }
    
    
    
}
