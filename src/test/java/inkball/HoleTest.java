package inkball;

import processing.core.PApplet;
import processing.core.PImage;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import processing.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.*;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HoleTest {
    static App app;

    @BeforeAll
    public static void setup() {
        app = new App();
        PApplet.runSketch(new String[]{"App"}, app);
        app.setup(); // Initialize the game environment.
    }

    @Test
    public void testDrawGameElementsViaDraw() {
        // Trigger the draw method to call drawGameElements indirectly
        app.draw();

        // Verify that at least some elements are present after calling draw
        // Checking balls list as a simple test
        assertNotNull(app.getBalls(), "Balls should be initialized.");
        assertTrue(app.getBalls().size() >= 0, "There should be zero or more balls.");

        // We can't check direct drawing, but we can test the state of game elements affected by drawGameElements.
        assertFalse(app.getBalls().isEmpty(), "There should be at least one ball drawn after setup.");
    }
    
}

// gradle run						Run the program
// gradle test						Run the testcases

// Please ensure you leave comments in your testcases explaining what the testcase is testing.
// Your mark will be based off the average of branches and instructions code coverage.
// To run the testcases and generate the jacoco code coverage report: 
// gradle test jacocoTestReport
