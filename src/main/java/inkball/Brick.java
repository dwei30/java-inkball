package inkball;

import processing.core.PImage;

public class Brick implements Drawable {

    private int x, y;
    private String type;
    private PImage sprite;
    private PImage crackedSprite; //cracked sprite
    private int hitCount; //track number of hits
    private final int maxHits = 3;
    private boolean destroyed;

    public Brick(int x, int y, String type, PImage sprite, PImage crackedSprite) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.sprite = sprite;
        this.crackedSprite = crackedSprite;
        this.hitCount = 0;
        this.destroyed = false;
    }

    // Getters for brick position and type
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getType() {
        return type;
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public PImage getSprite() {
        return sprite;
    }

    public void draw(App app) {
        if (!destroyed) {
            app.image(sprite, x * App.CELLSIZE, y * App.CELLSIZE + App.TOPBAR, App.CELLSIZE, App.CELLSIZE);
        }
    }

    // if hit
    public void hit() {
        if (destroyed) {
            return;
        }

        hitCount++;
        if (hitCount >= maxHits) {
            destroyed = true;
        } else {
            sprite = crackedSprite;
        }
    }
}
