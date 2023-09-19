import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.*;
import java.util.ArrayList;
import java.util.Iterator;

/*
 * Liam Caffrey
 * 21378316
 *
 */

public class InvadersApplication extends JFrame implements Runnable, KeyListener {

    public static final Dimension WindowSize = new Dimension(800,600);
    private BufferStrategy strategy;
    private Graphics buffer;
    private Spaceship PlayerShip;
    private static final int NumAliens = 35;
    private Alien[] AliensArray = new Alien[NumAliens];

    private Image bulletImage;
    private ArrayList bulletsList = new ArrayList();
    private boolean isInitialised = false;
    private static String workingDirectory;
    private boolean isGameInProgress = false;
    private int enemyWave = 1;
    private int score = 0;
    private int highscore = 0;

    public InvadersApplication() {
        //Display the window, centred on the screen
        Dimension screensize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int x = screensize.width/2 - WindowSize.width/2;
        int y = screensize.height/2 - WindowSize.height/2;
        setBounds(x, y, WindowSize.width, WindowSize.height);
        setVisible(true);
        this.setTitle("Space Invaders!");

        // load alien images
        ImageIcon icon = new ImageIcon(workingDirectory + "\\alien_ship_1.png");
        Image alienImage = icon.getImage();
        icon = new ImageIcon(workingDirectory + "\\alien_ship_2.png");
        Image alienImage2 = icon.getImage();

        // create and initialise alien enemies
        for (int i = 0; i< NumAliens; i++) {
            AliensArray[i] = new Alien(alienImage,alienImage2);
        }

        // load image for player
        icon = new ImageIcon(workingDirectory + "\\player_ship.png");
        Image shipImage = icon.getImage();
        icon = new ImageIcon(workingDirectory + "\\bullet.png");
        bulletImage = icon.getImage();// load image for the bullet
        PlayerShip = new Spaceship(shipImage, bulletImage);

        // This is where we start our thread for animation
        Thread t = new Thread(this);
        t.start();

        // take inputs
        addKeyListener(this);

        // initialise double-buffering
        createBufferStrategy(2);
        strategy = getBufferStrategy();
        buffer = strategy.getDrawGraphics();
        isInitialised = true;
    }
    // thread's entry point
    public void run() {
        while ( 1==1 ) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) { }
            //check if game is running
            if ( isGameInProgress ) {
                boolean anyAliensAlive = false;
                boolean alienDirectionReversalNeeded = false;
                for (int i = 0; i< NumAliens; i++) {
                    if ( AliensArray[i].isAlive ) {
                        anyAliensAlive = true;
                        if (AliensArray[i].move())
                            alienDirectionReversalNeeded=true;

                        // check if any alien had hit the player
                        if ( isCollision(PlayerShip.x,AliensArray[i].x,PlayerShip.y,AliensArray[i].y,54,50,32,32) )
                        {
                            isGameInProgress=false;
                        }
                    }
                }

                if (alienDirectionReversalNeeded) {
                    for (int i = 0; i< NumAliens; i++) {
                        if ( AliensArray[i].isAlive ) {
                            AliensArray[i].reverseDirection(); // if passed off bottom of screen, game over
                            if ( AliensArray[i].y>WindowSize.height-20) {
                                isGameInProgress=false;
                            }
                        }
                    }
                }

                if (!anyAliensAlive) {
                    enemyWave++;
                    startNewWave();
                }

                PlayerShip.move();
                Iterator iterator = bulletsList.iterator();
                while(iterator.hasNext()){
                    PlayerBullet b = (PlayerBullet) iterator.next();
                    if (b.move()) {
                        iterator.remove(); //removes bullets
                    }

                    else {
                        double x2 = b.x, y2 = b.y; // check if bullet has hit an alien
                        double w1 = 50, h1 = 32;
                        double w2 = 6, h2 = 16;
                        for (int i = 0; i< NumAliens; i++) {
                            if ( AliensArray[i].isAlive ) {
                                double x1 = AliensArray[i].x;
                                double y1 = AliensArray[i].y;
                                if ( isCollision(x1,x2,y1,y2,w1,w2,h1,h2) ) { // destroy alien and bullet
                                    AliensArray[i].isAlive=false;
                                    iterator.remove(); // this is a safe way to remove from an ArrayList while iterating
                                    score+=10;
                                    if (score>highscore)
                                        highscore=score;
                                    break; // no need to keep checking aliens so break out of for loop
                                }
                            }
                        }
                    }
                }
            }

