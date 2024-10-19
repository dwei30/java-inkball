package inkball;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.io.*;
import java.util.*;

public class App extends PApplet {

    public static final int CELLSIZE = 32; //8;
    public static final int CELLHEIGHT = 32;

    public static final int CELLAVG = 32;
    public static final int TOPBAR = 64;
    public static int WIDTH = 576; //CELLSIZE*BOARD_WIDTH;
    public static int HEIGHT = 640; //BOARD_HEIGHT*CELLSIZE+TOPBAR;
    public static final int BOARD_WIDTH = WIDTH/CELLSIZE;
    public static final int BOARD_HEIGHT = 20;

    public static final int INITIAL_PARACHUTES = 1;

    public static final int FPS = 30;

    public String configPath;
    private String[][] board;
    private ArrayList<Ball> balls = new ArrayList<>(); //stores balls
    private ArrayList<Line> lines;    // stores lines
    private ArrayList<Object[]> holeCentres = new ArrayList<>(); //stores the hole centres and colour (x , y , colour)
    private Line currentLine;          // current line drawn
    private ArrayList<int[]> spawnerLocations = new ArrayList<>(); //spawner coordinates
    
    private Queue<String> ballColoursToSpawn = new LinkedList<>(); //ball colours to spawn
    private int spawnInterval;
    private int spawnTimer;
    private int currentBallIndex = 0;
    private int queueOffset = 0; //for visual queue

    //countdown timer
    private int countdownTime;
    private int countdownX = 200;
    private int countdownY = 20;

    public static Random random = new Random();
	
	//scoring variables
    private int score = 0;
    private float scoreIncreaseModifier;
    private float scoreDecreaseModifier;
    private HashMap<String, Integer> scoreIncreaseValues = new HashMap<>();
    private HashMap<String, Integer> scoreDecreaseValues = new HashMap<>();

    //game status variables
    private int timeRemaining;
    private boolean levelEnded = false;
    private boolean gameEnded = false;
    private boolean timeUp = false;
    private boolean gameFinished = false;
    private int currentLevelIndex = 0;
    private JSONArray levels;
    private boolean paused = false;
    private int incrementedTime = 0;
    private boolean incrementingScore = false;
    
    //queue display
    private String lastEmittedBallColor = "";

    //animation variables
    private int counter = 0; //counter for tracking movement steps
    private int directionTopLeft = 0;
    private int directionBottomRight = 2;
    private int topLeftX = 0;
    private int topLeftY = 0; //initial position for the top-left tile
    private int bottomRightX = 17;
    private int bottomRightY = 17; //initial position for the bottom-right tile
    private boolean animationTriggered = false;


    private HashMap<String, PImage> sprites = new HashMap<>();
    
    //INITIALIZATION
    
    public App() {
        this.configPath = "config.json";
    }

    /**
     * Initialise the setting of the window size.
     */
	@Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Load all resources such as images. Initialise the elements such as the player and map elements.
     */
	@Override
    public void setup() {
        frameRate(FPS);
        lines = new ArrayList<>(); // new line array for storing lines
        currentLine = null;

        //loadSprites();
        loadSprites();
        initializeBoard(); //initialize game board structure
        loadConfigData(); //load config data from the config file

        //load level data for the current level index variable (level number)k
        loadLevelData(currentLevelIndex);

        //initialize spawn timer and countdown time
        spawnTimer = spawnInterval * FPS;
        countdownTime = spawnInterval;


		//See PApplet javadoc:
		//loadJSONObject(configPath)
		// the image is loaded from relative path: "src/main/resources/inkball/..."
		/*try {
            result = loadImage(URLDecoder.decode(this.getClass().getResource(filename+".png").getPath(), StandardCharsets.UTF_8.name()));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }*/
    }

    private void loadSprites() {

        //list all sprite names to be used in game
        String[] spriteNames = {
            "tile", "wall0", "wall1", "wall2", "wall3", "wall4",
            "hole0", "hole1", "hole2", "hole3", "hole4", "entrypoint",
            "ball0", "ball1", "ball2", "ball3", "ball4"
        };

        //store them in hash map
        for (String spriteName : spriteNames) {
            getSprite(spriteName);
        }
    }

