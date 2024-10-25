package inkball;

import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;

import processing.core.PImage;


public class Ball implements Drawable {

    public int radius = App.CELLSIZE / 2;
    private float x, y; //postiion
    private float vx, vy; //velocity
    private String colour;
    private PImage sprite;
    public int collisionBuffer = 0;
    private int colourNumber;

    /**
     * Creates a new Ball object with the specified position, velocity, color, and sprite.
     *
     * @param x      the initial x-coordinate of the ball
     * @param y      the initial y-coordinate of the ball
     * @param vx     the initial velocity in the x direction
     * @param vy     the initial velocity in the y direction
     * @param colour the color of the ball
     * @param sprite the PImage object representing the ball's sprite
     */
    public Ball(float x, float y, float vx, float vy, String colour, PImage sprite) {
        this.x = x + App.CELLSIZE / 2;
        this.y = y + App.CELLSIZE / 2;
        this.vx = vx;
        this.vy = vy;
        this.colour = colour;
        this.sprite = sprite;
        this.colourNumber = Integer.parseInt(colour.substring(4));
    }

    /**
     * Initializes the ball's velocity randomly, setting it to either 2 or -2 in both x and y directions.
     */
    public void initializeRandomVelocity() {
        //set velocity to either 2 or -2
        Random random = new Random();
        this.vx = random.nextBoolean() ? -2 : 2;
        this.vy = random.nextBoolean() ? -2 : 2;
    }

    /**
     * Sets the radius of the ball
     * 
     * @param newRadius the new radius of the ball
     */
    public void setRadius(int newRadius) {
        this.radius = newRadius;
    }

    /**
     * Gets the x-coordinate of the ball.
     *
     * @return the x-coordinate of the ball
     */
    public float getX() {
        return x;
    }
    
    /**
     * Gets the y-coordinate of the ball.
     *
     * @return the y-coordinate of the ball
     */
    public float getY() {
        return y;
    }
    
    /**
     * Gets the velocity in the x direction.
     *
     * @return the velocity in the x direction
     */
    public float getVx() {
        return vx;
    }
    
    /**
     * Gets the velocity in the y direction.
     *
     * @return the velocity in the y direction
     */
    public float getVy() {
        return vy;
    }

    /**
     * Gets the radius of the ball
     * 
     * @return the ball radius
     */
    public int getRadius() {
        return radius;
    }

    /**
     * Updates the ball's position by adding the current velocity to its x and y coordinates.
     */
    public void updatePostition() { //update ball postition
        x += vx;
        y += vy;
    }

    /**
     * Sets the velocity in the x direction.
     *
     * @param vx the new velocity in the x direction
     */
    public void setVx(float vx) {
        this.vx = vx;
    }

    /**
     * Sets the velocity in the y direction.
     *
     * @param vy the new velocity in the y direction
     */
    public void setVy(float vy) {
        this.vy = vy;
    }


    /**
     * Gets the color of the ball.
     *
     * @return the color of the ball as a string
     */
    public String getColour() {
        return colour;
    }

    /**
     * Gets the numerical value representing the ball's color.
     *
     * @return the color number of the ball
     */
    public int getColourNumber() {
        return colourNumber;
    }
    
    /**
     * Checks for collisions between the ball and walls or bricks. Updates the ball's velocity 
     * and color if a collision is detected.
     *
     * @param walls   the list of Wall objects to check for collisions
     * @param bricks  the list of Brick objects to check for collisions
     * @param sprites a hashmap containing the sprites
     */
    public void checkCollisions(ArrayList<Wall> walls, ArrayList<Brick> bricks, HashMap<String, PImage> sprites) {
        float centreX = x;  // X coordinate of the centre of the ball
        float centreY = y;

        //edge of screen
        if (centreX - radius < 0 || centreX + radius > App.WIDTH) {
            vx *= -1;
        }
        if (centreY - radius < App.TOPBAR || centreY + radius > App.HEIGHT) {
            vy *= -1;
        }

        if (collisionBuffer == 0) {  // Proceed only if no recent collision
            for (Wall wall : walls) {
                float wallCentreX = wall.getX() * App.CELLSIZE + App.CELLSIZE / 2;
                float wallCentreY = wall.getY() * App.CELLSIZE + App.CELLSIZE / 2 + App.TOPBAR;

                float distance = (float) Math.sqrt(Math.pow(centreX - wallCentreX, 2) + Math.pow(centreY - wallCentreY, 2));

                if (distance <= radius + App.CELLSIZE / 2) {
                    if (Math.abs(centreX - wallCentreX) > Math.abs(centreY - wallCentreY)) {
                        vx *= -1;
                    } else {
                        vy *= -1;
                    }
    
                    // check wall colour and ball colour
                    String wallType = wall.getType();
                    String ballType = this.colour;
                    
                    // If the colors are different, change the ball's color to match the wall
                    if (!wallType.equals("wall0") && !wallType.equals(ballType.replace("ball", "wall"))) {
                        String newBallType = wallType.replace("wall", "ball");  // Convert wall type to ball type
                        PImage newSprite = sprites.get(newBallType);  // Get the new sprite for the ball
                        
                        if (newSprite != null) {
                            changeColour(newBallType, newSprite);  // Change the ball's color and sprite
                        }
                    }
    
                    // Set collision buffer to prevent immediate re-collision
                    collisionBuffer = 2;
                    return;
                }
            }

            for (Brick brick : bricks) {
                if (!brick.isDestroyed()) {
                    float brickCentreX = brick.getX() * App.CELLSIZE + App.CELLSIZE / 2;
                    float brickCentreY = brick.getY() * App.CELLSIZE + App.CELLSIZE / 2 + App.TOPBAR;

                    float distance = (float) Math.sqrt(Math.pow(centreX - brickCentreX, 2) + Math.pow(centreY - brickCentreY, 2));

                    if (distance <= radius + App.CELLSIZE / 2) {
                        if (Math.abs(centreX - brickCentreX) > Math.abs(centreY - brickCentreY)) {
                            vx *= -1;
                        } else {
                            vy *= -1;
                        }

                        String brickType = brick.getType();
                        String ballType = this.colour;
                        // Damage the brick if the colors match, or if the brick is grey (brick0)
                        if (brickType.equals("wall0") || brickType.equals(ballType.replace("ball", "wall"))) {
                            brick.hit();  // Reduce the brick's health and change its sprite
                        }

                        collisionBuffer = 2;  // Set collision buffer
                        return;
                    }
                }
            }
        } else {
            collisionBuffer--;
        }
    }

    /**
     * Changes the color and sprite of the ball to the specified new color and sprite.
     *
     * @param newColour the new color for the ball
     * @param newSprite the new sprite image for the ball
     */
    public void changeColour(String newColour, PImage newSprite) {
        this.colour = newColour;
        this.sprite = newSprite;
        this.colourNumber = Integer.parseInt(newColour.substring(4)); // Update the colourNumber with the new color
    }

    /**
     * Draws the ball on the screen using the current sprite at the ball's position.
     *
     * @param app the App object used to render the ball
     */
    public void draw(App app) {
        //checkCollisions();
        app.image(sprite, x - radius , y - radius, 2 * radius, 2 * radius);
        
    }

}
