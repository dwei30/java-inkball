package inkball;

import processing.core.PImage;

public class Wall implements Drawable {

    private int x, y;
    private String type;
    private PImage sprite;

    /**
     * Constructs a Wall object with the specified position, type, and sprite.
     *
     * @param x      the x-coordinate of the wall on the grid
     * @param y      the y-coordinate of the wall on the grid
     * @param type   the type of the wall, which represents the wall's characteristics (e.g., color)
     * @param sprite the PImage object representing the wall's sprite
     */
    public Wall(int x, int y, String type, PImage sprite) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.sprite = sprite;
    }

    /**
     * Gets the x-coordinate of the wall.
     *
     * @return the x-coordinate of the wall
     */
    public float getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of the wall.
     *
     * @return the y-coordinate of the wall
     */
    public float getY() {
        return y;
    }

    /**
     * Gets the type of the wall, which is used to identify the wall's characteristics (e.g., color).
     *
     * @return the type of the wall as a string
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the sprite image of the wall.
     *
     * @return the PImage object representing the wall's sprite
     */
    public PImage getSprite() {
        return sprite;
    }

    /**
     * Gets the sprite image of the wall.
     *
     * @param sprite the new PImage object
     * @return the updated PImage object
     */
    public void setSprite(PImage sprite) {
        this.sprite = sprite;
    }

    /**
     * Draws the wall on the screen at its specified position.
     *
     * @param app the App object used to render the wall
     */
    public void draw(App app) {
        app.image(sprite, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR, App.CELLSIZE, App.CELLSIZE);
    }
}