    private void initializeBoard() {
        //initialize board based on dimensions given
        this.board = new String[(HEIGHT-TOPBAR)/CELLSIZE][WIDTH/CELLSIZE];
    }

    private void loadConfigData() {
        
        //load config file
        JSONObject config = loadJSONObject("config.json");
        levels = config.getJSONArray("levels"); //extracts levels from config file
    }

    //PROCESSING GAME DATA

    public PImage getSprite(String spriteName) {

        //load sprites from spriteName and store in hashmap
        PImage sprite = sprites.get(spriteName);
        if (sprite == null) {
            try {

                //tries to get path and load image
                String path = this.getClass().getResource("/inkball/" + spriteName + ".png").getPath().replace("%20", " ");
                sprite = loadImage(path);
                sprites.put(spriteName, sprite); //stores in hashmap
            } catch (NullPointerException e) {
                
                //catches error for if file not found
                System.out.println("The file /inkball/" + spriteName + ".png is missing or inaccessible.");
            }
        }
        return sprite;
    }

    public void loadLevelData(int levelIndex) {
        
        //check if the level index is within bounds
        if (levelIndex >= levels.size()) {
            gameEnded = true;
            return;
        }

        //parse each level data
        JSONObject levelData = levels.getJSONObject(levelIndex);
        loadLevel(levelData.getString("layout"));
        loadConfig(levelData);

        //initialize time and status flags for new level
        timeRemaining = levelData.getInt("time");
        levelEnded = false;
        timeUp = false;
        spawnTimer = spawnInterval * FPS;
        countdownTime = spawnInterval;
    }

    public void loadLevel(String filename) {
        
        //clear previous level coordinates
        clearLevelData();
        
        String[] lines = loadStrings(filename);
        for (int i = 0; i < lines.length; i++) {
            for (int j = 0; j < lines[i].length(); j++) {
                char tileChar = lines[i].charAt(j);
                

                //check next character
                char nextChar;
                if (j + 1 < lines[i].length()) {
                    nextChar = lines[i].charAt(j + 1);
                } else {
                    nextChar = ' ';
                }

                processTile(i, j, tileChar, nextChar, lines[i]);

                //skip next character if ball and hole to avoid number after the letter
                if (tileChar == 'B' || tileChar == 'H') {
                    j ++;
                }
            }
        }
    }

    private void clearLevelData() {
        
        //clear level data
        spawnerLocations.clear();
        holeCentres.clear();
    }

    private void processTile(int row, int col, char tileChar, char nextChar, String line) {
        
        //adds spawner coordinates if spawner detected
        if (tileChar == 'S') {
            spawnerLocations.add(new int[]{col * CELLSIZE, row * CELLSIZE + TOPBAR});
            board[row][col] = "entrypoint";
            return;
        }
        
        //checks if hole and balls coloured
        if (tileChar == 'H' || tileChar == 'B') {
            processSpecialTile(row, col, tileChar, nextChar);
        } else {
            board[row][col] = spriteNameFromChar(tileChar, ' ');
        }
    }

    private void processSpecialTile(int row, int col, char tileChar, char nextChar) {
        
        //sets sprite name for tile
        board[row][col] = spriteNameFromChar(tileChar, nextChar);
        
        //add hole or bole
        if (tileChar == 'B') {
            addBall(row, col, nextChar);
        } else if (tileChar == 'H') {
            addHole(row, col, nextChar);
        }
    }

    private void addBall(int row, int col, char nextChar) {
        
        //initialize ball coordinates
        float startX = col * CELLSIZE;
        float startY = row * CELLSIZE + TOPBAR;
        String colour = "ball" + nextChar;
        PImage sprite = getSprite(colour);

        //adds ball with random velocity and adds to the balls list
        Ball ball = new Ball(startX, startY, 0, 0, colour, sprite);
        ball.initializeRandomVelocity();  // set random velocities
        balls.add(ball);

        board[row][col] = "tile";
        board[row][col + 1] = "tile";
    }