            this.repaint();
        }
    }

    //Event-Handler
    public void keyPressed(KeyEvent e) {
        if ( isGameInProgress ) {
            if (e.getKeyCode()==KeyEvent.VK_LEFT)
                PlayerShip.setXSpeed(-4); //move player
            else if (e.getKeyCode()==KeyEvent.VK_RIGHT)
                PlayerShip.setXSpeed(4);
            else if (e.getKeyCode()==KeyEvent.VK_SPACE)
                if(bulletsList.size() < 10) { //limit the number of bullets the player can shoot
                    bulletsList.add(PlayerShip.shootBullet());
                }
        }

        else {
            if (e.getKeyCode()==KeyEvent.VK_SPACE) {
                startNewGame(); //if space is pressed start a new game
            }
        }
    }

    public void keyReleased(KeyEvent e) {}

    public void keyTyped(KeyEvent e) {}

    public void startNewGame() { //here we have the method to start a new game
        enemyWave = 1;
        score = 0;
        isGameInProgress = true;
        PlayerShip.setPosition(350,550); //inital placement of player ship
        startNewWave();
    }

    public void startNewWave() {
        // deploy aliens
        for (int i = 0; i< NumAliens; i++) {
            double xx = (i%5)*80;
            double yy = (i/5)*40 + 70;
            AliensArray[i].setPosition(xx, yy);
            AliensArray[i].setXSpeed(1+enemyWave);
            AliensArray[i].isAlive=true;
            AliensArray[i].framesDrawn=0;
        }
    }
    // helper method for collision detection
    private boolean isCollision(double x1,double x2,double y1,double y2,double w1,double w2,double h1,double h2) {
        if (((x1<x2 && x1+w1>x2) || (x2<x1 && x2+w2>x1)) && ((y1<y2 && y1+h1>y2) || (y2<y1 && y2+h2>y1)))
            return true;
        else
            return false;
    }
    // helper method for drawing strings centred at specified position
    private void writeString(Graphics g, int x, int y, int fontSize, String message) {
        Font f = new Font( "Times", Font.PLAIN, fontSize );
        g.setFont(f);
        FontMetrics fm = getFontMetrics(f);
        int width = fm.stringWidth(message);
        g.drawString(message, x-width/2, y);
    }

    // application's paint method
    public void paint(Graphics g) {
        if (!isInitialised)
            return;
        g = buffer; // draw to offscreen buffer
        // set background and clear past objects
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, WindowSize.width, WindowSize.height);
        if ( isGameInProgress ) {
        // redraw all game objects
            for (int i = 0; i< NumAliens; i++)
                AliensArray[i].paint(g);
            PlayerShip.paint(g);
            Iterator iterator = bulletsList.iterator();
            while(iterator.hasNext()){
                PlayerBullet b = (PlayerBullet) iterator.next();
                b.paint(g);
            }
            // score
            g.setColor(Color.GREEN);
            g.fillRect(0,30,800,35);
            g.setColor(Color.WHITE);
            writeString(g,WindowSize.width/2,60,30,"Score: "+score+" Best: "+highscore);
        }
        else {
            // redraw the menu screen
            g.setColor(Color.WHITE);
            writeString(g,WindowSize.width/2,200,60,"GAME OVER");
            writeString(g,WindowSize.width/2,300,30,"Press space to Play");
            writeString(g,WindowSize.width/2,350,25,"[Arrow keys to move, space to fire]");
        }
        // flip the buffers
        strategy.show();
    }
    // application entry point
    public static void main(String[] args) {
        workingDirectory = System.getProperty("user.dir");
        System.out.println("Working Directory = " + workingDirectory);
        InvadersApplication w = new InvadersApplication();
    }
}