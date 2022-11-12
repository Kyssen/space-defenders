/******************************************************************************************
 * Name:        BombEntity
 * Author:      Frederick Wang and Kyssen Yu
 * Date:        Mar 7, 2021
 * Purpose:     To create the bomb entities
 ******************************************************************************************/
import java.awt.geom.AffineTransform;

public class BombEntity extends Entity {
    private double moveSpeed = -500; // bomb move speed 
    private double angle = 0; // bomb angle
    private boolean used = false; // true if shot hits something
    private Game game; // the game in which the ship exists

    /* 
     * constructs the ship bomb
     */
    public BombEntity(Game g, String r, int newX, int newY, String newT) {
        super(r, newX, newY, newT); // calls the constructor in Entity
        game = g;
        //sets the bomb moving towards the direction of the mouse
        double diffX = (game.getShipCords()[0] + 50 - game.getMouseCords()[0]);
        double diffY = (game.getShipCords()[1] + 50 - (game.getMouseCords()[1] - 30));
        double hypo = Math.sqrt(diffY * diffY + diffX * diffX);
        double ratio = hypo / moveSpeed;
        dx = diffX / ratio;
        dy = diffY / ratio;
        angle = ((Math.atan2(diffY, diffX)) * 180) / Math.PI;
    } // constructor

    /* 
     * moves the bomb
     */
    public void move(long delta) {
        //rotates the bomb towards its travel direction
        affline = AffineTransform.getTranslateInstance(x, y);
        affline.rotate(Math.toRadians(angle) + Math.PI / 2, 20, 20);
        super.move(delta); // calls the move method in Entity
        // if shot moves off top of screen, remove it from entity list
        if (y < -100) {
            game.removeEntity(this);
        } // if
    } // move


    /* 
     * runs if bomb collides with an enemy
     */
    public void collidedWith(Entity other) {
        // prevents double kills
        if (used) {
            return;
        } // if

        // if it has hit an alien, kill it!
        if (other instanceof AlienEntity || other instanceof KamikazeAlienEntity || other instanceof BossAlienEntity) {
            //if collided with boss
            if (other instanceof BossAlienEntity) {
                game.bossHealth--;
                if (game.bossHealth < 1) {
                    game.removeEntity(this);
                    game.removeEntity(other);
                    game.notifyAlienKilled();
                    used = true;
                } else {
                    game.removeEntity(this);
                    used = true;
                } //else
                return;
            } //if
            // remove affect entities from the Entity list
            game.removeEntity(this);
            game.removeEntity(other);

            // notify the game that the alien is dead
            used = true;

            game.bomb(this);

        } // if

    } // collidedWith

} // BombEntity