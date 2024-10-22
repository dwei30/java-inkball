package inkball;

import processing.core.PApplet;
import processing.core.PImage;

import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SampleTest {

    @Test
    public void simpleTest() {
        App app = new App();
        app.loop();
        PApplet.runSketch(new String[] { "App" }, app);
        app.setup();
        app.delay(1000); // delay is to give time to initialise stuff before drawing begins
    }

    @Test
    public void testBallInitialization() {

        Ball ball = new Ball(100, 100, 2, 2, "ball0", null);

        assertEquals(100 + App.CELLSIZE / 2, ball.getX(), "Ball X position should be initialized correctly");
        assertEquals(100 + App.CELLSIZE / 2, ball.getY(), "Ball Y position should be initialized correctly");
        assertEquals(2, ball.getVx(), "Ball X velocity should be initialized correctly");
        assertEquals(2, ball.getVy(), "Ball Y velocity should be initialized correctly");

        // Check if the ball's color is initialized correctly
        assertEquals("ball0", ball.getColour(), "Ball color should be initialized correctly");

        // Test the color number is derived correctly from the ball's color string
        assertEquals(0, ball.getColourNumber(), "Ball color number should be initialized as expected");

        //tests that the ball is initialized with correct initial postion and velocity
    } 


    // @Test
    // public void testBallMovement() {

    //     Ball ball = new Ball(100, 100, 2, 2, "ball0", null);
    //     ball.updatePostition();

    //     assertEquals(102 + App.CELLSIZE / 2, ball.getX(), "Ball X position should be updated correctly");
    //     assertEquals(102 + App.CELLSIZE / 2, ball.getY(), "Ball Y position should be updated correctly");

    //     //tests that the ball moves as expected
    // }

    // @Test
    // public void testBallWallCollision() {

    //     Ball ball = new Ball(570, 100, 2, 0, "ball0", null); // Ball near right wall
    //     ArrayList<Wall> walls = new ArrayList<>();
    //     walls.add(new Wall(17, 3, "wall0", null));

    //     ball.updatePostition();
    //     ball.checkCollisions(walls, new ArrayList<>(), new HashMap<>());

    //     assertEquals(-2, ball.getVx(), "Ball should bounce back from the right wall");

    //     //tests the ball bounces the right wall
    // }

    // @Test
    // public void testBallTopbarCollision() {

    //     Ball ball = new Ball(100, 65, 0, -20, "ball0", null); //set ball near topbar where it is > 64 with y velocity
    //     ArrayList<Wall> walls = new ArrayList<>();

    //     ball.updatePostition();
    //     ball.checkCollisions(walls, new ArrayList<>(), new HashMap<>());

    //     assertEquals(20, ball.getVy(), "Ball should bounce back from the topbar");

    //     //tests the ball doesnt go over the top bar and bounces instead

    // }

    // @Test
    // public void testBallWallTileCollision() {

    //     Ball ball = new Ball(56, 56, 2, 2, "ball0", null); //set ball at coord 56 56
    //     ArrayList<Wall> walls = new ArrayList<>();

    //     walls.add(new Wall(2, 2, "wall0", null)); // wall tile at coord 64 64

    //     ball.updatePostition();
    //     ball.checkCollisions(walls, new ArrayList<>(), new HashMap<>());

    //     assertTrue(ball.getVx() == -2 || ball.getVy() == -2, "Ball should reflect off wall tile");

    //     //tests the ball reflects off a wall tile
    // }





}

// gradle run						Run the program
// gradle test						Run the testcases

// Please ensure you leave comments in your testcases explaining what the testcase is testing.
// Your mark will be based off the average of branches and instructions code coverage.
// To run the testcases and generate the jacoco code coverage report: 
// gradle test jacocoTestReport
