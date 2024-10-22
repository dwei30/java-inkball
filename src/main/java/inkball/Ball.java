package inkball;

import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;

import processing.core.PImage;


public class Ball implements Drawable {

    public static int radius = App.CELLSIZE / 2;
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
     * Checks if the ball is near a hole and changes size of ball if near hole
     * If the ball is captured by the hole, it checks if the colors match and returns the appropriate result.
     *
     * @param holeCentres the list of hole centers and their associated colors
     * @param ballColour  the color number of the ball
     * @return 1 if the ball is captured by a matching hole, -1 if the ball enters a wrong hole, or 0 if not captured
     */
    public int checkHole(ArrayList<Object[]> holeCentres, int ballColour){
        //ball Centre
        float centreX = x;
        float centreY = y;

        //iterate through holes
        for (Object[] holeCentre : holeCentres) {
            float holeCentreX = (float) holeCentre[0];
            float holeCentreY = (float) holeCentre[1];
            int holeColour = (int) holeCentre[2];

            //calculate distance
            float dx = holeCentreX - centreX;
            float dy = holeCentreY - centreY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            //if within 32 from centre
            if (distance <= 32) {
                // Calculate size factor based on distance to the centre of the hole
                float sizeFactor = 1 - (distance / 32.0f);
                radius = (int) (App.CELLSIZE / 2 * (1 - sizeFactor));

                //attraction force
                float attractionX = dx * 0.005f;
                float attractionY = dy * 0.005f;

                //adjust the direction
                float currentSpeed = (float) Math.sqrt(vx * vx + vy * vy);

                //update velocity
                vx += attractionX;
                vy += attractionY;

                //normalize velocity to keep speed
                float newSpeed = (float) Math.sqrt(vx * vx + vy * vy);
                if (newSpeed > 0) {
                    vx = (vx / newSpeed) * currentSpeed;
                    vy = (vy / newSpeed) * currentSpeed;
                }
                

                //check if captured
                if (distance <= 10) {
                    //check if ball colour matches hole, or grey colour ball, or grey colour hole
                    
                    //System.out.println("Checking ball color: " + ballColour + " against hole color: " + holeColour);
                    if (holeColour == ballColour) {
                        System.out.println("Exact match: " + holeColour + " and " + ballColour);
                        return 1; // captured
                    } else if (ballColour == 0) {
                        System.out.println("Grey ball matched with hole color: " + holeColour);
                        return 1; // captured
                    } else if (holeColour == 0) {
                        System.out.println("Ball color: " + ballColour + " matched with grey hole");
                        return 1; // captured
                    } else {
                        System.out.println("Mismatch: Ball color " + ballColour + " and hole color " + holeColour);
                        return -1; // nope
                    }
                }
                return 0;
            }
        }

        //if ball moved away
        radius = App.CELLSIZE / 2;
        return 0;
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

        // Debug print statements to confirm the change
        System.out.println("Ball color changed to: " + newColour);
        System.out.println("Updated color number: " + colourNumber);
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
