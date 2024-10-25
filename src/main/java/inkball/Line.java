package inkball;

import java.awt.Point;
import java.util.ArrayList;
import processing.core.PApplet;

public class Line {
    //store all points that make up the line
    private ArrayList<Point> points;
    private final float thickness = 10.0f; 

    /**
     * Constructs a new Line object with an empty list of points.
     */
    public Line() {
        this.points = new ArrayList<>();
    }

    /**
     * Adds a point to the line at the specified x and y coordinates.
     *
     * @param x the x-coordinate of the point to add
     * @param y the y-coordinate of the point to add
     */
    public void addPoint(float x, float y) {
        points.add(new Point((int) x, (int) y));
    }

    /**
     * Returns the list of points that make up the line.
     *
     * @return the list of points that form the line
     */
    public ArrayList<Point> getPoints() {
        return points;
    }

    /**
     * Draws the line on the screen by connecting the points with straight lines.
     *
     * @param app the App object used to render the line
     */
    public void draw(App app) { // Connect dots via line
        app.strokeWeight(thickness);
        if (points.size() > 1) {
            for (int i = 0; i < points.size() - 1; i++) {
                Point p1 = points.get(i);
                Point p2 = points.get(i + 1);
                app.line(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }

    /**
     * Checks if the mouse is near the line by comparing its position with each segment of the line.
     *
     * @param mouseX the x-coordinate of the mouse
     * @param mouseY the y-coordinate of the mouse
     * @return true if the mouse is near any segment of the line, false otherwise
     */
    public boolean mouseNearLine(float mouseX, float mouseY) {
        for (int i = 0; i < points.size() -1; i ++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);

            if (pointNearSegment(mouseX, mouseY, p1.x, p1.y, p2.x, p2.y)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Handles the collision between the ball and the line, adjusting the ball's velocity if a collision occurs.
     *
     * @param ball the Ball object to check for a collision
     * @return true if a collision occurred, false otherwise
     */
    public boolean handleCollision(Ball ball){
        float ballx = ball.getX();
        float bally = ball.getY();
        float ballVx = ball.getVx();
        float ballVy = ball.getVy();
        float ballRadius = ball.getRadius();

        for (int i = 0; i < points.size() - 1; i++) { //iterate each point
            Point p1 = points.get(i);
            Point p2 = points.get(i+1);

            if (ballNearSegment(ballx + ballVx, bally + ballVy, p1.x, p1.y, p2.x, p2.y, ballRadius)) {
                
                //calculate normal vectors
                float dx = p2.x - p1.x;
                float dy = p2.y - p1.y;

                //create normals
                float[] normal1 = {-dy, dx};
                float[] normal2 = {dy, -dx};

                //nomalize vectors
                float length1 = (float) Math.sqrt((normal1[0]) * normal1[0] + normal1[1] * normal1[1]);
                normal1[0] /= length1;
                normal1[1] /= length1;

                float length2 = (float) Math.sqrt((normal2[0]) * normal2[0] + normal2[1] * normal2[1]);
                normal2[0] /= length2;
                normal2[1] /= length2;

                //calculate dot product
                float dot1 = (ballVx * normal1[0]) + (ballVy * normal1[1]);
                float dot2 = (ballVx * normal2[0]) + (ballVy * normal2[1]);

                //choose closer normal
                float[] closerNormal;
                if (dot1 <= dot2) {
                    closerNormal = normal1;
                } else {
                    closerNormal = normal2;
                }

                //change velocity using formula v - 2 (v * n) n

                float vn = (ballVx * closerNormal[0]) + (ballVy * closerNormal[1]);
                ball.setVx(ballVx - 2 * vn * closerNormal[0]);
                ball.setVy(ballVy - 2 * vn * closerNormal[1]);

                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a point is near a line segment by calculating the distance between the point and the segment.
     *
     * @param px the x-coordinate of the point
     * @param py the y-coordinate of the point
     * @param x1 the x-coordinate of the first point of the segment
     * @param y1 the y-coordinate of the first point of the segment
     * @param x2 the x-coordinate of the second point of the segment
     * @param y2 the y-coordinate of the second point of the segment
     * @return true if the point is near the segment, false otherwise
     */
    private boolean pointNearSegment(float px, float py, float x1, float y1, float x2, float y2) {
        float dist = distanceToSegment(px, py, x1, y1, x2, y2);
        return dist <= thickness / 2;
    }

    /**
     * Checks if a ball is near a line segment by calculating the distance between the ball and the segment.
     *
     * @param px the x-coordinate of the ball
     * @param py the y-coordinate of the ball
     * @param x1 the x-coordinate of the first point of the segment
     * @param y1 the y-coordinate of the first point of the segment
     * @param x2 the x-coordinate of the second point of the segment
     * @param y2 the y-coordinate of the second point of the segment
     * @return true if the ball is near the segment, false otherwise
     */
    private boolean ballNearSegment(float px, float py, float x1, float y1, float x2, float y2, float ballRadius) {
        float dist = distanceToSegment(px, py, x1, y1, x2, y2);
        return dist <= thickness / 2 + ballRadius;
    }

    /**
     * Calculates the distance from a point to a line segment.
     *
     * @param px the x-coordinate of the point
     * @param py the y-coordinate of the point
     * @param x1 the x-coordinate of the first point of the segment
     * @param y1 the y-coordinate of the first point of the segment
     * @param x2 the x-coordinate of the second point of the segment
     * @param y2 the y-coordinate of the second point of the segment
     * @return the distance from the point to the segment
     */
    private float distanceToSegment(float px, float py, float x1, float y1, float x2, float y2) {
        float temp = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
        
        //if the segment is just a point
        if (temp == 0) return dist(px, py, x1, y1);

        //calculate closest point on line
        float temp2 = Math.max(0, Math.min(1, ((px - x1) * (x2 - x1) + (py - y1) * (y2 - y1)) / temp));

        float finalX = x1 + temp2 * (x2 - x1);
        float finalY = y1 + temp2 * (y2 - y1);

        return dist(px, py, finalX, finalY);
    }

    /**
     * Calculates the Euclidean distance between two points.
     *
     * @param x1 the x-coordinate of the first point
     * @param y1 the y-coordinate of the first point
     * @param x2 the x-coordinate of the second point
     * @param y2 the y-coordinate of the second point
     * @return the distance between the two points
     */
    private float dist(float x1, float y1, float x2, float y2) {
        return PApplet.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }
}
