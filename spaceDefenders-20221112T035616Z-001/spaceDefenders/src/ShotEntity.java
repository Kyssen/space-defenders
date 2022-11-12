/******************************************************************************************
 * Name:        ShotEntity
 * Author:      Frederick Wang and Kyssen Yu
 * Date:        Mar 7, 2021
 * Purpose:     constructs player shot entity
 ******************************************************************************************/
import java.awt.geom.AffineTransform;

public class ShotEntity extends Entity {
    public double moveSpeed = -1000; // shot move speed
    private boolean used = false; // true if shot hits something
    private Game game; // the game in which the ship exists

    /* 
     * shot constructor for player ship
     */
    public ShotEntity(Game g, String r, int newX, int newY, String newT) {
        super(r, newX, newY, newT); // calls the constructor in Entity
        game = g;
        //sets the shot moving in the direction of the mouse cursor
        double diffX = (game.getShipCords()[0] + 50 - game.getMouseCords()[0]);
        double diffY = (game.getShipCords()[1] + 50 - (game.getMouseCords()[1] - 30));
        double hypo = Math.sqrt(diffY * diffY + diffX * diffX);
        double ratio = hypo / moveSpeed;
        dx = diffX / ratio;
        dy = diffY / ratio;
    } // constructor

    /* 
     * moves the shot according to dx and dy
     */
    public void move(long delta) {
        affline = AffineTransform.getTranslateInstance(x - 20, y - 20);
        super.move(delta); // calls the move method in Entity
        // if shot moves off top of screen, remove it from entity list
        if (y < -100) {
            game.removeEntity(this);
        } // if
    } // move


    /* 
     * runs if shot collides with an alien entity
     */
    public void collidedWith(Entity other) {
        // prevents double kills
        if (used) {
            return;
        } // if

        // if it has hit an alien, kill it!
        if (other instanceof AlienEntity || other instanceof KamikazeAlienEntity || other instanceof BossAlienEntity) {
            //if shot collided with boss
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
            game.removeEntity(this);
            game.removeEntity(other);

            // notify the game that the alien is dead
            game.notifyAlienKilled();
            used = true;
        } // if

    } // collidedWith

} // ShotEntity class