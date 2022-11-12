/******************************************************************************************
 * Name:        Game
 * Author:      Frederick Wang and Kyssen Yu
 * Date:        Mar 7, 2021
 * Purpose:     2D space themed shooting game
 ******************************************************************************************/
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Game extends Canvas {

    private static final long serialVersionUID = 1L;
    private BufferStrategy strategy; // take advantage of accelerated graphics
    private boolean waitingForSpacePress = true; // true if game held up until
    private boolean waitingForMousePress = true; // true if game held up until
    // a key is pressed
    private boolean leftPressed = false; // true if a key currently pressed
    private boolean rightPressed = false; // true if d key currently pressed
    private boolean upPressed = false; // true if w key currently pressed
    private boolean downPressed = false; // true if s key currently pressed
    private boolean bombPressed = false; // true if right mouse currently pressed
    private boolean mousePressed = false; // true if left mouse currently pressed

    private boolean gameWon = false; //true if game won
    private boolean gameRunning = true; //true if game is running

    //storage for all images used in the game
    private static BufferedImage img = null;
    private static BufferedImage heart = null;
    private static BufferedImage bombPic = null;
    private static BufferedImage coin = null;
    private static BufferedImage banner = null;
    private static BufferedImage upgradeRoom = null;
    private static BufferedImage explosion = null;

    //counter variables
    private int shipHealth = 4;
    protected int bossHealth = 0;
    private int shipHealthMax = 4;
    private int level = 1;
    private int bombNum = 2;
    private int bombLevel = 1;
    private int coins = 0;
    private int speedLevel = 1;
    private int healthLevel = 1;
    private int shotLevel = 1;

    private ArrayList < Entity > entities = new ArrayList < Entity > (); // list of entities
    private ArrayList < Entity > removeEntities = new ArrayList < Entity > (); // list of entities

    private Entity ship; // the ship
    private Entity boss; // the boss

    private double moveSpeed = 300; // movement speed in each direction
    private long lastFire = 0; // time last shot fired
    private long firingInterval = 1000; // interval between shots
    protected int alienCount; // # of aliens left on screen

    /*
     * constructs game and set it running
     */
    public Game() {
        // create a frame to contain game
        JFrame container = new JFrame("Space Defenders");

        // get hold the content of the frame
        JPanel panel = (JPanel) container.getContentPane();

        // set up the resolution of the game
        panel.setPreferredSize(new Dimension(2000, 1000));
        panel.setLayout(null);

        // set up canvas size (this) and add to frame
        setBounds(0, 0, 2000, 1000);
        panel.add(this);

        // Tell AWT not to bother repainting canvas since that will
        // be done using graphics acceleration
        setIgnoreRepaint(true);

        // make the window visible
        container.pack();
        container.setResizable(false);
        container.setVisible(true);

        // if user closes window, shutdown game and jre
        container.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            } // windowClosing
        });

        // add key listener to this canvas
        addKeyListener(new KeyInputHandler());
        addMouseListener(new MouseInputHandler());

        // request focus so key events are handled by this canvas
        requestFocus();

        // create buffer strategy to take advantage of accelerated graphics
        createBufferStrategy(2);
        strategy = getBufferStrategy();

        // initialize entities
        initEntities();

        // start the game
        gameLoop();
    } // constructor

    /*
     * initialized all the entities required for the game
     */
    private void initEntities() {
        Entity alien;
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();

        // create the ship and put in center of screen
        if (level == 2) {
            ship = new ShipEntity(this, "sprites/level2.png", 850, 500, "ship");
        } else if (level == 3) {
            ship = new ShipEntity(this, "sprites/level3.png", 850, 500, "ship");
        } else {
            ship = new ShipEntity(this, "sprites/resize.png", 850, 500, "ship");
        }
        entities.add(ship);

        // create bosses on levels 2 and 3
        alienCount = 0;
        if (level == 2 || level == 3) {
            boss = new BossAlienEntity(this, "sprites/boss.png", 2000, 250, "boss", g);
            alienCount++;
            bossHealth = 5;
            entities.add(boss);
        } //if

        //creates the kamikaze entities
        for (int i = 0; i < 2 * level; i++) {
            if (i % 2 == 0) {
                alien = new KamikazeAlienEntity(this, "sprites/kamikaze.png", (int)(Math.random() * 2000),
                    (int)(Math.random() * -1000), "kamikaze", g);
            } else {
                alien = new KamikazeAlienEntity(this, "sprites/kamikaze.png", (int)(Math.random() * 2000),
                    (int)(Math.random() * -1000), "kamikaze", g);
            } //else
            entities.add(alien);
            alienCount++;
        } //for

        //creates the shooting alien entities
        for (int i = 0; i < 10 + 5 * (level - 1); i++) {
            if (i % 2 == 0) {
                alien = new AlienEntity(this, "sprites/aliensprite.png", (int)(Math.random() * 2000),
                    (int)(Math.random() * -1000), "alien", g);
            } else {
                alien = new AlienEntity(this, "sprites/aliensprite.png", (int)(Math.random() * 2000),
                    (int)((Math.random() * 1000) + 1000), "alien", g);
            } //else
            entities.add(alien);
            alienCount++;
        } //for
    } // initEntities

    /*
     * Remove an entity from the game. It will no longer be moved or drawn.
     */
    public void removeEntity(Entity entity) {
        removeEntities.add(entity);
    } // removeEntity

    /*
     * Remove player ship from the game. It will no longer be moved or drawn.
     */
    public void removeShip(Entity entity) {
        if (entity.equals(ship) && shipHealth < 1) {
            removeEntities.add(entity);
        } //if
    } // removeEntity

    /*
     * Notification that the player has died.
     */
    public void notifyDeath() {
        shipHealth--;
        if (shipHealth < 1) {
            gameWon = false;
            waitingForSpacePress = true;
            waitingForMousePress = true;
            bossHealth = 0;
            shipHealthMax = 4;
            moveSpeed = 300;
            firingInterval = 1000;
            bombLevel = 1;
            level = 1;
        } //if
    } // notifyDeath

    /*
     * Notification that the play has killed all aliens on each level
     */
    public void notifyWin() {
        waitingForSpacePress = true;
        waitingForMousePress = true;
        level++;
    } // notifyWin

    /*
     * Notification that the play has beat the game
     */
    public void notifyGameWin() {
        gameWon = true;
        waitingForSpacePress = true;
        waitingForMousePress = true;
        bossHealth = 0;
        shipHealthMax = 4;
        moveSpeed = 300;
        firingInterval = 1000;
        bombLevel = 1;
        level = 1;
    } //notifyGameWin

    /*
     * Notification than an alien has been killed
     */
    public void notifyAlienKilled() {
        alienCount--;
        coins += 100;
        if (alienCount <= 0) {
            notifyWin();
        } // if
    } // notifyAlienKilled

    /*
     * player try to fire
     */
    public void tryToFire() {
        // check that we've waited long enough to fire
        if ((System.currentTimeMillis() - lastFire) < firingInterval) {
            return;
        } // if
        // add a shot
        lastFire = System.currentTimeMillis();
        ShotEntity shot = new ShotEntity(this, "sprites/shipshot.png", ship.getX() + 50, ship.getY() + 50, "shot");
        entities.add(shot);
    } // tryToFire

    /*
     * upgrades player bombs
     */
    public void upgradeBomb() {
        if (coins >= 500 + (bombLevel - 1) * 150) {
            bombNum++;
            coins -= 500 + (bombLevel - 1) * 150;
            bombLevel++;
        } //if
    } //upgradeBomb

    /*
     * upgrades player's ship speed
     */
    public void upgradeShip() {
        if (coins >= 500 + (speedLevel - 1) * 150) {
            moveSpeed += 100;
            coins -= 500 + (speedLevel - 1) * 150;
            speedLevel++;
        } //if
    } //upgradeShip

    /*
     * upgrades player ship health
     */
    public void upgradeHealth() {
        if (coins >= 500 + (healthLevel - 1) * 150) {
            shipHealthMax += 1;
            coins -= 500 + (healthLevel - 1) * 150;
            healthLevel++;
        } //if
    } //upgradeHealth

    /*
     * upgrades player shot interval
     */
    public void upgradeShot() {
        if (coins >= 500 + (shotLevel - 1) * 150) {
            firingInterval -= 200;
            coins -= 500 + (shotLevel - 1) * 150;
            shotLevel++;
        } //if
    } //upgradeShot

    /*
     * player try to bomb
     */
    public void tryToBomb() {
        // check that we've waited long enough to bomb
        if ((System.currentTimeMillis() - lastFire) < firingInterval || bombNum < 1) {
            return;
        } // if

        // add a bomb
        lastFire = System.currentTimeMillis();
        BombEntity shot = new BombEntity(this, "sprites/bomb.png", ship.getX() + 10, ship.getY() - 30, "shot");
        entities.add(shot);
        bombNum--;
    } // tryToBomb

    /*
     * gameloop runs the game frame by frame until game ends
     */
    public void gameLoop() {
        long lastLoopTime = System.currentTimeMillis();

        // keep loop running until game ends
        while (gameRunning) {
            long delta = System.currentTimeMillis() - lastLoopTime;
            lastLoopTime = System.currentTimeMillis();

            // get graphics context for the accelerated surface
            Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
            //creates the screen for game
            g.drawImage(img, 0, 0, 2000, 1000, null);
            g.setColor(Color.white);
            g.setFont(new Font("Georgia", Font.PLAIN, 30));
            g.drawString(String.valueOf(coins), 1800, 100);
            g.drawString("Level " + level, 50, 100);
            for (int i = 0; i < shipHealth; i++) {
                g.drawImage(heart, 20 + 50 * i, 950, 50, 45, null);
            } //for
            for (int i = 0; i < bombNum; i++) {
                g.drawImage(bombPic, 1800 - 30 * i, 900, 70, 94, null);
            } //for
            g.drawImage(coin, 1750, 60, 48, 60, null);
            if (level == 2 || level == 3) {
                if (bossHealth > 0) {
                    g.setColor(Color.white);
                    g.fillRect(1700, 200, 150, 25);
                    g.setColor(Color.green);
                    g.fillRect(1700, 200, bossHealth * 30, 25);
                } //if
            } //if

            // makes each alien shoot
            if (!waitingForMousePress || !waitingForSpacePress) {
                for (int i = 0; i < entities.size(); i++) {
                    Entity entity = (Entity) entities.get(i);
                    if (((Entity) entities.get(i)).getType().equals("alien")) {
                        AlienShotEntity s = ((AlienEntity) entities.get(i)).tryFire();
                        if (s.getType().equals("shot")) {
                            entities.add(s);
                        } //if
                    } //if
                    if (((Entity) entities.get(i)).getType().equals("boss")) {
                        AlienShotEntity s = ((BossAlienEntity) entities.get(i)).tryFire();
                        if (s.getType().equals("shot")) {
                            entities.add(s);
                        } //if
                    } //if
                    entity.move(delta);
                    entity.draw(g);
                } // for
            } // if

            /*
             * checks collisions with every entity
             */
            for (int i = 0; i < entities.size(); i++) {
                for (int j = i + 1; j < entities.size(); j++) {
                    Entity me = (Entity) entities.get(i);
                    Entity him = (Entity) entities.get(j);

                    if (me.collidesWith(him)) {
                        me.collidedWith(him);
                        him.collidedWith(me);
                    } // if
                } // for
            } // for

            // remove dead entities
            entities.removeAll(removeEntities);
            removeEntities.clear();

            // prints screen in between levels
            if (waitingForMousePress || waitingForSpacePress) {
                g.setColor(Color.black);
                g.fillRect(0, 0, 2000, 1000);

                //lose screen
                if (shipHealth < 1) {
                    g.drawImage(banner, 0, 0, 2000, 500, null);
                    g.setColor(Color.red);
                    g.setFont(new Font("Georgia", Font.PLAIN, 100));
                    g.drawString("DEFEAT! Play Again?", 500, 750);
                    g.setFont(new Font("Georgia", Font.PLAIN, 30));
                    g.setColor(Color.white);
                    g.drawString("Press Space to PLAY", 825, 950);
                } //if

                //win screen
                else if (level == 1) {
                    g.drawImage(banner, 0, 0, 2000, 500, null);
                    if (gameWon) {
                        g.setColor(Color.blue);
                        g.setFont(new Font("Georgia", Font.PLAIN, 60));
                        g.drawString("CONGRATULATIONS!", 650, 650);
                        g.drawString("YOU WIN", 830, 750);
                    } //if
                    g.setColor(Color.white);
                    g.setFont(new Font("Georgia", Font.PLAIN, 30));
                    g.drawString("Press Space to PLAY", 825, 950);
                } // else if

                // upgrade shop screen
                else if (level != 4) {
                    g.drawImage(upgradeRoom, 0, 0, 2000, 1000, null);
                    g.setColor(Color.black);
                    g.setFont(new Font("Georgia", Font.PLAIN, 30));
                    g.drawString(String.valueOf(coins), 1800, 100);
                    g.drawImage(coin, 1750, 60, 48, 60, null);

                    g.drawString("Press 1-4", 160, 100);
                    g.drawString("to", 205, 150);
                    g.drawString("UPGRADE", 150, 200);

                    g.drawString("Level " + bombLevel, 130, 750);
                    g.drawString("Upgrade Missile Launcher", 30, 800);
                    g.drawString("Price: " + coins + "/" + (500 + (bombLevel - 1) * 150), 80, 850);

                    g.drawString("Level " + speedLevel, 650, 750);
                    g.drawString("Upgrade Ship Speed", 550, 800);
                    g.drawString("Price: " + coins + "/" + (500 + (speedLevel - 1) * 150), 580, 850);

                    g.drawString("Level " + healthLevel, 1100, 750);
                    g.drawString("Upgrade Max Health", 1000, 800);
                    g.drawString("Price: " + coins + "/" + (500 + (healthLevel - 1) * 150), 1030, 850);

                    g.drawString("Level " + shotLevel, 1600, 750);
                    g.drawString("Upgrade Machine Gun", 1500, 800);
                    g.drawString("Price: " + coins + "/" + (500 + (shotLevel - 1) * 150), 1540, 850);

                    g.drawString("Press Space to CONTINUE", 780, 950);
                    g.setFont(new Font("TimesRoman", Font.PLAIN, 70));
                    g.drawString("Ship Upgrades", 800, 100);
                } //else if


            } // if

            //notifies game win
            if (level == 4) {
                notifyGameWin();
            } //if 

            // clear graphics and flip buffer
            g.dispose();
            strategy.show();

            // ship should not move without user input
            ship.setHorizontalMovement(0);
            ship.setVerticalMovement(0);

            // respond to user moving ship
            if ((leftPressed) && (!rightPressed)) {
                ship.setHorizontalMovement(-moveSpeed);
            } //if
            if ((rightPressed) && (!leftPressed)) {
                ship.setHorizontalMovement(moveSpeed);
            } //if
            if (upPressed && (!downPressed)) {
                ship.setVerticalMovement(-moveSpeed);
            } //if
            if (downPressed && (!upPressed)) {
                ship.setVerticalMovement(moveSpeed);
            } // if

            // if mouse pressed, try to fire
            if (mousePressed) {
                tryToFire();
            } // if
            if (bombPressed) {
                tryToBomb();
            } // if

            // pause
            try {
                Thread.sleep(0);
            } catch (Exception e) {} //catch
        } // while
    } // gameLoop

    /*
     * generates player bomb
     */
    public void bomb(BombEntity b) {
        Graphics2D g = (Graphics2D) strategy.getDrawGraphics();
        g.drawImage(explosion, b.getX() - 100, b.getY() - 100, 200, 200, null);
        for (int i = 0; i < entities.size(); i++) {
            if (((Entity) entities.get(i)).getType().equals("alien") || ((Entity) entities.get(i)).getType().equals("kamikaze")) {
                if (Math.abs(((Entity) entities.get(i)).getX() - b.getX()) < 200 && Math.abs(((Entity) entities.get(i)).getY() - b.getY()) < 200) {
                    removeEntities.add(entities.get(i));
                    notifyAlienKilled();
                } //if
            } //if
        } //for
    } //bomb

    /*
     * reset the game with preset variable values every level
     */
    private void startGame() {
        // clear out any existing entities and initialize a new set
        entities.clear();
        bombNum = 2 + bombLevel;
        initEntities();

        leftPressed = false;
        rightPressed = false;
        upPressed = false;
        downPressed = false;
        bombPressed = false;
        mousePressed = false;
        shipHealth = shipHealthMax;
    } // startGame

    /*
     * inner class KeyInputHandler handles keyboard input from the user
     */
    private class KeyInputHandler extends KeyAdapter {
        //runs when key is pressed
        public void keyPressed(KeyEvent e) {
            // if waiting for keypress to start game, upgrade items
            if (waitingForSpacePress) {
                if (e.getKeyChar() == 49) {
                    if (waitingForSpacePress) {
                        upgradeBomb();
                    } //if
                } //if
                if (e.getKeyChar() == 50) {
                    if (waitingForSpacePress) {
                        upgradeShip();
                    } //if
                } //if
                if (e.getKeyChar() == 51) {
                    if (waitingForSpacePress) {
                        upgradeHealth();
                    } //if
                } //if
                if (e.getKeyChar() == 52) {
                    if (waitingForSpacePress) {
                        upgradeShot();
                    } //if
                } //if
                return;
            } // if

            // respond to move left, right, up, or down
            if (e.getKeyCode() == KeyEvent.VK_A) {
                leftPressed = true;
            } // if

            if (e.getKeyCode() == KeyEvent.VK_D) {
                rightPressed = true;
            } // if

            if (e.getKeyCode() == KeyEvent.VK_W) {
                upPressed = true;
            } // if
            if (e.getKeyCode() == KeyEvent.VK_S) {
                downPressed = true;
            } //if

        } // keyPressed

        /*
         * runs when key is released
         */
        public void keyReleased(KeyEvent e) {
            // if waiting for keypress to start game, do nothing
            if (waitingForSpacePress) {
                return;
            } // if

            // respond to move left, right, up, or down
            if (e.getKeyCode() == KeyEvent.VK_A) {
                leftPressed = false;
            } // if

            if (e.getKeyCode() == KeyEvent.VK_D) {
                rightPressed = false;
            } // if

            if (e.getKeyCode() == KeyEvent.VK_W) {
                upPressed = false;
            } //if

            if (e.getKeyCode() == KeyEvent.VK_S) {
                downPressed = false;
            } //if

        } // keyReleased

        /*
         * interface in title, win, and loss screen
         */
        public void keyTyped(KeyEvent e) {
            // if waiting for key press to start game
            if (waitingForSpacePress || waitingForMousePress) {
                if (e.getKeyChar() == 32 && level == 1) {
                    waitingForSpacePress = false;
                    waitingForMousePress = false;
                    startGame();
                    coins = 0;
                } else if (e.getKeyChar() == 32) {
                    waitingForSpacePress = false;
                    waitingForMousePress = false;
                    startGame();
                } //else if

            } //if

            // if escape is pressed, end game
            if (e.getKeyChar() == 27) {
                System.exit(0);
            } // if escape pressed

        } // keyTyped

    } // class KeyInputHandler

    /*
     * MouseInputHandler handles mouse input from the user
     */
    private class MouseInputHandler extends MouseAdapter {
        /*
         * handle mouse pressed events
         */
        public void mousePressed(MouseEvent e) {
            // if waiting for mouse press to start game, do nothing
            if (waitingForMousePress) {
                return;
            } // if

            // respond to move fire of bomb
            if (e.getButton() == MouseEvent.BUTTON1) {
                mousePressed = true;
            } // if
            if (e.getButton() == MouseEvent.BUTTON3) {
                bombPressed = true;
            } // if
        } //mousePressed

        /*
         * handle mouse released events
         */
        public void mouseReleased(MouseEvent e) {
            // if waiting for keypress to start game, do nothing
            if (waitingForMousePress) {
                return;
            } // if
            // respond to move fire or bomb
            if (e.getButton() == MouseEvent.BUTTON1) {
                mousePressed = false;
            } // if
            if (e.getButton() == MouseEvent.BUTTON3) {
                bombPressed = false;
            } // if
        } // keyReleased
    } // class KeyInputHandler

    /*
     * returns player ship coordinates
     */
    public double[] getShipCords() {
        double[] cords = {
            ship.getX(),
            ship.getY()
        };
        return cords;
    } //getShipCords

    /*
     * returns alien ship coordinates
     */
    public double[] getAlienCords() {
        int k = 0;
        double[] cords = new double[alienCount * 2];
        for (int i = 0; i < entities.size(); i++) {
            if (((Entity) entities.get(i)).getType().equals("alien")) {
                cords[k] = ((Entity) entities.get(i)).getX();
                cords[k + alienCount] = ((Entity) entities.get(i)).getY();
                k++;

            } //if
        } //for
        return cords;
    } //getAlienCords

    /*
     * returns player mouse coordinates
     */
    public double[] getMouseCords() {
        double[] cords = {
            MouseInfo.getPointerInfo().getLocation().getX(),
            MouseInfo.getPointerInfo().getLocation().getY()
        };
        return cords;
    } //getMouseCords

    public static void main(String[] args) {
        //saves all the images needed for game
        try {
            img = ImageIO.read(new File("sprites/space.jpg"));
            heart = ImageIO.read(new File("sprites/heart.png"));
            bombPic = ImageIO.read(new File("sprites/bomb.png"));
            coin = ImageIO.read(new File("sprites/coin.png"));
            banner = ImageIO.read(new File("sprites/banner.png"));
            upgradeRoom = ImageIO.read(new File("sprites/upgradeRoom.jpg"));
            explosion = ImageIO.read(new File("sprites/explosion.png"));
        } catch (IOException e) {} //catch
        new Game();
    } // main
} // Game