    private void addHole(int row, int col, char nextChar) {
        
        //make hole x2 bigger
        //store hole co-ordinates into the array
        float holeCentreX = (col * CELLSIZE) + (CELLSIZE);
        float holeCentreY = (row * CELLSIZE) + (CELLSIZE) + TOPBAR;
        int holeColour = Character.getNumericValue(nextChar);

        holeCentres.add(new Object[]{holeCentreX, holeCentreY, holeColour});

        String holeType = spriteNameFromChar('H', nextChar);

        //set other cells as hole
        board[row][col] = holeType;
        board[row][col + 1] = holeType;
        board[row + 1][col] = holeType;
        board[row + 1][col + 1] = holeType;
        
        //clear adjacent tile
        board[row][col + 1] = "tile";
    }
    
    public void loadConfig(JSONObject levelData) {

        //set spawn interval from config
        spawnInterval = levelData.getInt("spawn_interval");
        JSONArray ballsArray = levelData.getJSONArray("balls");

        //add balls to spawn queue
        ballColoursToSpawn.clear();
        for (int i = 0; i < ballsArray.size(); i++) {
            String colour = ballsArray.getString(i);
            ballColoursToSpawn.add(String.valueOf(getColourNumberFromName(colour)));
        }

        //set spawn timer and ball index for queue
        spawnTimer = spawnInterval * FPS;
        currentBallIndex = 0;

        //load score modifiers from config file
        scoreIncreaseModifier = (float) levelData.getDouble("score_increase_from_hole_capture_modifier");
        scoreDecreaseModifier = (float) levelData.getDouble("score_decrease_from_wrong_hole_modifier");

        //load score values
        loadScoreValues();
    }

    private void loadScoreValues() {
        
        //load score values from config file
        JSONObject config = loadJSONObject("config.json");
        JSONObject scoreIncreaseConfig = config.getJSONObject("score_increase_from_hole_capture");
        JSONObject scoreDecreaseConfig = config.getJSONObject("score_decrease_from_wrong_hole");

        //put values in increase hash set
        scoreIncreaseValues.put("grey", scoreIncreaseConfig.getInt("grey"));
        scoreIncreaseValues.put("orange", scoreIncreaseConfig.getInt("orange"));
        scoreIncreaseValues.put("blue", scoreIncreaseConfig.getInt("blue"));
        scoreIncreaseValues.put("green", scoreIncreaseConfig.getInt("green"));
        scoreIncreaseValues.put("yellow", scoreIncreaseConfig.getInt("yellow"));

        //put values in decrease hash set
        scoreDecreaseValues.put("grey", scoreDecreaseConfig.getInt("grey"));
        scoreDecreaseValues.put("orange", scoreDecreaseConfig.getInt("orange"));
        scoreDecreaseValues.put("blue", scoreDecreaseConfig.getInt("blue"));
        scoreDecreaseValues.put("green", scoreDecreaseConfig.getInt("green"));
        scoreDecreaseValues.put("yellow", scoreDecreaseConfig.getInt("yellow"));
    }

    //HANDLE INPUT

	@Override
    public void keyPressed(KeyEvent event){
        
        //get input
        char key = event.getKey();
        
        //check for r key for restart
        if (key == 'r') {
            handleRestart();
        }

        //check spacebar for pause/play
        if (key == ' ') {
            paused = !paused;
        }
    }

    private void handleRestart() {
        
        //if game end reset entire game
        if (gameEnded) {
            resetGame();
        } else if (levelEnded || timeUp) {
            
            //if time up or level end restart level
            restartLevel();
        }
    }

    private void resetGame() {
        
        //if game ended, reset game
        
        //reset all game related variables to initial
        currentLevelIndex = 0;
        score = 0;
        incrementedTime = 0;
        gameEnded = false;
        levelEnded = false;
        timeUp = false;
        incrementingScore = false;
        animationTriggered = false; 
        
        //clear balls and lines
        balls.clear();
        lines.clear();
        
        //load initial level data
        loadLevelData(currentLevelIndex);
        //loop();
    }

    private void restartLevel() {
        
        //if level ended, restart level
        //loop();
        balls.clear();
        lines.clear();
        loadLevelData(currentLevelIndex);
    }

    /**
     * Receive key released signal from the keyboard.
     */
	// @Override
    // public void keyReleased(){
        
    // }

    @Override
    public void mousePressed() {
        
        // create a new player-drawn line object if left click
        if (mouseButton == LEFT) {
            startNewLine();
        } else if (mouseButton == RIGHT) { //right click to delete line
            deleteLine();
        }
    }
	
