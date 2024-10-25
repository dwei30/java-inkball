## Inkball game in Java

## What is the InkBall Game?
This project is a replica of the Inkball game found in older versions of Windows, developed in Java using the Processing library for graphics. The game involves directing coloured balls into holes of matching colour using player-drawn lines. If a ball enters a wrong hole, the score is penalized, and the ball respawns. The objective is to capture all the balls by guiding them to the correct holes.

## Features:
- **Ball Spawning**: Balls spawn with random velocities.
- **Line Drawing**: Players draw lines that reflect balls.
- **Collision Detection**: Detects collisions with walls and player lines.
- **Scoring**: Adjusts score based on ball and hole colour matching.
- **Timer & Levels**: Includes a timer for each level and support for multiple levels.
- **Win/Loss Conditions**: Conditions are based on ball capture and time expiration.
- **Pause/Restart**: Functionality to pause and restart the game.
- **Animated Yellow Tiles**: Yellow tiles move along the game board after level completion.

## Software Requirements:
- Java 8
- Gradle
- Processing 4.0b1 (core and data libraries)
- JSON library for configuration handling

## How to Run

#### Requirements
- Java 8
- Gradle

To run the game, build the project using Gradle and launch it in an environment compatible with Processing.

```bash
gradle build
gradle run
