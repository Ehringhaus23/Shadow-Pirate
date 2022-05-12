import bagel.*;
import bagel.Font;
import bagel.Image;
import bagel.Window;
import bagel.util.Point;
import bagel.util.Rectangle;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Skeleton Code for SWEN20003 Project 1, Semester 1, 2022
 *
 * Please filling your name below
 * @author Zixu Zhou
 */

public class ShadowPirate extends AbstractGame {
    private final static int WINDOW_WIDTH = 1024;
    private final static int WINDOW_HEIGHT = 768;
    private final static int WIN_X = 990;
    private final static int WIN_Y = 630;
    private final static int STEP_SIZE = 20;
    private final static int MAX_ENTRIES = 50;
    private final static int NUM_OF_VALUES = 3;
    private final static int SHIP_TOP = 60;
    private final static int SHIP_BOTTOM = 670;
    private final static int HEALTH_X = 20;
    private final static int HEALTH_Y = 35;
    private final static int SAILOR_DAMAGE = 25;
    private final static int SAILOR_MAX_HP = 100;
    private final static int TEXT_Y_POINT = 402;
    private final static int TEXT_Y_BOTTOM = TEXT_Y_POINT + 70;
    private final static String GAME_TITLE = "ShadowPirate";
    private final Image BACKGROUND_IMAGE = new Image("res/background0.png");
    private final Image sailorLeft = new Image("res/sailorLeft.png");
    private final Image sailorRight = new Image("res/sailorRight.png");
    private final Image BLOCK = new Image("res/block.png");
    private final Font FONT = new Font("res/wheaton.otf", 55);
    private final Font HEALTH_FONT = new Font("res/wheaton.otf", 30);
    private final double SAILOR_HEIGHT = sailorLeft.getHeight();
    private final double SAILOR_WIDTH = sailorLeft.getWidth();
    private final double BLOCK_HEIGHT = BLOCK.getHeight();
    private final double BLOCK_WIDTH = BLOCK.getWidth();
    private boolean check = true;
    private boolean sailorState = false;
    private int gameWin = 2;
    private int sailorX = 0;
    private int sailorY = 0;
    private int currentHP = SAILOR_MAX_HP;
    private final int blockDamage = 10;
    private String lastMove;
    private String[][] groupedPoints = new String[MAX_ENTRIES][NUM_OF_VALUES];
    private Rectangle sailorRec = new Rectangle(sailorX - sailorLeft.getWidth()/2, sailorY - sailorLeft.getHeight()/2, sailorLeft.getWidth(), sailorLeft.getHeight());
    private Rectangle[] blocks = new Rectangle[MAX_ENTRIES];
    private double orange[] = {0.9, 0.6, 0};
    private double red[] = {1, 0, 0};
    private double green[] = {0, 0.8, 0.2};
    private ArrayList<String> positions = new ArrayList<String>();
    public ShadowPirate() {
        super(WINDOW_WIDTH, WINDOW_HEIGHT, GAME_TITLE);
    }

    /**
     * The entry point for the program.
     */
    public static void main(String[] args) {
        ShadowPirate game = new ShadowPirate();
        game.run();
    }


    /**
     * Method used to read file and create objects
     */
    private void readCSV(String fileName) throws FileNotFoundException {


        //using a scanner to split the csv file into an ArrayList
        Scanner sc = new Scanner(new File(fileName));
        sc.useDelimiter("\\W");
        while (sc.hasNext()){
            positions.add(sc.next());
        }

        //since the arrayList still has empty space characters, remove them.
        for (int i = positions.size() - 1; i >= 0; i--) {
            if ((i + 1) % (NUM_OF_VALUES + 1) == 0) {
                positions.remove(i);
            }
        }
        transformIntoArray();
        sc.close();
    }

    /**
     * Performs a state update.
     * allows the game to exit when the escape key is pressed.
     */


    //Turns the 1d arraylist into a 2d array.
    public void transformIntoArray(){
        for(int i = 0; i < MAX_ENTRIES ; i++){
            for (int j = 0; j < NUM_OF_VALUES; j++) {
                groupedPoints[i][j] = positions.get(j % 3 + i * 3);
            }
        }
    }

    /**
     * Since sailor is always the first entry, can simply use the first 2 numerical values to get starting location
     * of the sailor.
     */
    public void initializeSailor(){
        sailorX = Integer.valueOf(positions.get(1));
        sailorY = Integer.valueOf(positions.get(2));
    }