    @Override
    public void mouseDragged() {
        
        //add line when held
        if (mouseButton == LEFT && currentLine != null) {
            currentLine.addPoint(mouseX, mouseY);
        }
    }

    @Override
    public void mouseReleased() {

        //draw line end
		if (mouseButton == LEFT && currentLine != null) {
            lines.add(currentLine);
            currentLine = null;
        }
    }

    private void startNewLine() {

        //create new line and add mouse position to Line
        currentLine = new Line();
        currentLine.addPoint(mouseX, mouseY);
    }

    private void deleteLine() {

        //iterate through lines to delete line closest to mouse position
        for (int i = 0; i < lines.size(); i ++) {
            Line line = lines.get(i);
            if (line.mouseNearLine(mouseX, mouseY)) {
                lines.remove(i);
                break;
            }    
        } 
    }

    // GAME LOOP AND DRAWING
    
    /**
     * Draw all elements in the game by current frame.
     */
	@Override
    public void draw() {

        //set background colour
        background(123);

        //update game status
        updateGameStatus();

        //draw game elements like boards lines balls
        drawGameElements();
        
        //check if level is complete
        if (!paused && !incrementingScore) {
            checkLevelCompletion();
        } else if (incrementingScore) {
            
            //update score otherwise
            updateAndDrawScore();
        }

        //display messages like pause end
        displayMessages();

        //stop balls if game end
        if (levelEnded || gameEnded) {
            return;
        }
        
    }
 
    private void updateGameStatus() {
        
        //display relavent messages and update spawn timer if ball spawned
        displayMessages();
        updateSpawnTimer();

        //update remaining time if not paused and not incrementing score
        if (!paused & !incrementingScore) {
            updateTimeRemaning();
        }
    }
    
    private void updateTimeRemaning() {
        
        //if level not ended and time not up
        if (!levelEnded && !timeUp) {   
            
            //decrease time every second
            if (frameCount % FPS == 0 && timeRemaining > 0) {
                timeRemaining --;
            }

            //if no time, timeup and level end
            if (timeRemaining == 0) {
                timeUp = true;
                levelEnded = true;
            }
        }
    }
    
    private void updateSpawnTimer() {

        //if there are balls to spawn or timer active
        if (!ballColoursToSpawn.isEmpty() || (spawnTimer > 0)) {
            
            //if not paused and time not up
            if (!paused && !timeUp) {

                //if spawn timer hits 0, spawn ball and reset timer
                if (spawnTimer <= 0) {
                    spawnBall();
                    spawnTimer = spawnInterval * FPS;
                    countdownTime = spawnInterval; // Reset the countdown time when a ball is spawned
                } else {

                    //decrease time and update countdown time
                    spawnTimer--;
                    countdownTime = spawnTimer / FPS;
                }
            }
        } else {

            //reset countdown when no balls left
            countdownTime = 0;
        }
    }

    // GAME ELEMENTS

    private void drawGameElements() {

        //draw game elemenets: board, lines, balls, ball queue, timer and score
        drawBoard();
        drawLines();
        drawBalls();
        displayBallQueue();
        displayTimer();
        displayScore();
    }

    private void drawBoard() {

        //iterate through game board
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                String spriteName = board[i][j];
                if (spriteName != null) {
                    
                    //draw everything but hole
                    if (!spriteName.startsWith("hole") || !spriteName.startsWith("X")) {
                        PImage sprite = getSprite(spriteName);
                        if (sprite != null) {

                            //draw sprite at corresponding location
                            image(sprite, j * CELLSIZE, i * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                        }
                    }
                }
            }
        }

