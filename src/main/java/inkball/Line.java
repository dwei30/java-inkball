package inkball;

import java.awt.Point;
import java.util.ArrayList;
import processing.core.PApplet;

public class Line implements Drawable {
    //store all points that make up the line
    private ArrayList<Point> points;
    private final float thickness = 10.0f; 

    public Line() {
        this.points = new ArrayList<>();
    }

    public void addPoint(float x, float y) {
        points.add(new Point((int) x, (int) y));
    }

    public ArrayList<Point>  getPoints() {
        return points;
    }

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

    //check ball collision with line
    public boolean handleCollision(Ball ball){
        float ballx = ball.getX();
        float bally = ball.getY();
        float ballVx = ball.getVx();
        float ballVy = ball.getVy();

        for (int i = 0; i < points.size() - 1; i++) { //iterate each point
            Point p1 = points.get(i);
            Point p2 = points.get(i+1);

            if (ballNearSegment(ballx + ballVx, bally + ballVy, p1.x, p1.y, p2.x, p2.y)) {
                
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

    //checks if point near line
    private boolean pointNearSegment(float px, float py, float x1, float y1, float x2, float y2) {
        float dist = distanceToSegment(px, py, x1, y1, x2, y2);
        return dist <= thickness / 2;
    }

    //checks if ball near line
    private boolean ballNearSegment(float px, float py, float x1, float y1, float x2, float y2) {
        float dist = distanceToSegment(px, py, x1, y1, x2, y2);
        return dist <= thickness / 2 + Ball.radius;
    }

    //distance to segment
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

    private float dist(float x1, float y1, float x2, float y2) {
        return PApplet.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }


}
