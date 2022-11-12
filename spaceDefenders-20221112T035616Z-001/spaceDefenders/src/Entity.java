/******************************************************************************************
 * Name:        Entity
 * Author:      Frederick Wang and Kyssen Yu
 * Date:        Mar 7, 2021
 * Purpose:     object that resolves collisions and movement
 ******************************************************************************************/
import java.awt.*;
import java.awt.geom.AffineTransform;

public abstract class Entity {
    protected double x; // current x location
    protected double y; // current y location
    protected Sprite sprite; // this entity's sprite
    protected String type;
    protected double dx; // horizontal speed (px/s)  + -> right
    protected double dy; // vertical speed (px/s) + -> down
    protected AffineTransform affline;
    private Rectangle me = new Rectangle(); // bounding rectangle of
    // this entity
    private Rectangle him = new Rectangle(); // bounding rect. of other
    // entities

    /* 
     * super constructor for all entities
     */
    public Entity(String r, int newX, int newY, String t) {
        x = newX;
        y = newY;
        type = t;
        sprite = (SpriteStore.get()).getSprite(r);
    } // constructor

    /* 
     * super method for moving entities 
     */
    public void move(long delta) {
        // update location of entity based on move speeds
        x += (delta * dx) / 1000;
        y += (delta * dy) / 1000;
    } // move

    // get and set velocities
    public void setHorizontalMovement(double newDX) {
        dx = newDX;
    } // setHorizontalMovement

    public void setVerticalMovement(double newDY) {
        dy = newDY;
    } // setVerticalMovement

    public double getHorizontalMovement() {
        return dx;
    } // getHorizontalMovement

    public double getVerticalMovement() {
        return dy;
    } // getVerticalMovement

    // get position
    public int getX() {
        return (int) x;
    } // getX

    public int getY() {
        return (int) y;
    } // getY

    public String getType() {
        return type;
    } // getY

    /*
     * Draw this entity to the graphics object provided at (x,y)
     */
    public void draw(Graphics2D g) {
        sprite.draw(g, affline);
    } // draw

    /* 
     * checks if an entity has collided with another
     */
    public boolean collidesWith(Entity other) {
        me.setBounds((int) x, (int) y, sprite.getWidth(), sprite.getHeight());
        him.setBounds(other.getX(), other.getY(),
            other.sprite.getWidth(), other.sprite.getHeight());
        return me.intersects(him);
    } // collidesWith

    /* 
     * notifies that entities have collided
     */
    public abstract void collidedWith(Entity other);

} // Entity