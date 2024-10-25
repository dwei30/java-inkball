package inkball;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import processing.core.PApplet;
import processing.core.PImage;

import static org.junit.jupiter.api.Assertions.*;

public class BallHoleTest {

    static App app;

    @BeforeAll
    public static void setup() {
        app = new App();
        PApplet.runSketch(new String[]{"App"}, app);
        app.setup(); // Initialize the game environment
    }

    private PImage loadSprite(String filename) {
        PImage sprite = app.loadImage("src/main/resources/inkball/" + filename);
        assertNotNull(sprite, "Failed to load the sprite: " + filename);
        return sprite;
    }

    @Test
    public void testBallEntersCorrectHole() {
        // Clear any existing balls and holes
        app.getBalls().clear();
        app.getHoles().clear();

        // Load the ball1.png sprite from the resources folder
        PImage ballSprite = loadSprite("ball1.png");

        // Add a ball and a matching hole at the same coordinates (100, 100)
        Ball ball = new Ball(100, 100, 0, 0, "ball1", ballSprite);
        app.getBalls().add(ball);
        
        // Make sure hole is offset with 16
        Hole hole = new Hole(100 + App.CELLSIZE / 2, 100 + App.CELLSIZE / 2, 1, ballSprite);
        app.getHoles().add(hole);

        // Test if the ball is captured by the correct hole
        int result = hole.checkBallCapture(ball);

        // Assert the ball enters the correct hole
        assertEquals(1, result, "Ball should enter the hole with the correct color.");
    }

    @Test
    public void testBallMissesHole() {
        // Clear any existing balls and holes
        app.getBalls().clear();
        app.getHoles().clear();

        // Load the ball1.png sprite from the resources folder
        PImage ballSprite = loadSprite("ball1.png");

        // Add a ball and a hole at different coordinates (100, 100 for the ball, 200, 200 for the hole)
        Ball ball = new Ball(100, 100, 0, 0, "ball1", ballSprite);
        app.getBalls().add(ball);

        // Make sure hole is offset with 16
        Hole hole = new Hole(200 + App.CELLSIZE / 2, 200 + App.CELLSIZE / 2, 1, ballSprite); // Place hole far from the ball
        app.getHoles().add(hole);

        // Test if the ball misses the hole
        int result = hole.checkBallCapture(ball);

        // Assert that the ball does not enter the hole
        assertEquals(0, result, "Ball should miss the hole.");
    }
}
