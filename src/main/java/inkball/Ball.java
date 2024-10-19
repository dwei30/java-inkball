package inkball;

import java.util.Random;
import java.util.ArrayList;
import java.util.HashMap;

import processing.core.PImage;


public class Ball {

    public static int radius = App.CELLSIZE / 2;
    private float x, y; //postiion
    private float vx, vy; //velocity
    private String colour;
    private PImage sprite;
    public int collisionBuffer = 0;
    private int colourNumber;

    public Ball(float x, float y, float vx, float vy, String colour, PImage sprite) {
        this.x = x + App.CELLSIZE / 2;
        this.y = y + App.CELLSIZE / 2;
        this.vx = vx;
        this.vy = vy;
        this.colour = colour;
        this.sprite = sprite;
        this.colourNumber = Integer.parseInt(colour.substring(4));
    }

    public void initializeRandomVelocity() {
        //set velocity to either 2 or -2
        Random random = new Random();
        this.vx = random.nextBoolean() ? -2 : 2;
        this.vy = random.nextBoolean() ? -2 : 2;
    }

    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    public float getVx() {
        return vx;
    }
    
    public float getVy() {
        return vy;
    }

    public void updatePostition() { //update ball postition
        x += vx;
        y += vy;
    }

    public void setVx(float vx) {
        this.vx = vx;
    }

    public void setVy(float vy) {
        this.vy = vy;
    }

    public String getColour() {
        return colour;
    }

    public int getColourNumber() {
        return colourNumber;
    }
    

    public void checkCollisions(String[][] board, HashMap<String, PImage> sprites) {
        float diameter = 2 * radius;
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
            // Grid positions of the ball
            int gridX = (int) (centreX / App.CELLSIZE);
            int gridY = (int) ((centreY - App.TOPBAR) / App.CELLSIZE); 
            
            // Check for wall collisions
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    int checkX = gridX + j;
                    int checkY = gridY + i;

                    // check if within grid
                    if (checkX >= 0 && checkX < board[0].length && checkY >= 0 && checkY < board.length) {
                        String tile = board[checkY][checkX];

                        if (tile != null && tile.startsWith("wall")) {
                            // tile Centre position
                            float tileCentreX = (checkX * App.CELLSIZE) + (App.CELLSIZE / 2);
                            float tileCentreY = (checkY * App.CELLSIZE) + (App.CELLSIZE / 2) + App.TOPBAR;

                            // distance between Centres
                            float distance = (float) Math.sqrt(Math.pow(centreX - tileCentreX, 2) + Math.pow(centreY - tileCentreY, 2));

                            // if less than radius
                            if (distance <= radius + (App.CELLSIZE / 2)) {
                                if (Math.abs(centreX - tileCentreX) > Math.abs(centreY - tileCentreY)) {
                                    vx *= -1;
                                } else {
                                    vy *= -1;
                                }

                                //check colour of wall
                                if (tile.startsWith("wall")) {
                                    String wallColour = tile;
                                    String ballColour = wallColour.replace("wall", "ball"); //replace wall string to colour
                                    PImage newSprite = sprites.get(ballColour); 

                                    if (newSprite != null && tile != "wall0") {
                                        changeColour(ballColour, newSprite);
                                    }
                                }

                                collisionBuffer = 5;
                                return;
                            }
                        }
                    }
                }
            } 
        } else {
            collisionBuffer--;
        }
    }

    //if near hole
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

    //change ball colour
    public void changeColour(String newColour, PImage newSprite) {
        this.colour = newColour;
        this.sprite = newSprite;
        this.colourNumber = Integer.parseInt(newColour.substring(4)); // Update the colourNumber with the new color

        // Debug print statements to confirm the change
        System.out.println("Ball color changed to: " + newColour);
        System.out.println("Updated color number: " + colourNumber);
    }

    //get colour name from sprite name
    /*private String getColourName(String spriteName) {
        switch (spriteName) {
            case "ball1": return "orange";
            case "ball2": return "blue";
            case "ball3": return "green";
            case "ball4": return "yellow";
            case "ball0": return "grey";
            default: return "grey";
        }
    } */
    

    //draw ball
    public void draw(App app) {
        //checkCollisions();
        app.image(sprite, x - radius , y - radius, 2 * radius, 2 * radius);
        
    }

}
