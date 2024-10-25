package inkball;

import processing.core.PImage;

public class Brick extends Wall {

    //private int x, y;
    //private String type;
    //private PImage sprite;
    private PImage crackedSprite; //cracked sprite
    private int hitCount; //track number of hits
    private final int maxHits = 3;
    private boolean destroyed;

    /**
     * Constructs a Brick object with the specified position, type, sprite, and cracked sprite.
     * Tracks the number of hits and determines when the brick is destroyed.
     *
     * @param x            the x-coordinate of the brick on the grid
     * @param y            the y-coordinate of the brick on the grid
     * @param type         the type of the brick, which represents the brick's characteristics (e.g., color)
     * @param sprite       the PImage object representing the brick's sprite
     * @param crackedSprite the PImage object representing the cracked version of the brick's sprite
     */
    public Brick(int x, int y, String type, PImage sprite, PImage crackedSprite) {
        super(x, y, type, sprite);
        this.crackedSprite = crackedSprite;
        this.hitCount = 0;
        this.destroyed = false;
    }

    /**
     * Gets the current hit count of the brick, representing the number of times it has been hit.
     *
     * @return the current hit count of the brick
     */
    public int getHitCount() {
        return hitCount;
    }

    /**
     * Returns whether the brick is destroyed. A brick is considered destroyed if it has been hit the maximum number of times.
     *
     * @return true if the brick is destroyed, false otherwise
     */
    public boolean isDestroyed() {
        return destroyed;
    }

    /**
     * Draws the brick on the screen at its specified position if it has not been destroyed.
     *
     * @param app the App object used to render the brick
     */
    public void draw(App app) {
        if (!destroyed) {
            app.image(getSprite(), getX() * App.CELLSIZE, getY() * App.CELLSIZE + App.TOPBAR, App.CELLSIZE, App.CELLSIZE);
        }
    }

    // if hit
    /**
     * Handles the event when the brick is hit by a ball. Increases the hit count and changes the brick's appearance to 
     * a cracked sprite after the first hit. If the brick is hit the maximum number of times, it is marked as destroyed.
     */
    public void hit() {
        if (destroyed) {
            return;
        }

        hitCount++;
        if (hitCount >= maxHits) {
            destroyed = true;
        } else {
            setSprite(crackedSprite);
        }
    }
}
