package inkball;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import processing.core.PApplet;

import java.awt.Point;
import static org.junit.jupiter.api.Assertions.*;

public class LineTest {

    static App app;
    static Line line;

    @BeforeAll
    public static void setup() {
        app = new App();
        PApplet.runSketch(new String[]{"App"}, app);
        app.setup();

        // new line for testing
        line = new Line();
    }

    @Test
    public void testAddPointToLine() {
        // add a line point at 50 50
        int x = 50;
        int y = 50;
        line.addPoint(x, y);

        // check the line was added
        assertEquals(1, line.getPoints().size(), "Line should have 1 point after adding.");
        
        // check the point added is correct
        Point point = line.getPoints().get(0);
        assertEquals(x, point.x, "X coordinate of the point should match.");
        assertEquals(y, point.y, "Y coordinate of the point should match.");

        // tests that a line is properly added
    }

    @Test
    public void testHandleCollisionWithBall() {
        // create ball to the left of the vertical line travelling right only
        Ball ball = new Ball(70, 50, 2, 0, "ball0", null);

        // create vertical line
        line.addPoint(100, 40);
        line.addPoint(100, 60);

        // test ball collision
        boolean collisionOccurred = line.handleCollision(ball);

        assertTrue(collisionOccurred, "Ball should collide with the line.");

        // tests that a collision occurs when a ball encounters a line
    }

    @Test
    public void testLineDrawing() {
        // Test if the line can be drawn without errors
        assertDoesNotThrow(() -> line.draw(app), "Line drawing should not throw any exceptions.");
    }
}
