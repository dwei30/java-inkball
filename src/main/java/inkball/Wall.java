package inkball;

import processing.core.PImage;

public class Wall implements Drawable {

    private int x, y;
    private String type;
    private PImage sprite;

    public Wall(int x, int y, String type, PImage sprite) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.sprite = sprite;
    }

    // Getters for wall position and type
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getType() {
        return type;
    }

    public PImage getSprite() {
        return sprite;
    }

    // Method to draw the wall
    public void draw(App app) {
        app.image(sprite, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR, App.CELLSIZE, App.CELLSIZE);
    }
}
