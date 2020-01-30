
package game2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Server extends Client{

    public Server(int port) {
        try {
            seed = (byte)(Math.random()*Byte.MAX_VALUE);
            ServerSocket socket = new ServerSocket(port);
            client = socket.accept();
            client.setKeepAlive(true);
            out = client.getOutputStream();
            in = client.getInputStream();
            out.write(seed);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean isSummer() {
        return false; //To change body of generated methods, choose Tools | Templates.
    }
    
}