        //iterate through game board again and draw holes
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                String spriteName = board[i][j];
                if (spriteName != null && spriteName.startsWith("hole")) {
                    PImage sprite = getSprite(spriteName);
                    if (sprite != null) {
                        
                        //draw hole at corresponding location
                        image(sprite, j * CELLSIZE, i * CELLSIZE + TOPBAR, CELLSIZE * 2, CELLSIZE * 2);
                    }
                }
            }
        }
    }

    private void drawLines() {
        
        //draw lines if time not up
        if (!timeUp){
            stroke(0);  // line colour
            strokeWeight(10);  // line thickness
            for (Line line : lines) {
                line.draw(this);
            }

            //draw line if exists
            if (currentLine != null) {
                currentLine.draw(this);
            }
        }
    }

    private void drawBalls() {
        
        //List to store removed balls
        List<Ball> ballsToRemove = new ArrayList<>();

        //iterate through each ball
        for (Ball ball : balls) {
            updateBalls(ball, ballsToRemove);
        }

        //remove balls marked for removal
        balls.removeAll(ballsToRemove);
    }

    private void displayBallQueue() {
        
        //set queue display position
        int startX = 20 + queueOffset;
        int startY = 10;
        int ballSpacing = 34;

        //queue visual
        //display ball queue logic
        noStroke(); //remove border lines

        fill(0); //create black rectangle
        rect(18, 5, 170, 44);

        //move to left by 1 unit if shifted to right
        if (queueOffset > 0) {
            queueOffset--; //move queue back 1 pixel per frame
        }

        //display most recent emitted ball
        if (!lastEmittedBallColor.isEmpty()) {
            String firstSpriteName = "ball" + lastEmittedBallColor;
            PImage firstSprite = getSprite(firstSpriteName);

            //display first ball if availible
            if (firstSprite != null) {
                image(firstSprite, startX - ballSpacing, startY, CELLSIZE, CELLSIZE); // Display the solo sprite
            }
        }
        

        //display next 5 balls
        String[] queueArray = ballColoursToSpawn.toArray(new String[0]);
        for (int i = 0; i < queueArray.length; i ++) {
            String ballColorNumber = queueArray[i];
            String spriteName = "ball" + ballColorNumber;
            PImage sprite = getSprite(spriteName);

            //display balls in corrosponding postiion
            if (sprite != null) {
                image(sprite, startX + i * ballSpacing, startY, CELLSIZE, CELLSIZE);
            }
        }

        // hide the first ball if the queue is empty (worst case)
        if (ballColoursToSpawn.isEmpty() && spawnTimer == 0) {
            lastEmittedBallColor = ""; // reset last emitted ball color to hide it
        }

        //mask off sides
        fill(200);
        rect(0, 5, 18, 44); //left side
        rect(188, 5, WIDTH - 190, 44); //right side
        rect(0, 0, WIDTH, 5); //above
        rect(0, 49, WIDTH, TOPBAR - 49); // below

    }

    // SCORE AND TIMERS

    private void displayMessages() {
        
        //display message on game state
        //pause message
        if (paused) {
            fill(255, 0, 0);
            textAlign(CENTER, CENTER);
            text("=== PAUSED ===", WIDTH / 2, TOPBAR - 20);
        } else if (timeUp && gameEnded && !gameFinished) {
            
            //time up message
            fill(255, 0, 0);
            textAlign(CENTER, CENTER);
            text("=== TIME'S UP ===", WIDTH / 2, TOPBAR - 20);
        } else if (gameEnded && gameFinished) {
            
            //game end message
            fill(255, 0, 0);
            textAlign(CENTER, CENTER);
            text("=== ENDED ===", WIDTH / 2, TOPBAR / 2);
        }
    }

    private void updateAndDrawScore() {
        
        //update score and yellow animations when level complete
        if (incrementedTime < timeRemaining) {
            if (frameCount % 2 == 0) { //increment every 2 frames
                score ++; //increment score per time
                incrementedTime++;
                animateTiles(1);
            } else {
                animateTiles(0);
            }

            //update score display
            displayScore();
        } else {
            
            //when finished reset states
            incrementingScore = false;
            animationTriggered = false;
            timeRemaining = 0;
        }
    }

    private void displayTimer() {
        
        //display timer
        fill(0);
        textSize(16);
        textAlign(RIGHT, CENTER);
        text("Time: " + timeRemaining, WIDTH - 30, 40);

        //display queue timer
        fill(0);
        textSize(14);
        textAlign(LEFT, CENTER);
        text(String.format("%.1f", spawnTimer / (float) FPS), countdownX, countdownY);
    }

    private void displayScore() {
        //display current score
        fill(0);
        textSize(16);
        textAlign(RIGHT, CENTER);
        text("Score: " + score, WIDTH - 30, 20);
    }

    // LOGIC AND MECHANICS

    private void updateBalls(Ball ball, List<Ball> ballsToRemove) {
    
        //update ball postitions and check for collisions if not paused or not time up
        if (!paused & !timeUp){
            ball.updatePostition();
        }
        ball.checkCollisions(board, sprites);
        handleBallInteractions(ball, ballsToRemove);
        
        //draw updated ball
        ball.draw(this);
    }

    private void handleBallInteractions(Ball ball, List<Ball> ballsToRemove) {
        
        //ball colour
        int ballColourNumber = ball.getColourNumber();
        
        //check line collision with ball
        for (int i = lines.size() - 1; i >= 0; i --) {
            Line line = lines.get(i);
            if (line.handleCollision(ball)) {
                lines.remove(i);
            }
        }

        //check if ball enters hole
        int result = ball.checkHole(holeCentres, ballColourNumber);
        if (result == 1) {

            //correct hole/ball colour, increase score
            increaseScore(ballColourNumber);
            ballsToRemove.add(ball);

        } else if (result == -1) {

            //wrong colour, decreasej score and add ball back to queue
            decreaseScore(ballColourNumber);
            ballsToRemove.add(ball);
            ballColoursToSpawn.add(String.valueOf(ballColourNumber)); // add ball back to queue
            spawnTimer = spawnInterval * FPS; //reset spawn timer
        }
    }

    private void increaseScore(int ballColourNumber) {
        
        //increase ball score based on colour
        String colourName = getColorNameFromNumber(ballColourNumber);
        if (scoreIncreaseValues.containsKey(colourName)) {
            int increaseValue = scoreIncreaseValues.get(colourName);
            score += increaseValue * scoreIncreaseModifier;
        } else {
            System.out.println("Error: Colour name " + colourName + " not found in scoreIncreaseValues.");
        }
    }

    private void decreaseScore(int ballColourNumber) {

        //decrease ball score based on colour
        String colourName = getColorNameFromNumber(ballColourNumber);
        if (scoreDecreaseValues.containsKey(colourName)) {
            int decreaseValue = scoreDecreaseValues.get(colourName);
            score -= decreaseValue * scoreDecreaseModifier;
        } else {
            System.out.println("Error: Colour name " + colourName + " not found in scoreDecreaseValues.");
        }
    }

    private void checkLevelCompletion() {

        //check if all balls are cleared and level is completed
        if (balls.isEmpty() && ballColoursToSpawn.isEmpty()) {
            
            //if not game ended
            if (!gameEnded) {
                
                //add remaining time to score
                System.out.println("time remaining: " + timeRemaining + " " + incrementingScore);
                if (timeRemaining > 0 && !incrementingScore) {
                    incrementingScore = true;
                    incrementedTime = 0; //reset for new level
                    animationTriggered = false;
                }
            }

            //check what type of game end it is
            if (!incrementingScore) {

                //if last level
                if (isLastLevel()) {
                    
                    //game finished
                    gameEnded = true;
                    gameFinished = true;
                } else {
                    
                    //move to next level only when finishing incrementing score 
                    currentLevelIndex ++;
                    levelEnded = false;
                    timeUp = false;
                    gameFinished = false;
                    incrementingScore = false;
                    animationTriggered = true; 
                    incrementedTime = 0;
                    
                    //clear exisitng lists
                    balls.clear();
                    lines.clear();
                    
                    //load next level
                    loadLevelData(currentLevelIndex);
                }  
            }
        }
    }
    
    private void animateTiles(int i) {

        //retrieve yellow wall sprite
        PImage yellowWallSprite = getSprite("wall4");
        
        if (!animationTriggered) {

            //resets to initial values per animation 
            counter = 0; //counter for tracking movement steps
            directionTopLeft = 0;
            directionBottomRight = 2;
            topLeftX = 0;
            topLeftY = 0; //initial position for the top-left tile
            bottomRightX = 17;
            bottomRightY = 17; //initial position for the bottom-right tile
            animationTriggered = true;
        }

        // Draw the top-left tile at its current position
        image(yellowWallSprite, topLeftX * CELLSIZE, topLeftY * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
        image(yellowWallSprite, bottomRightX * CELLSIZE, bottomRightY * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);

        if (i == 1) {
        
            //move top left tile based on direction
            // 0 = right, 1 = down, 2 = left, 3 = up
            if (directionTopLeft == 0) {
                topLeftX ++;
            } else if (directionTopLeft == 1) {
                topLeftY ++;
            } else if (directionTopLeft == 2) {
                topLeftX --;
            } else if (directionTopLeft == 3) {
                topLeftY --;
            }

            //move bottom right tile based on direction
            if (directionBottomRight == 0) {
                bottomRightX ++;
            } else if (directionBottomRight == 1) {
                bottomRightY ++;
            } else if (directionBottomRight == 2) {
                bottomRightX --;
            } else if (directionBottomRight == 3) {
                bottomRightY --;
            }
            
            

            //increase counter and check if direction needs changing
            counter ++;
            if (counter == 17) {
                counter = 0; //reset counter

                directionTopLeft = (directionTopLeft + 1) % 4;
                directionBottomRight = (directionBottomRight + 1) % 4;
            }
        }
    }

    private boolean isLastLevel() {

        //checks if level index is last from levels
        return currentLevelIndex >= levels.size() - 1;
    }
    
    private void spawnBall() {

        //spawn new ball at random spawner location
        if (spawnerLocations.isEmpty() || ballColoursToSpawn.isEmpty()) {
            return;
        }

        int[] spawner = spawnerLocations.get(random.nextInt(spawnerLocations.size()));
        float startX = spawner[0];
        float startY = spawner[1];

        //get colour for new ball
        String colourString = ballColoursToSpawn.poll();
        lastEmittedBallColor = colourString; //save for queue

        int colourNumber = Integer.parseInt(colourString);
        String spriteName = "ball" + colourNumber;
        PImage sprite = getSprite(spriteName);
        currentBallIndex++;
        
        //spawn new ball
        Ball newBall = new Ball(startX, startY, random.nextBoolean() ? 2 : -2, random.nextBoolean() ? 2 : -2, spriteName, sprite);
        balls.add(newBall);

        //shift the visual queue by one ball width
        queueOffset += 34; 
    }

    // UTILITY

    private String getSpriteNameFromColor(String colour) {

        //return sprite name based on colour name
        switch (colour.toLowerCase()) {
            case "orange":
                return "ball1";
            case "blue":
                return "ball2";
            case "green":
                return "ball3";
            case "yellow":
                return "ball4";
            case "grey":
                return "ball0";
            default:
                return "ball0";
        }
    }

    private int getColourNumberFromName(String colourName) {
        
        //return colour number based on colour name
        switch (colourName.toLowerCase()) {
            case "orange":
                return 1;
            case "blue":
                return 2;
            case "green":
                return 3;
            case "yellow":
                return 4;
            case "grey":
                return 0;
            default:
                return 0;
        }
    }

    private String getColorNameFromNumber(int colorNumber) {
        
        //return colour name based on colour number
        switch (colorNumber) {
            case 0:
                return "grey";
            case 1:
                return "orange";
            case 2:
                return "blue";
            case 3:
                return "green";
            case 4:
                return "yellow";
            default:
                return "grey";
        }
    }

    public String spriteNameFromChar(char tileChar, char nextChar) {
        
        //map character fomr level files to sprite name
        switch (tileChar) {
            case 'X':
                return "wall0";
            case '1':
                return "wall1";
            case '2':
                return "wall2";
            case '3':
                return "wall3";
            case '4':
                return "wall4";
            case 'H':
                switch (nextChar) {
                    case '0': return "hole0";
                    case '1': return "hole1";
                    case '2': return "hole2";
                    case '3': return "hole3";
                    case '4': return "hole4";
                    default: return "hole0";
                }
            case 'B':
                switch (nextChar) {
                    case '0': return "ball0";
                    case '1': return "ball1";
                    case '2': return "ball2";
                    case '3': return "ball3";
                    case '4': return "ball4";
                    default: return "ball0";
                }
            case 'S':
                return "entrypoint";
            default:
                return "tile";
        }
    }

    public List<Ball> getBalls() {
        return balls;
    }
    
    public static void main(String[] args) {
        PApplet.main("inkball.App");
    }

}
