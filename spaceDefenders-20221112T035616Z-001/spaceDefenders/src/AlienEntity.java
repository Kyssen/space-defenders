/******************************************************************************************
 * Name:        AlienEntity
 * Author:      Frederick Wang and Kyssen Yu
 * Date:        Mar 7, 2021
 * Purpose:     To create the alien ship entities
 ******************************************************************************************/
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class AlienEntity extends Entity {

    private long firingInterval = 5000; //firing interval
    private long lastFire = 0; // laster fired time
    private double moveSpeed = 200; // horizontal speed
    private Game game; // the game in which the alien exists

    /* 
     * constructor for alien ship
     */
    public AlienEntity(Game g, String r, int newX, int newY, String newT, Graphics2D graphics) {
        super(r, newX, newY, newT); // calls the constructor in Entity
        game = g;
    } // constructor

    /*
     * tries to fire with alien ship
     */
    public AlienShotEntity tryFire() {
        if (this.getY() < 1000 && this.getY() > 0) {
            if ((System.currentTimeMillis() - lastFire) < firingInterval) {
                return new AlienShotEntity(game, "sprites/shot.png", 0, 10000, "");
            } // if

            // otherwise add a shot
            lastFire = System.currentTimeMillis();
            AlienShotEntity shot = new AlienShotEntity(game, "sprites/shot.png", this.getX() + 10, this.getY() - 30, "shot");
            return shot;
        } //if
        return new AlienShotEntity(game, "sprites/shot.png", 0, 10000, "");
    } //tryFire

    /* 
     * moves the alien around the screen
     */
    public void move(long delta) {
        int prox = 0;
        double[] cords = game.getAlienCords();

        //gets the alien ship to move towards the player
        double diffX = (game.getShipCords()[0] + 50 - x);
        double diffY = (game.getShipCords()[1] + 50 - y);
        double hypo = Math.sqrt(diffY * diffY + diffX * diffX);
        double ratio = hypo / moveSpeed;
        if (hypo > 400) {
            dx = diffX / ratio;
            dy = diffY / ratio;
        } else {
            dx = -diffX / ratio;
            dy = -diffY / ratio;
        } //else

        //prevents aliens to clump together
        for (int i = 0; i < cords.length / 2; i++) {
            double diffXA = (cords[i] - x);
            double diffYA = (cords[i + cords.length / 2] - y);
            double hypoA = Math.sqrt(diffYA * diffYA + diffXA * diffXA);
            double ratioA = hypoA / moveSpeed;
            if (hypoA < 70 && prox > 0) {
                dx = -diffXA / ratioA;
                dy = -diffYA / ratioA;
            } //if
            if (hypoA < 70) {
                prox++;
            } //if
        } //for

        /*
         * points alien ship at player
         */
        double angle = ((Math.atan2(diffY, diffX)) * 180) / Math.PI;
        affline = AffineTransform.getTranslateInstance(x, y);
        affline.rotate(Math.toRadians(angle), 20, 20);

        // proceed with move
        super.move(delta);
    } // move

    /* 
     * runs if alien ship collides with something
     */
    public void collidedWith(Entity other) {
        if (other instanceof ShipEntity) {
            // remove affect entities from the Entity list
            game.removeEntity(this);
            game.removeShip(other);

            // notify the game that the alien is dead
            game.alienCount--;
            game.notifyDeath();
        } // if
    } //collidedWith

} // AlienEntity