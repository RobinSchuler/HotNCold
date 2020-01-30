package SimpleGameLibrarry;




import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * Simple Game Librarry.
 * Use an Object of the SimpleSoundManager for Sounds.
 * Extend this class and call the start() method to start the Graphic and Sound engines
 * @see SimpleGameLibrarry.SimpleSoundmanager
 */
public abstract class SimpleGameLibrarry {
    
    private volatile boolean running = true,light,windowed, recordMode = false;
    private final Object runningMarker = new Object(), lightMarker = new Object();
    private volatile boolean shutdown = false;
    private static int resolutionX, resolutionY, scaleUp ;
    private int currfps, currtps, currlps, recordModeFrameCounter = 0;
    private float minPsf, minPst, passedRenderTime;
    private static BufferedImage drawboard, lightMap, lightMap2;
    private static JFrame frame;
    private DisplayMode oldDisp;
    private String recordPath;
    private LinkedBlockingDeque<BufferedImage> recodImageQue = new LinkedBlockingDeque<>();
    
    private final Thread renderer = new Thread(new Runnable() {

        @Override
        public void run() {
            long time = System.currentTimeMillis();
            while(!shutdown){
                long timenow = System.currentTimeMillis();
                long passedTime = timenow - time;
                time = timenow;
                if(passedTime < minPsf){
                    try {
                        Thread.sleep((int)minPsf - passedTime);
                    } catch (InterruptedException ex) {}
                    currfps = (int)(1_000f/minPsf);
                }
                else{
                    currfps = (int)(1_000f/passedTime);
                }
                passedRenderTime = passedTime/1_000f;
                drawboard = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
                onRender();
                if(light){
                    drawboard.getGraphics().drawImage(lightMap2, 0, 0, frame.getWidth(), frame.getHeight(), null);
                }
                onRenderGui();
                frame.getGraphics().drawImage(drawboard, 0, 0, null);
                if(recordMode){
                    recodImageQue.offer(drawboard);
                }
            }
        }
    });
    private final Thread lightrenderer = new Thread(new Runnable() {

        @Override
        public void run() {
            long time = System.currentTimeMillis();
            while(!shutdown){
                while(light){
                    long timenow = System.currentTimeMillis();
                    long passedTime = timenow - time;
                    time = timenow;
                    if(passedTime < minPsf){
                        try {
                            Thread.sleep((int)minPsf - passedTime);
                        } catch (InterruptedException ex) {}
                        currlps = (int)(1_000f/minPsf);
                    }
                    else{
                        currlps = (int)(1_000f/passedTime);
                    }
                    lightMap = new BufferedImage(resolutionX, resolutionY, BufferedImage.TYPE_INT_ARGB);
                    Graphics g = lightMap.getGraphics();
                    g.setColor(new Color(0, 0, 0, light()));
                    g.fillRect(0, 0, resolutionX, resolutionY);
                    onRenderLight();
                    lightMap2 = lightMap;
                }
                synchronized(lightMarker){
                    try {
                        lightMarker.wait();
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
    });
    private final Thread ticker = new Thread(new Runnable() {

        @Override
        public void run() {
            long time = System.currentTimeMillis();
            while(!shutdown){
                while (running) {      
                    long timenow = System.currentTimeMillis();
                    long passedTime = timenow - time;
                    time = timenow;
                    if(passedTime < minPst){
                        try {
                            Thread.sleep((int)minPst - passedTime);
                        } catch (InterruptedException ex) {}
                        currtps = (int)(1_000f/minPst);
                    }
                    else{
                        currtps = (int)(1_000f/passedTime);
                    }
                    onUpdate(passedTime/1_000f);
                }
                synchronized(runningMarker){
                    try {
                        runningMarker.wait();
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
    });

    /**
     * Does nothing.
     * Call the start() method to start the the Graphic and Sound engines.
     */
    public SimpleGameLibrarry() {
    }
    
    public void setIcon(String path){
        frame.setIconImage((new ImageIcon(path)).getImage());
    }
    
    /**
     * 
     * @param resolutionX the resolution
     * @param resolutionY the resolution
     * @param maxFps the maximum frames that should be rendered per second
     * @param maxTps the maximum ticks that should be made per second
     * @param title the title of the game
     * @param windowed toggles between fullscreen and Winwow mode
     * @param light do light ticks
     */
    public void start(int resolutionX, int resolutionY, int maxFps, int maxTps, String title, boolean windowed,boolean light) {
        this.windowed = windowed;
        setMaxFps(maxFps);
        setMaxTps(maxTps);
        lightMap2 = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        frame = new JFrame(title);
        int scale = resolutionX;
        if(resolutionY < scale)
            scale = resolutionY;
        setResolution(resolutionX, resolutionY, scale);

        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
            cursorImg, new Point(0, 0), "blank cursor");
        frame.getContentPane().setCursor(blankCursor);

        frame.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                onKey(e.getKeyCode(), true);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                onKey(e.getKeyCode(), false);
            }
        });
        frame.addMouseMotionListener(new MouseMotionListener() {
            int mousex = 0, mousey = 0;
            @Override
            public void mouseDragged(MouseEvent e) {
                onMouseMove(e.getX()*SimpleGameLibrarry.resolutionX/frame.getWidth(), e.getY()*SimpleGameLibrarry.resolutionY/frame.getHeight(), 
                        (e.getX()- mousex) *SimpleGameLibrarry.resolutionX/frame.getWidth(), (e.getY() - mousey) *SimpleGameLibrarry.resolutionY/frame.getHeight());
                mousex = e.getX();
                mousey = e.getY();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                onMouseMove(e.getX()*SimpleGameLibrarry.resolutionX/frame.getWidth(), e.getY()*SimpleGameLibrarry.resolutionY/frame.getHeight(), 
                        (e.getX()- mousex) *SimpleGameLibrarry.resolutionX/frame.getWidth(), (e.getY() - mousey) *SimpleGameLibrarry.resolutionY/frame.getHeight());
                mousex = e.getX();
                mousey = e.getY();
            }
        });
        frame.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
                onMouse(e.getButton(), true, e.getX()*SimpleGameLibrarry.resolutionX/frame.getWidth(), e.getY()*SimpleGameLibrarry.resolutionY/frame.getHeight());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                onMouse(e.getButton(), false, e.getX()*SimpleGameLibrarry.resolutionX/frame.getWidth(), e.getY()*SimpleGameLibrarry.resolutionY/frame.getHeight());
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
        frame.setVisible(true);
        changeLight(light);
        ticker.start();
        renderer.start();
        lightrenderer.start();
    }
    
    /**
     * switch the lightengine on or of
     * @param on true for on; false for off...
     */
    public final void changeLight(boolean on){
        if(on){
            synchronized(lightMarker){
                light = true;
                lightMarker.notifyAll();
            }
        }
        else{
            light = false;
        }
    }
    
    /**
     * changes the maxFps
     * @param maxFps the maximum frames that should be rendered per second
     */
    public final void setMaxFps(int maxFps) {
        this.minPsf = 1_000f/maxFps;
    }

    /**
     * changes the maxTps
     * @param maxTps the maximum ticks that should be made per second
     */
    public final void setMaxTps(int maxTps) {
        this.minPst = 1_000f/maxTps;
    }

    /**
     * changes the resolution
     * @param resolutionX the resolution
     * @param resolutionY the resolution
     */
    public final void setResolution(int resolutionX, int resolutionY, int scale) {
        this.resolutionX = resolutionX;
        this.resolutionY = resolutionY;
        this.scaleUp = scale;
        if(windowed){
            frame.setBounds(java.awt.Toolkit.getDefaultToolkit().getScreenSize().width*1/20, java.awt.Toolkit.getDefaultToolkit().getScreenSize().height*1/20, 
                    resolutionX, resolutionY);
            frame.setDefaultCloseOperation(3);
        }
        else{
            frame.setUndecorated(true);
            GraphicsDevice device = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice(); 
            try{
                device.setFullScreenWindow(frame);
                if (device.isDisplayChangeSupported()) {
                    oldDisp = device.getDisplayMode();
                    int changeto = -1;
                    int dist = -1;
                    for (int i = 0; i < device.getDisplayModes().length; i++) {
                        if(dist < 0 || (dist >= device.getDisplayModes()[i].getWidth()-resolutionX && device.getDisplayModes()[i].getBitDepth() >= 16)){
                            dist = device.getDisplayModes()[i].getWidth()-resolutionX;
                            changeto = i;
                        }
                    }
                    if(changeto != -1)
                        device.setDisplayMode(device.getDisplayModes()[changeto]);
                }
            }
            catch(Exception e){
                device.setDisplayMode(oldDisp);
                device.setFullScreenWindow(null);
            }
        }
    }
    
    /**
     * test if this resolution is possible. creates a window to do so... may freeze the screen for a while
     * @param resolutionX the resolution aimed for 
     * @param resolutionY the resolution aimed for 
     * @return a String telling what the closest possible resolution was. the String is formated like this: "resolutionX" x "resoltionY"
     */
    public static String getActualResolutionForInput(int resolutionX, int resolutionY){
        GraphicsDevice device = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice(); 
        JFrame f = new JFrame();
        f.setUndecorated(true);
        try{
            device.setFullScreenWindow(f);
            if (device.isDisplayChangeSupported()) {
                int changeto = -1;
                int dist = -1;
                for (int i = 0; i < device.getDisplayModes().length; i++) {
                    if(dist < 0 || (dist >= device.getDisplayModes()[i].getWidth()-resolutionX && device.getDisplayModes()[i].getBitDepth() >= 16)){
                        dist = device.getDisplayModes()[i].getWidth()-resolutionX;
                        changeto = i;
                    }
                }
                if(changeto != -1){
                    device.setFullScreenWindow(null);
                    f.dispose();
                    return device.getDisplayModes()[changeto].getWidth() + " / " + device.getDisplayModes()[changeto].getHeight();
                }
            }
        }
        catch(Exception e){
            device.setFullScreenWindow(null);
            f.dispose();
        }
        device.setFullScreenWindow(null);
        f.dispose();
        return "No fitting resolution found... ";
    }

    /**
     * pauses the game
     * rendering might still occour
     * does nothing when the game is already paused
     */
    public final void pause(){
        running = false;
    }
    
    /**
     * resumes the game
     * does nothing when the game is already running
     */
    public final void resume(){
        running = false;
        synchronized(runningMarker){
            runningMarker.notifyAll();
        }
    }
    
    /**
     * overrite this method!
     * do all your necessary drawing inside of this
     */
    public abstract void onRender();
    
    /**
     * overrite this method!
     * do all your shutdown logic here
     */
    public abstract void onShutdown();
    
    /**
     * overrite this method!
     * do all your necessary lighting inside of this
     */
    public abstract void onRenderLight();
    
    /**
     * overrite this method!
     * do all your necessary drawing for the GUI inside of this
     */
    public abstract void onRenderGui();
    
    /**
     * overrite this method!
     * do all your necessary game logic inside of this method.
     * @param passedTime the time that passed since the last onUpdate call
     */
    public abstract void onUpdate(float passedTime);
    
    /**
     * overrite this method!
     * this will be called when on pc and a key gets pressed or released
     * @param keyId the id of the key.
     * @param pressed if the key was pressed or released
     */
    public abstract void onKey(int keyId, boolean pressed);
    
    /**
     * overrite this method!
     * this will be called when the screen gets touched or any mouse button hit.
     * @param Id the id of the button or 0 if on android.
     * @param pressed if pressed or released
     * @param x the x location of the event
     * @param y the y location of the event
     */
    public abstract void onMouse(int Id, boolean pressed, int x, int y);
    
    /**
     * overrite this method!
     * @param x the new location of the mouse
     * @param y the new location of the mouse
     * @param movex the distance moved
     * @param movey the distance moved
     */
    public abstract void onMouseMove(int x, int y, int movex, int movey);
    
    /**
     * use this method during the onRender.
     * use this for HUD only!
     * 
     * @param i the image to be drawn
     * @param x the xcoord of the image
     * @param y the ycoord of the image
     */
    public final void drawImage(Image i, int x, int y){
        drawboard.getGraphics().drawImage(i, (int)(x*frame.getWidth()/resolutionX), (int)(y*frame.getHeight()/resolutionY),(int)(i.getWidth(null)*frame.getWidth()/resolutionX),(int)(i.getHeight(null)*frame.getHeight()/resolutionY), null);
    }
    
    /**
     * use this method during the onRender.
     * @param i the image to be drawn
     * @param x the xcoord of the image
     * @param y the ycoord of the image
     * @param width the width of the image
     * @param height the height of the image
     */
    public final void drawImage(Image i, float x, float y, float width, float height){
        drawboard.getGraphics().drawImage(i, (int)(x*scaleUp*frame.getWidth()/resolutionX), (int)(y*scaleUp*frame.getHeight()/resolutionY),(int)(width*scaleUp*frame.getWidth()/resolutionX),(int)(height*scaleUp*frame.getHeight()/resolutionY), null);
    }
    
    /**
     * use this method during the onRender.
     * @param i the image to be drawn
     * @param x the xcoord of the image
     * @param y the ycoord of the image
     * @param width the width of the image
     * @param height the height of the image
     * @param rotation  the angle of rotation in radians
     */
    public final void drawImage(Image i, float x, float y, float width, float height, float rotation){
        Graphics2D g = (Graphics2D)drawboard.getGraphics();
        g.rotate(rotation, (int)( (x+width/2)*scaleUp*frame.getWidth()/resolutionX), (int)( (y+height/2)*scaleUp*frame.getHeight()/resolutionY));
        g.drawImage(i, (int)(x*scaleUp*frame.getWidth()/resolutionX), (int)(y*scaleUp*frame.getHeight()/resolutionY),(int)(width*scaleUp*frame.getWidth()/resolutionX),(int)(height*scaleUp*frame.getHeight()/resolutionY), null);
    }
    
    /**
     * use this method during the onRender.
     * @param s the string to be drawn
     * @param x the xcoord of the string
     * @param y the ycoord of the string
     * @param fontName - the font name. This can be a font face name or a font family name, and may represent either a logical font or a physical font found in this GraphicsEnvironment. The family names for logical fonts are: Dialog, DialogInput, Monospaced, Serif, or SansSerif. Pre-defined String constants exist for all of these names, for example, DIALOG. If name is null, the logical font name of the new Font as returned by getName() is set to the name "Default". 
     * @param style - the style constant for the Font The style argument is an integer bitmask that may be PLAIN, or a bitwise union of BOLD and/or ITALIC (for example, ITALIC or BOLD|ITALIC). If the style argument does not conform to one of the expected integer bitmasks then the style is set to PLAIN. 
     * @param size - the point size of the Font
     */
    public final void drawString(String s, float x, float y, String fontName, int style, int size){
        Graphics g = drawboard.getGraphics();
        g.setFont(new Font(fontName, style, size));
        g.drawString(s, (int)(x*scaleUp*frame.getWidth()/resolutionX), (int)(y*scaleUp*frame.getHeight()/resolutionY));
    }
    
    /**
     * use this method during the onRender.
     * @param s the string to be drawn
     * @param x the xcoord of the string
     * @param y the ycoord of the string
     * @param fontName - the font name. This can be a font face name or a font family name, and may represent either a logical font or a physical font found in this GraphicsEnvironment. The family names for logical fonts are: Dialog, DialogInput, Monospaced, Serif, or SansSerif. Pre-defined String constants exist for all of these names, for example, DIALOG. If name is null, the logical font name of the new Font as returned by getName() is set to the name "Default". 
     * @param style - the style constant for the Font The style argument is an integer bitmask that may be PLAIN, or a bitwise union of BOLD and/or ITALIC (for example, ITALIC or BOLD|ITALIC). If the style argument does not conform to one of the expected integer bitmasks then the style is set to PLAIN. 
     * @param size - the point size of the Font
     * @param c the color of the string
     */
    public final void drawString(String s, float x, float y, String fontName, int style, int size, Color c){
        Graphics g = drawboard.getGraphics();
        g.setColor(c);
        g.setFont(new Font(fontName, style, size));
        g.drawString(s, (int)(x*scaleUp*frame.getWidth()/resolutionX), (int)(y*scaleUp*frame.getHeight()/resolutionY));
    }
    
    /**
     * use this method during the onRender.
     * @param s the string to be drawn
     * @param x the xcoord of the string
     * @param y the ycoord of the string
     */
    public final void drawString(String s, float x, float y){
        drawboard.getGraphics().drawString(s, (int)(x*scaleUp*frame.getWidth()/resolutionX), (int)(y*scaleUp*frame.getHeight()/resolutionY));
    }
    
    /**
     * use this method during the onRender.
     * @param s the string to be drawn
     * @param x the xcoord of the string
     * @param y the ycoord of the string
     * @param c the color of the String
     */
    public final void drawString(String s, float x, float y, Color c){
        Graphics g = drawboard.getGraphics();
        g.setColor(c);
        g.drawString(s, (int)(x*scaleUp*frame.getWidth()/resolutionX), (int)(y*scaleUp*frame.getHeight()/resolutionY));
    }
    
    /**
     * use this method during the onRender.
     * @param x the xcoord of the string
     * @param y the ycoord of the string
     * @param widht the width of the rect
     * @param height the hight of the rect
     * @param color the color of the rect
     */
    public final void drawRect(float x, float y, float widht, float height, Color color){
        Graphics g = drawboard.getGraphics();
        g.setColor(color);
        g.fillRect((int)(x*scaleUp*frame.getWidth()/resolutionX), (int)(y*scaleUp*frame.getHeight()/resolutionY), (int)(widht*scaleUp*frame.getWidth()/resolutionX), (int)(height*scaleUp*frame.getHeight()/resolutionY));
    }
    
    /**
     * use this method during the onRender.
     * @param x the xcoord of the string
     * @param y the ycoord of the string
     * @param widht the width of the rect
     * @param height the hight of the rect
     * @param color the color of the rect
     * @param rotation  the angle of rotation in radians
     */
    public final void drawRect(float x, float y, float widht, float height, Color color, float rotation){
        Graphics2D g = (Graphics2D)drawboard.getGraphics();
        g.setColor(color);
        g.rotate(rotation, (int)( (x+widht/2)*scaleUp*frame.getWidth()/resolutionX), (int)( (y+height/2)*scaleUp*frame.getHeight()/resolutionY));
        g.fillRect((int)(x*scaleUp*frame.getWidth()/resolutionX), (int)(y*scaleUp*frame.getHeight()/resolutionY), (int)(widht*scaleUp*frame.getWidth()/resolutionX), (int)(height*scaleUp*frame.getHeight()/resolutionY));
    }
    
    /**
     * use this method during the onRender.
     * @param x the xcoord of the string
     * @param y the ycoord of the string
     * @param widht the width of the rect
     * @param height the hight of the rect
     * @param animation the animation to be drawn
     */
    public final void drawAnimation(float x, float y, float widht, float height, Animation animation){
        drawboard.getGraphics().drawImage(animation.draw(passedRenderTime), (int)(x*scaleUp*frame.getWidth()/resolutionX), (int)(y*scaleUp*frame.getHeight()/resolutionY),(int)(widht*scaleUp*frame.getWidth()/resolutionX),(int)(height*scaleUp*frame.getHeight()/resolutionY), null);
    }
    
    /**
     * use this method during the onRender.
     * @param x the xcoord of the string
     * @param y the ycoord of the string
     * @param widht the width of the rect
     * @param height the hight of the rect
     * @param animation the animation to be drawn
     * @param rotation the angle of rotation in radians
     */
    public final void drawAnimation(float x, float y, float widht, float height, Animation animation, float rotation){
        Graphics2D g = (Graphics2D)drawboard.getGraphics();
        g.rotate(rotation, (int)( (x+widht/2)*scaleUp*frame.getWidth()/resolutionX), (int)( (y+height/2)*scaleUp*frame.getHeight()/resolutionY));
        g.drawImage(animation.draw(passedRenderTime), (int)(x*scaleUp*frame.getWidth()/resolutionX), (int)(y*scaleUp*frame.getHeight()/resolutionY),(int)(widht*scaleUp*frame.getWidth()/resolutionX),(int)(height*scaleUp*frame.getHeight()/resolutionY), null);
    }
    
    /**
     * use this method during the onRender.
     * @param x the xcoord of the string
     * @param y the ycoord of the string
     * @param widht the width of the rect
     * @param height the hight of the rect
     * @param animation the animation sequence to be drawn
     */
    public final void drawSmoothAnimationSequence(float x, float y, float widht, float height, SmoothAnimationSequence animation){
        drawboard.getGraphics().drawImage(animation.draw(passedRenderTime), (int)(x*scaleUp*frame.getWidth()/resolutionX), (int)(y*scaleUp*frame.getHeight()/resolutionY),(int)(widht*scaleUp*frame.getWidth()/resolutionX),(int)(height*scaleUp*frame.getHeight()/resolutionY), null);
    }
    
    /**
     * use this method during the onRender.
     * @param x the xcoord of the string
     * @param y the ycoord of the string
     * @param widht the width of the rect
     * @param height the hight of the rect
     * @param animation the animation sequence to be drawn
     * @param rotation  the angle of rotation in radians
     */
    public final void drawSmoothAnimationSequence(float x, float y, float widht, float height, SmoothAnimationSequence animation, float rotation){
        Graphics2D g = (Graphics2D)drawboard.getGraphics();
        g.rotate(rotation, (int)( (x+widht/2)*scaleUp*frame.getWidth()/resolutionX), (int)( (y+height/2)*scaleUp*frame.getHeight()/resolutionY));
        g.drawImage(animation.draw(passedRenderTime), (int)(x*scaleUp*frame.getWidth()/resolutionX), (int)(y*scaleUp*frame.getHeight()/resolutionY),(int)(widht*scaleUp*frame.getWidth()/resolutionX),(int)(height*scaleUp*frame.getHeight()/resolutionY), null);
    }
    
    /**
     * use this method during the onRender.
     * @param x the xcoord of the string
     * @param y the ycoord of the string
     * @param widht the width of the rect
     * @param height the hight of the rect
     * @param color the color of the rect
     */
    public final void drawRectOutline(float x, float y, float widht, float height, Color color){
        Graphics g = drawboard.getGraphics();
        g.setColor(color);
        g.drawRect((int)(x*scaleUp*frame.getWidth()/resolutionX), (int)(y*scaleUp*frame.getHeight()/resolutionY), (int)(widht*scaleUp*frame.getWidth()/resolutionX), (int)(height*scaleUp*frame.getHeight()/resolutionY));
    }
    
    /**
     * use this method during the onRender.
     * @param x the xcoord of the string
     * @param y the ycoord of the string
     * @param widht the width of the rect
     * @param height the hight of the rect
     * @param color the color of the rect
     * @param rotation  the angle of rotation in radians
     */
    public final void drawRectOutline(float x, float y, float widht, float height, Color color, float rotation){
        Graphics2D g = (Graphics2D)drawboard.getGraphics();
        g.rotate(rotation, (int)( (x+widht/2)*scaleUp*frame.getWidth()/resolutionX), (int)( (y+height/2)*scaleUp*frame.getHeight()/resolutionY));
        g.setColor(color);
        g.drawRect((int)(x*scaleUp*frame.getWidth()/resolutionX), (int)(y*scaleUp*frame.getHeight()/resolutionY), (int)(widht*scaleUp*frame.getWidth()/resolutionX), (int)(height*scaleUp*frame.getHeight()/resolutionY));
    }
    
    /**
     * use this method during the onRender.
     * @param x the xcoord of the string
     * @param y the ycoord of the string
     * @param widht the width of the circle
     * @param height the hight of the circle
     * @param color the color of the circle
     */
    public final void drawCircle(float x, float y, float widht, float height, Color color){
        Graphics g = drawboard.getGraphics();
        g.setColor(color);
        g.fillOval((int)(x*scaleUp*frame.getWidth()/resolutionX), (int)(y*scaleUp*frame.getHeight()/resolutionY), (int)(widht*scaleUp*frame.getWidth()/resolutionX), (int)(height*scaleUp*frame.getHeight()/resolutionY));
    }
    
    /**
     * use this method during the onRender.
     * @param x the xcoord of the string
     * @param y the ycoord of the string
     * @param widht the width of the circle
     * @param height the hight of the circle
     * @param color the color of the circle
     */
    public final void drawCircleOutline(float x, float y, float widht, float height, Color color){
        Graphics g = drawboard.getGraphics();
        g.setColor(color);
        g.drawOval((int)(x*scaleUp*frame.getWidth()/resolutionX), (int)(y*scaleUp*frame.getHeight()/resolutionY), (int)(widht*scaleUp*frame.getWidth()/resolutionX), (int)(height*scaleUp*frame.getHeight()/resolutionY));
    }
    
    /**
     * use this method during the onRenderLight.
     * @param x the xcoord of the string
     * @param y the ycoord of the string
     * @param rot the color of the circle min 0 max 1
     * @param gruen the color of the circle min 0 max 1
     * @param blau the color of the circle min 0 max 1
     * @param radius the radius of the circle
     * @param maxstaerke the color brightness of the circle min 0 max 255
     */
    public final void putLightSource(float x, float y, float rot, float gruen, float blau, float radius, int maxstaerke){
        x *= scaleUp;
        y *= scaleUp;
        radius *= scaleUp;
        if(!light)
            return;
        for (int i = (int)-radius; i < (int)+radius; i++) {
            int startEndy = (int)(Math.sqrt( radius*radius - i*i  ));
            int posX = (int)(x)+i;
            if(posX < 0 || posX >= resolutionX)
                continue;
            for (int j = -startEndy; j < startEndy; j++) {
                int posY = (int)(y)+j;
                if(posY < 0 || posY >= resolutionY)
                    continue;
                float dist = (float)(Math.sqrt(i*i + j*j));
                Color c = new Color(lightMap.getRGB(posX, posY),true); 
                int r = (int)(c.getRed() + rot * (1-dist/radius) * 255) ;
                int g = (int)(c.getGreen() + gruen * (1-dist/radius) * 255) ;
                int b = (int)(c.getBlue() + blau * (1-dist/radius) * 255) ;
                int a = (int)(c.getAlpha() - (1-dist/radius) * maxstaerke) ;
                if(a < 10)
                    a = 10;
                if(a > 245)
                    a = 245;
                if(r < 0)
                    r = 0;
                if(r > 255)
                    r = 255;
                if(g < 0)
                    g = 0;
                if(g > 255)
                    g = 255;
                if(b < 0)
                    b = 0;
                if(b > 255)
                    b = 255;
                lightMap.setRGB(posX, posY, new Color(r, g, b, a).getRGB());
            }
        }
    }
    
    /**
     * get the curret Fps
     * @return current Frames per second
     */
    public final int getCurrFps() {
        return currfps;
    }
    
    /**
     * get the current Tps
     * @return current Ticks per second
     */
    public final int getCurrTps() {
        return currtps;
    }
    
    /**
     * get the current Lps
     * @return current Ticks per second
     */
    public final int getCurrLps() {
        return currlps;
    }
    
    /**
     * call this to close the game
     */
    public final void shutdown(){
        new Thread(new Runnable() {

            @Override
            public void run() {
                frame.setVisible(false);
                onShutdown();
                running = false;
                shutdown = true;
                light = false;
                if(recordMode){
                    recordMode = false;
                    try {
                        recorder.join();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(SimpleGameLibrarry.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if(!windowed){
                    try {
                        java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setDisplayMode(oldDisp);
                    } catch (Exception exception) {
                    }
                }
                try {
                    renderer.join();
                    while (ticker.isAlive()) {                
                        synchronized(runningMarker){
                            runningMarker.notifyAll();
                        }
                    }
                    while (lightrenderer.isAlive()) {                
                        synchronized(lightMarker){
                            lightMarker.notifyAll();
                        }
                    }
                } catch (InterruptedException ex) {
                }
                finally{
                    System.exit(0);
                }
            }
        }).start();
    }
    
    /**
     * returns not the actual resolution just the value you set it to
     * @return the resolution X
     */
    public int getResolutionX() {
        return resolutionX;
    }

    /**
     * returns not the actual resolution just the value you set it to
     * @return the resolution Y
     */
    public int getResolutionY() {
        return resolutionY;
    }
    
    /**
     * @return resolution X / resolution Y
     */
    public float getResolutionFactor() {
        return resolutionX/(float)resolutionY;
    }
    
    private final Thread recorder = new Thread(new Runnable() {

        @Override
        public void run() {
            while (recordMode) {                
                while (!recodImageQue.isEmpty()) {      
                    try {
                        String suffix = "";
                        int counter = recordModeFrameCounter++;
                        for (int i = 0; i < 6; i++) {
                            suffix = counter%10 + suffix;
                            counter/=10;
                        }
                        ImageIO.write(recodImageQue.pop(), "png", new File(recordPath + "." + suffix + ".png"));
                    } catch (IOException ex) {
                        Logger.getLogger(SimpleGameLibrarry.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    });
    
    /**
     * starts to save all rendered frames
     * @param path the folder to save the frames into
     */
    public void recordSection(String path){
        recordModeFrameCounter = 0;
        this.recordPath = path;
        recordMode = true;
        recorder.start();
    }
    
    /**
     * stops saving all rendered frames
     * does nothing when recordSection(String path) wasn't called before
     */
    public void stopRecording(){
        recordMode = false;
        try {
            recorder.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(SimpleGameLibrarry.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * keep the value between 0.1 and 0.9 for best results...
     * @return the ambience light
     */
    public abstract float light();
    
}
