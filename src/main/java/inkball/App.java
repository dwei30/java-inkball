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
    private ArrayList<Wall> walls = new ArrayList<>();
    private ArrayList<Ball> balls = new ArrayList<>(); //stores balls
    private ArrayList<Line> lines;    // stores lines
    private ArrayList<Object[]> holeCentres = new ArrayList<>(); //stores the hole centres and colour (x , y , colour)
    private Line currentLine;          // current line drawn
    private ArrayList<int[]> spawnerLocations = new ArrayList<>(); //spawner coordinates
    private ArrayList<Brick> bricks = new ArrayList<>(); // stores bricks (EXTENSION)


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
    
    /**
     * Constructor for the App class, sets the configuration file path.
     */
    
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

    /**
     * Loads sprite images and stores them in a hashmap.
     */
    private void loadSprites() {

        //list all sprite names to be used in game
        String[] spriteNames = {
            "tile", "wall0", "wall1", "wall2", "wall3", "wall4",
            "hole0", "hole1", "hole2", "hole3", "hole4", "entrypoint",
            "ball0", "ball1", "ball2", "ball3", "ball4",
            "break0", "break1", "break2", "break3", "break4" // extension sprites for broken tiles
        };

        //store them in hash map
        for (String spriteName : spriteNames) {
            getSprite(spriteName);
        }
    }

    
    /**
     * Initializes the game board based on the height and width of the game window.
     */
    private void initializeBoard() {
        //initialize board based on dimensions given
        this.board = new String[(HEIGHT-TOPBAR)/CELLSIZE][WIDTH/CELLSIZE];
    }

    /**
     * Loads game configuration data from the config file.
     */
    private void loadConfigData() {
        
        //load config file
        JSONObject config = loadJSONObject("config.json");
        levels = config.getJSONArray("levels"); //extracts levels from config file
    }

    //PROCESSING GAME DATA


    /**
     * Retrieves the sprite image for the given sprite name from the hashmap, or loads it from the file system if not found.
     *
     * @param spriteName the name of the sprite to load
     * @return the PImage object representing the sprite
     */
    private PImage getSprite(String spriteName) {

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
                System.out.println("The file /inkball/" + spriteName + ".png is missing or inaccessible");
            }
        }
        return sprite;
    }

    /**
     * Loads the level data for the specified level index, setting up the level layout and game elements.
     *
     * @param levelIndex the index of the level to load
     */
    private void loadLevelData(int levelIndex) {
        
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

    /**
     * Loads the level layout from the specified file.
     *
     * @param filename the name of the file containing the level layout
     */
    private void loadLevel(String filename) {
        
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
                if (tileChar == 'B' || tileChar == 'H' || tileChar == 'F' || tileChar == 'V') {
                    j ++;
                }
            }
        }
    }

    /**
     * Clears the data of the current level, removing spawners and holes.
     */
    private void clearLevelData() {
        
        //clear level data
        spawnerLocations.clear();
        holeCentres.clear();
    }

    /**
     * Processes the individual tile characters from the level file, creating game elements accordingly.
     *
     * @param row the row index of the tile
     * @param col the column index of the tile
     * @param tileChar the character representing the tile
     * @param nextChar the next character in the line, used for multi-character tiles
     * @param line the entire line being processed
     */
    private void processTile(int row, int col, char tileChar, char nextChar, String line) {
        
        //adds wall
        if (tileChar == 'X' || tileChar == '1' || tileChar == '2' || tileChar == '3' || tileChar == '4') {
            String wallType = spriteNameFromChar(tileChar, ' ');
            PImage wallSprite = getSprite(wallType);
            Wall wall = new Wall(col, row, wallType, wallSprite);
            walls.add(wall);
            //board[row][col] = "wall";
        }
        
         if (tileChar == '5' || tileChar == '6' || tileChar == '7' || tileChar == '8' || tileChar <= '9') {
             addBrick(row, col, tileChar);
         }
        
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

    /**
     * Processes special tiles like holes and balls, adding them to the game board and storing relevant information.
     *
     * @param row      the row index of the special tile
     * @param col      the column index of the special tile
     * @param tileChar the character representing the special tile ('H' for hole, 'B' for ball)
     * @param nextChar the next character in the line, used to determine the color of the hole or ball
     */
    private void processSpecialTile(int row, int col, char tileChar, char nextChar) {
        
        //sets sprite name for tile
        board[row][col] = spriteNameFromChar(tileChar, nextChar);
        
        //add hole or ball
        if (tileChar == 'B') {
            addBall(row, col, nextChar);
        } else if (tileChar == 'H') {
            addHole(row, col, nextChar);
        }
    }

    /**
     * Adds a ball to the game board based on the given row, column, and color.
     *
     * @param row the row index of the ball
     * @param col the column index of the ball
     * @param nextChar the character representing the ball color
     */
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

    /**
     * Adds a hole to the game board based on the given row, column, and color.
     *
     * @param row the row index of the hole
     * @param col the column index of the hole
     * @param nextChar the character representing the hole color
     */
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
    
    /**
     * Adds a brick to the game board based on the given row, column, and type.
     *
     * @param row the row index of the brick
     * @param col the column index of the brick
     * @param tileChar the character representing the brick type
     */
    private void addBrick(int row, int col, char tileChar) {
        
        if (tileChar >= '5' && tileChar <= '9') {
            int tileValue = Character.getNumericValue(tileChar) - 5;
            String type = "wall" + (tileValue); // Convert brick numbers to wall types
            PImage brickSprite = getSprite(type);  // Use the same sprites as walls

            //get cracked wall sprites
            String type2 = "break" + (tileValue);
            PImage crackedBrickSprite = getSprite(type2);

            Brick brick = new Brick(col, row, type, brickSprite, crackedBrickSprite);
            bricks.add(brick);
        }
    }
    
    /**
     * Loads the level configuration such as spawn intervals, ball colors, and score modifiers.
     *
     * @param levelData the JSON object containing the level configuration data
     */
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

    /**
     * Loads the score increase and decrease values from the config file.
     */
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

    /**
     * Handles keyboard input events, allowing the user to restart the game or pause/resume the game.
     *
     * @param event the KeyEvent object representing the key that was pressed
     */
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

    /**
     * Handles the logic for restarting the game or the current level based on the current game state.
     */
    private void handleRestart() {
        
        //if game end reset entire game
        if (gameEnded) {
            resetGame();
        } else if (levelEnded || timeUp) {
            
            //if time up or level end restart level
            restartLevel();
        }
    }

    /**
     * Resets the entire game to its initial state, clearing all game elements and starting from the first level.
     */
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

    /**
     * Restarts the current level, resetting the balls and lines while keeping the current level configuration.
     */
    private void restartLevel() {
        
        //if level ended, restart level
        //loop();
        balls.clear();
        lines.clear();

        loadLevelData(currentLevelIndex);
    }

    /**
     * Handles mouse press events, allowing the user to start drawing a new line with the left click 
     * or delete an existing line with the right click.
     */
    @Override
    public void mousePressed() {
        
        // create a new player-drawn line object if left click
        if (mouseButton == LEFT) {
            startNewLine();
        } else if (mouseButton == RIGHT) { //right click to delete line
            deleteLine();
        }
    }
	
    /**
     * Handles mouse drag events, allowing the user to add points to the current line while the left mouse button is held.
     */
    @Override
    public void mouseDragged() {
        
        //add line when held
        if (mouseButton == LEFT && currentLine != null) {
            currentLine.addPoint(mouseX, mouseY);
        }
    }

    /**
     * Handles mouse release events, finalizing the current line and adding it to the list of lines.
     */
    @Override
    public void mouseReleased() {

        //draw line end
		if (mouseButton == LEFT && currentLine != null) {
            lines.add(currentLine);
            currentLine = null;
        }
    }

    /**
     * Starts a new line by creating a new Line object and adding the current mouse position as the first point.
     */
    private void startNewLine() {

        //create new line and add mouse position to Line
        currentLine = new Line();
        currentLine.addPoint(mouseX, mouseY);
    }

    /**
     * Deletes the line closest to the current mouse position, if one exists, by iterating through all lines.
     */
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
     * Draws the game elements on the screen, updating the game state and handling animations, 
     * score display, and level completion checks.
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
 
    /**
     * Updates the game status, including the spawn timer, time remaining, and game pause state.
     */
    private void updateGameStatus() {
        
        //display relavent messages and update spawn timer if ball spawned
        displayMessages();
        updateSpawnTimer();

        //update remaining time if not paused and not incrementing score
        if (!paused & !incrementingScore) {
            updateTimeRemaning();
        }
    }
    
    /**
     * Updates the remaining time for the current level, decrementing it each second, and checks if the time has run out.
     */
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
    
    /**
     * Updates the spawn timer and triggers the spawning of a new ball when the timer reaches zero.
     */
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

    /**
     * Draws all the game elements including the board, walls, bricks, lines, balls, ball queue, timer, and score.
     */
    private void drawGameElements() {

        //draw game elemenets: board, lines, balls, ball queue, timer and score
        drawBoard();
        drawWalls();
        drawBricks(); // EXTENSION
        drawLines();
        drawBalls();
        displayBallQueue();
        displayTimer();
        displayScore();
    }

    /**
     * Draws the game board, including the base tiles and spawners, as well as any special tiles such as holes.
     */
    private void drawBoard() {

        // draw base board (standard tile and spawners only)
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                PImage tileSprite = getSprite("tile");
                
                if (board[i][j].equals("entrypoint")) {
                    PImage spawnerSprite = getSprite("entrypoint");
                    image(spawnerSprite, j * CELLSIZE, i * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                } else {
                    // Otherwise, draw the tile sprite
                    image(tileSprite, j * CELLSIZE, i * CELLSIZE + TOPBAR, CELLSIZE, CELLSIZE);
                }
            }
        }

        //iterate through game board again and draw holes
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                String spriteName = board[i][j];
                if (spriteName != null && spriteName.startsWith("hole")) {
                    PImage sprite = getSprite(spriteName);
                    image(sprite, j * CELLSIZE, i * CELLSIZE + TOPBAR, CELLSIZE * 2, CELLSIZE * 2);
                }
            }
        }

    }

    /**
     * Draws all the wall elements on the game board.
     */
    private void drawWalls() {
        for (Wall wall : walls) {
            wall.draw(this);
        }
    }
    
    /**
     * Draws all the breakable brick elements on the game board.
     */
    private void drawBricks() {
        for (Brick brick : bricks) {
            brick.draw(this);
        }
    }

    /**
     * Draws all player-drawn lines and the current line being drawn.
     */
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

    /**
     * Draws all the balls on the game board and updates their positions. Removes balls marked for removal.
     */
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

    /**
     * Displays the ball queue, showing the upcoming balls to be spawned and the last emitted ball.
     */
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

    /**
     * Displays messages on the screen such as pause state, time up, or game end.
     */
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

    /**
     * Updates the score based on the remaining time and animates the tiles when the level is complete.
     */
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

    /**
     * Displays the remaining time and the countdown timer for the next ball spawn on the screen.
     */
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

    /**
     * Displays the player's current score on the screen.
     */
    private void displayScore() {
        //display current score
        fill(0);
        textSize(16);
        textAlign(RIGHT, CENTER);
        text("Score: " + score, WIDTH - 30, 20);
    }

    // LOGIC AND MECHANICS

    /**
     * Updates the position of the given ball, checks for collisions, and handles interactions such as entering holes.
     *
     * @param ball           the Ball object to update
     * @param ballsToRemove  the list of balls marked for removal after interacting with a hole
     */
    private void updateBalls(Ball ball, List<Ball> ballsToRemove) {
    
        //update ball postitions and check for collisions if not paused or not time up
        if (!paused & !timeUp){
            ball.updatePostition();
        }
        ball.checkCollisions(walls, bricks, sprites);
        handleBallInteractions(ball, ballsToRemove);
        
        //draw updated ball
        ball.draw(this);
    }

    /**
     * Handles interactions between the ball and game elements such as lines, holes, and tiles.
     *
     * @param ball           the Ball object to check interactions for
     * @param ballsToRemove  the list of balls marked for removal after interacting with a hole
     */
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

        //check if ball on acceleration tile
        //ball.checkAccelerationTile(accelTiles);
    }

    /**
     * Increases the score based on the ball's color when it enters the correct hole.
     *
     * @param ballColourNumber the color number of the ball that entered the hole
     */
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

    /**
     * Decreases the score based on the ball's color when it enters the wrong hole.
     *
     * @param ballColourNumber the color number of the ball that entered the wrong hole
     */
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

    /**
     * Checks if the level is complete by verifying if all balls have been cleared and if no more balls are left to spawn.
     * If the level is complete, it handles transitioning to the next level or ending the game.
     */
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
    
    /**
     * Animates yellow tiles during level completion, moving them around the board in a clockwise direction.
     *
     * @param i determines whether to animate the tiles this frame (1 for moving tiles, 0 for drawing tiles without movement)
     */
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

    /**
     * Determines whether the current level is the last level in the game.
     *
     * @return true if the current level is the last level, false otherwise
     */
    private boolean isLastLevel() {

        //checks if level index is last from levels
        return currentLevelIndex >= levels.size() - 1;
    }
    
    /**
     * Spawns a new ball at a random spawner location with a random velocity and color based on the ball queue.
     * If no spawners or ball colors are available, this method does nothing.
     */
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

    /**
     * Converts a color name to its corresponding numerical value.
     *
     * @param colourName the name of the color
     * @return the numerical value corresponding to the color name
     */
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

    /**
     * Converts a color number to its corresponding color name.
     *
     * @param colorNumber the numerical value of the color
     * @return the name of the color corresponding to the given number
     */
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

    /**
     * Converts a tile character from the level file to its corresponding sprite name.
     *
     * @param tileChar the character representing the tile
     * @param nextChar the next character in the line, used for multi-character tiles
     * @return the sprite name corresponding to the tile character
     */
    private String spriteNameFromChar(char tileChar, char nextChar) {
        
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

    /**
     * Returns the list of Ball objects currently in the game.
     *
     * @return the list of Ball objects
     */
    public List<Ball> getBalls() {
        return balls;
    }
    
    public static void main(String[] args) {
        PApplet.main("inkball.App");
    }

}