    @Override
    public void update(Input input) {
        BACKGROUND_IMAGE.draw(Window.getWidth() / 2.0, Window.getHeight() / 2.0);
        //draws the starting messages centered horizontally until the spacebar is pressed
        if (check == true) {
            FONT.drawString("PRESS SPACE TO START",
                    (Window.getWidth() - FONT.getWidth("PRESS SPACE TO START")) / 2, TEXT_Y_POINT);
            FONT.drawString("USE ARROW KEYS TO FIND LADDER",
                    (Window.getWidth() - FONT.getWidth("USE ARROW KEYS TO FIND LADDER")) / 2, TEXT_Y_BOTTOM);
        }

        //initiate the world when space is pressed
        if (input.wasPressed(Keys.SPACE)) {
            check = false;
            try {
                readCSV("res/level0.csv");
                gameWin = 0;
                initializeSailor();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        if(gameWin == 0){


            //Keeps printing the blocks and respective rectangles
            if (groupedPoints[0][0] != null) {
                drawBlocks(groupedPoints);
            }

            //displays current health on top left
            displayHealth(currentHP);
            updateSailorBox();
            gameWon();
            outOfBounds(sailorX, sailorY);


            //Checks last known left/right movement of sailor to check if sailor should be facing left or right
            if (sailorState == false) {
                sailorRight.draw(sailorX, sailorY);
            } else {
                sailorLeft.draw(sailorX, sailorY);
            }

            //Implementing sailor movement through keys
            if (input.wasPressed(Keys.RIGHT)) {
                sailorState = false;
                moveRight();
                lastMove = "right";

            }

            if (input.wasPressed(Keys.LEFT)) {
                sailorState = true;
                moveLeft();
                lastMove = "left";
            }

            if (input.wasPressed(Keys.UP)) {
                moveUp();
                lastMove = "up";
            }

            if (input.wasPressed(Keys.DOWN)) {
                moveDown();
                lastMove = "down";
            }

            checkCollision();


            if (currentHP <= 0){gameLost();}

            outOfBounds(sailorX, sailorY);

        }
        if(gameWin == -1){
            gameLostMessage();
        }
        if (gameWin == 1){
            gameWonMessage();
        }
        if (input.wasPressed(Keys.ESCAPE)) {
            Window.close();
        }

    }

    //checks if you are on the ladder and thus won the game!
    public void gameWon(){
        if (sailorX >= WIN_X && sailorY >= WIN_Y) {
            gameWin = 1;
        }
    }


    public void gameLost(){
        gameWin = -1;
    }

    public void gameWonMessage(){
        FONT.drawString("CONGRATULATIONS",
                (Window.getWidth() - FONT.getWidth("CONGRATULATIONS")) / 2, TEXT_Y_POINT);
    }

    public void gameLostMessage(){
        FONT.drawString("GAME OVER",
                (Window.getWidth() - FONT.getWidth("GAME OVER")) / 2, TEXT_Y_POINT);
    }


    //This functions displays the current HP in the top left corner of screen
    public void displayHealth(int currentHP){
        double percentHP = percentHealth(currentHP, SAILOR_MAX_HP);
        if (percentHP >= 65) {
            drawHealth(HEALTH_FONT, green, percentHP);
        }
        else if (percentHP < 35){
            drawHealth(HEALTH_FONT, red, percentHP);
        }
        else{
            drawHealth(HEALTH_FONT, orange, percentHP);
        }
    }

    public int percentHealth(int current, int max){
        return Math.round(current * 100 / max);
    }

    public void drawHealth (Font font, double colour[], double percentHealth){
        font.drawString(percentHealth + "%", HEALTH_X, HEALTH_Y,
                new DrawOptions().setBlendColour(colour[0], colour[1], colour[2]));
    }


    // function that checks if the sailor rectangle is intersecting any block and if so, rebound the sailor.
    public void checkCollision(){
        for (int i = 0; i < blocks.length; i++){
            if (blocks[i] != null){
                if (sailorRec.intersects(blocks[i])){
                    currentHP -= blockDamage;
                    if (lastMove.equalsIgnoreCase("right")){
                        moveLeft();
                    }
                    else if (lastMove.equalsIgnoreCase("left")){
                        moveRight();
                    }
                    else if (lastMove.equalsIgnoreCase("up")){
                        moveDown();
                    }
                    else if (lastMove.equalsIgnoreCase("down")){
                        moveUp();
                    }
                    break;
                }
            }
        }
    }


    //This function draws the blocks in, and sets up their rectangles.
    public void drawBlocks(String[][] points) {
        for (int i = 0; i < points.length; i++) {
            if (points[i][0].equalsIgnoreCase("Block")){
                int tempX = Integer.parseInt(points[i][1]);
                int tempY = Integer.parseInt(points[i][2]);
                BLOCK.draw(tempX, tempY);
                Rectangle temp = new Rectangle(topLeftValue(tempX, BLOCK_WIDTH), topLeftValue(tempY, BLOCK_HEIGHT),
                        BLOCK_WIDTH, BLOCK_HEIGHT);
                blocks[i] = temp;
            }
        }
    }

    //Checks if you have gone out of bounds!
    public void outOfBounds(int x, int y){
        if (x > WINDOW_WIDTH || x < 0 || y > SHIP_BOTTOM || y < SHIP_TOP){
            gameLost();
        }
    }


    //Function updates Sailor rectangle at all times after he has moved.
    public void updateSailorBox(){
        Rectangle temp = new Rectangle(topLeftValue(sailorX, SAILOR_WIDTH),
                topLeftValue(sailorY, SAILOR_HEIGHT), sailorLeft.getWidth(), sailorLeft.getHeight());
        sailorRec = temp;
        //System.out.println(sailorX - sailorLeft.getWidth()/2 + " " + sailorLeft.getHeight());
    }

    public void moveLeft(){
        sailorX -= STEP_SIZE;
    }

    public void moveRight(){
        sailorX += STEP_SIZE;
    }

    public void moveUp(){
        sailorY -= STEP_SIZE;
    }

    public void moveDown(){
        sailorY += STEP_SIZE;
    }

    public int topLeftValue(int x, double dimension){
        double topLeftX = (x - (dimension/2));
        return (int)topLeftX;
    }
}
