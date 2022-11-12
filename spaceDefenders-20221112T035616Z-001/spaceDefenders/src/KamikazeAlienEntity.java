/******************************************************************************************
 * Name:        KamikazeAlienEntity
 * Author:      Frederick Wang and Kyssen Yu
 * Date:        Mar 7, 2021
 * Purpose:     constructs kamikaze alien entities
 ******************************************************************************************/
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class KamikazeAlienEntity extends Entity {
    private double moveSpeed = 200; // move speed
    private Game game; // the game in which the alien exists

    /* 
     * constructs kamikaze aliens
     */
    public KamikazeAlienEntity(Game g, String r, int newX, int newY, String newT, Graphics2D graphics) {
        super(r, newX, newY, newT); // calls the constructor in Entity
        game = g;
    } // constructor

    /* 
     * moves the alien in the direction of the player ship
     */
    public void move(long delta) {
        int prox = 0;
        double[] cords = game.getAlienCords();
        double diffX = (game.getShipCords()[0] + 50 - x);
        double diffY = (game.getShipCords()[1] + 50 - y);
        double hypo = Math.sqrt(diffY * diffY + diffX * diffX);
        double ratio = hypo / moveSpeed;

        dx = diffX / ratio;
        dy = diffY / ratio;

        //for preventing alien ship clumping
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

        // sets the alien ship pointing towards the player ship
        double angle = ((Math.atan2(diffY, diffX)) * 180) / Math.PI;
        affline = AffineTransform.getTranslateInstance(x, y);
        affline.rotate(Math.toRadians(angle) + Math.PI / 2, 20, 20);

        // proceed with move
        super.move(delta);
    } // move

    /* 
     * runs if kamikaze aliens collide with entity
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

} // KamikazeAlienEntity