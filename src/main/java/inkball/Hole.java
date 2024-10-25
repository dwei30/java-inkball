
package inkball;

import processing.core.PImage;

/**
 * Represents a hole in the InkBall game.
 */
public class Hole implements Drawable {
    private int x, y;          // Position of the hole (centre)
    private int colourNumber;  // Colour number (e.g., 0 for grey, 1 for orange, etc.)
    private PImage sprite;     // The sprite image representing the hole
    public static final float ATTRACTION_RADIUS = 32; // Radius for attraction force

    /**
     * Constructor to initialize a hole with its position, color, and sprite.
     *
     * @param x            The X coordinate of the hole (centre).
     * @param y            The Y coordinate of the hole (centre).
     * @param colourNumber The color number of the hole.
     * @param sprite       The PImage sprite representing the hole.
     */
    public Hole(int x, int y, int colourNumber, PImage sprite) {
        this.x = x;
        this.y = y;
        this.colourNumber = colourNumber;
        this.sprite = sprite;
    }

    /**
     * Draws the hole on the screen using the provided App instance.
     *
     * @param app The App instance for drawing.
     */
    public void draw(App app) {
        app.image(sprite, x - (App.CELLSIZE), y - (App.CELLSIZE), App.CELLSIZE * 2, App.CELLSIZE * 2);
    }

    /**
     * Checks if the given ball is entering this hole and if the colors match.
     *
     * @param ball The ball to check.
     * @return 1 if the ball is captured correctly (matching color), -1 if it is the wrong color, 0 if no capture.
     */
    public int checkBallCapture(Ball ball) {
        // Ball centre
        float centreX = ball.getX();
        float centreY = ball.getY();

        // Calculate distance between ball and hole center
        float dx = x - centreX;
        float dy = y - centreY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // If within attraction radius (32 pixels)
        if (distance <= 32) {
            // Calculate size factor based on distance to the centre of the hole
            float sizeFactor = 1 - (distance / 32.0f);
            ball.setRadius((int) (App.CELLSIZE / 2 * (1 - sizeFactor)));

            // Attraction force
            float attractionX = dx * 0.005f;
            float attractionY = dy * 0.005f;

            // Adjust the direction
            float currentSpeed = (float) Math.sqrt(ball.getVx() * ball.getVx() + ball.getVy() * ball.getVy());

            // Update velocity
            ball.setVx(ball.getVx() + attractionX);
            ball.setVy(ball.getVy() + attractionY);

            // Normalize velocity to keep speed constant
            float newSpeed = (float) Math.sqrt(ball.getVx() * ball.getVx() + ball.getVy() * ball.getVy());
            if (newSpeed > 0) {
                ball.setVx((ball.getVx() / newSpeed) * currentSpeed);
                ball.setVy((ball.getVy() / newSpeed) * currentSpeed);
            }

            // Check if captured
            if (distance <= 10) {
                // Check if ball color matches hole, or if it's a grey ball/hole (ballColour 0 or holeColour 0)
                if (ball.getColourNumber() == colourNumber) {
                    return 1; // Correctly captured
                } else if (ball.getColourNumber() == 0) {
                    return 1; // Grey ball matches with any hole
                } else if (colourNumber == 0) {
                    return 1; // Any ball matches with a grey hole
                } else {
                    return -1; // Wrong hole capture
                }
            }
            return 0; // Ball is near, but not yet captured
        }

        return 0; // No interaction
    }
    
    /**
     * Gets the x-coordinate of the hole.
     *
     * @return the x-coordinate of the hole
     */
    public float getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of the hole.
     *
     * @return the y-coordinate of the hole
     */
    public float getY() {
        return y;
    }

    /**
     * Gets the colour number of the hole.
     *
     * @return the colour number of the hole
     */
    public int getColourNumber() {
        return colourNumber;
    }
}
