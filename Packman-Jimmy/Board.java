import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.util.TimerTask;
public class Board extends JPanel implements ActionListener {
    private Dimension d;
    private final Font smallFont = new Font("Helvetica", Font.BOLD, 14);
    private Image ii;
    //red,green,blue
    private final Color dotColor = new Color(192, 192, 0);
    private final Color dotColor2 = new Color(0,0,192);//implemented a second dot color and made it blue to reference the blue dots on the screen(also known as the multiplier)
    private final Color dotColor3 = new Color(192,0,0);//implemeted a third dot color and made it red to reference the red dots on the screen(also known as the ghost eater dot)
    private final Color frozenColor = new Color(0, 150, 150);//initialize a froze dot color to make it seem like the map is frozen when pacman uses his special ability

    private Color mazeColor, mazeColor2;//initialize a second mazeColor for when pacman uses his ability
    private boolean inGame = false;
    private boolean dying = false;
    private boolean dyingGhost = false;
    private final int BLOCK_SIZE = 24;
    private final int N_BLOCKS = 23;//increased the map size to be 23x23
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE;
    private final int PAC_ANIM_DELAY = 2;
    private final int PACMAN_ANIM_COUNT = 4;
    private final int MAX_GHOSTS = 12;
    private int PACMAN_SPEED = 6;

    private int pacAnimCount = PAC_ANIM_DELAY;
    private int pacAnimDir = 1;
    private int pacmanAnimPos = 0;
    private int N_GHOSTS = 6;
    private int pacsLeft, score;
    private int[] dx, dy;
    private int[] ghost_x, ghost_y, ghost_dx, ghost_dy, ghostSpeed;

    private Image ghost;
    private Image blueghost, frozenghost;//initialize a private Image called blueghost & frozenghost that I uploaded in the image file
    private Image pacman1, pacman2up, pacman2left, pacman2right, pacman2down;
    private Image pacman3up, pacman3down, pacman3left, pacman3right;
    private Image pacman4up, pacman4down, pacman4left, pacman4right;

    private int pacman_x, pacman_y, pacmand_x, pacmand_y;
    private int req_dx, req_dy, view_dx, view_dy;

    private int maxNumber = 50;//initialize a maxNumber that pac-man has to reach in order to unlock pacman special ability
    private boolean isFull = false;//initialize isFull to be false because pacman hasn't reached a percentage of 100%
    private int countdown = 0;//created a countdown that initially starts at zero for the multiplier 
    private int countdown2 = 0;//created a second countdown that initially starts at zeros for the ghost eaters
    private int bluePoints = 2;//created a score for the blue points
    private int yellowPoints = 1;//created a score for the yellow points
    private int percentage = 0;//created a variable called percentage & set it to zero that tracks the amount of percentage pacman is to reaching his ability

    private int score2;//initialize a score2 that would keep track of how close this score is to the maxNumber
    private int scoreSpeed = 100;
    private int countdown3 = 0;//Created a third countdown that initially set to zero
    private void countd(){//created a private method called countd that sets the first countdown to 250 when called
      countdown = 250;
    }
    private void countd2(){//created a private method called countd2 that sets the second countdown to 200 when called
      countdown2 = 200;
    }
    private void countd3(){//created a private method called countd3 that sets the third countdown to 350 when called
      countdown3 = 350;
    }
    private void percentageUntilFull(Graphics2D c){//this method basically prints out the percentage of how much pac man needs to score in order to unlock pacman ability. Takes a Graphics2d as a parameter to print out the string that we want
      String k;
      String f;
      if(score2 < maxNumber){//always checks if score2 is less than or equal to maxNumber
        percentage = score2 * 100/ maxNumber;//this basically creates the percentage
        c.setFont(smallFont);
        c.setColor(new Color(96, 128, 255));
        f = "Percentage: " + percentage + "%";
        c.drawString(f, 400, SCREEN_SIZE + 16);
      }else{//if score2 is greater than maxNumber it will do the following
        isFull = true;//set isFull to be True
        c.setFont(smallFont);
        c.setColor(new Color(96, 128, 255));
        k = "Press Space for special ability!";//And it will print the following on the screen
        c.drawString(k, 400, SCREEN_SIZE + 16);
      }
    }

    private void multiplier(Graphics2D j){//this multiplier takes a grahpics2d as a parameter in order to output the multiplier onto the screen
      if(countdown > 0){//this if statements is a boundary for the countdown so the countdown can only go down one if the countdown is initially greater than 0
        countdown--;
      }
      if(countdown2 > 0){//same thing for the second countdown
        countdown2--;
      }
      String a;//intialize a string called a
      if(countdown != 0){//this if statements always checks if the first countdown is not equal to zero and if true it will do the following below
        j.setFont(smallFont);
        j.setColor(new Color(96, 128, 255));
        a = "Multiplier 2x!!";        //Basically this entire code inside this if statement will print out a multiplier on the screen so the user/player will know that a multiplier is present
        j.drawString(a, 170, SCREEN_SIZE + 16);
        bluePoints = 4;//since it's a multiplier it will double the points for the original blue dots whenever pac-man eats it`
        yellowPoints = 2;//same for the yellow points
      }else{
        bluePoints = 2; //else it will set the dots to their originial points
        yellowPoints = 1;
      }
    }

    /**
    These numbers make up the maze. They provide information out of which we create the corners and the points. Number 1 is a left corner. Numbers 2, 4 and 8 represent top, right, and bottom corners respectively. Number 16 is a point. These numbers can be added, for example number 19 in the upper left corner means that the square will have top and left borders and a point (16 + 2 + 1).
    **/

    //changed the map up a little bit made it bigger and created different shapes present on the screen
    private final short levelData[] = {
        19, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22, 6, 
        17, 16, 16, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 16, 16, 16, 16, 16, 16, 16, 20, 4,
        17, -32, 20, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 17, 16, 16, 24, -16, 16, 16, 20, 4,
        17, 16, 20, 0, 19, 18, 18, 18, 18, 18, 18, 18, 22, 0, 17, 16, 20, 0, 17, 16, 16, 20, 4,
        17, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, -32, 20, 0, 17, 16, 20, 0, 17, 16, 16, 20, 4,
        17, 16, 20, 0, 17, 16, -32, 16, 16, 24, 16, 16, 20, 0, 17, -32, 20, 0, 17, -32, 16, 20, 4,
        17, 16, 20, 0, 17, 16, 16, 16, 20, 0, 17, 16, 20, 0, 17, 16, 20, 0, 17, 16, 16, 16, 22,
        17, 16, 16, 18, 16, 16, 16, 16, 20,0, 17, 16, 16, 18, 16, 16, 20, 0, 17, 16, 16, 16, 20,
        17, 16, 16, 16, 16, 16, 24, 24, 28,0, 25, 24, 24, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20,
          25, 16, 16, 16, 16, 20, 0, 0, 0, 0, 0, 0, 0, 17, 16, 16, 20, 0, 25, 24, 16, -32, 20,
        1, 17, 16, 16, 16, 16, 18, 18, 22, 0, 19, 18, 18, 16, 16, -32, 20, 0, 0, 0, 17, 16, 20,
        1, 17, 16, -32, 16, 16, 16, 16, 20,0, 17, 16, 16, 16, 16, 16, 16, 18, 18, 18, 16, 16, 28,
        1, 17, 16, 16, 24, 16, 16, 16, 20, 0, 17, 16, 16, 16, 16, -16, 16, 16, 16, 16, 16, 20, 4,
        1, 17, 16, 20, 0, 17, 16, -32, 16, 18, 16, 16, -32, 16, 16, 16, 16, 16, 16, 16, 16, 20, 4,
        1, 17, 16, 20, 0, 17, 16, 16, 24, 24, 24, 24, 24, 24, 24, -32, 16, 16, 16, 16, 16, 20, 4,
        19, 16, 16, 20, 0, 17, 16, 20, 0, 0, 0, 0, 0, 0, 0, 17, 16, 16, 16, 16, 16, 20, 12,
        17, 16, 16, 20, 0, 17, 16, 16, 18, 18, 22, 0, 19, 18, 18, 16, 16, 16, 16, 16, 16, -32, 22,
        17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 16, 24, 24, 24, 24, 24, 28,
        17, -32, 16, 20, 0, 17, 16, 16, 16, 16, 20, 0, 17, 16, 16, 16, 20, 0, 0, 0, 0, 0, 4,
        17, 16, 24, 28, 0, 17, 16, 24, 24, 24, 24, 18, 16, 16, 16, 16, 20, 0, 3, 2, 2, 2, 6,
        17, 20, 0, 0, 0, 17, 20, 0, 0, 0, 0, 17, 16, 16, 16, 16, 20, 0, 1, 0, 0, 0, 4,
        17, -16, 18, 18, 18, 16, 20, 0, 0, 0, 0, 17, -32, 16, 16, 16, 20, 0, 1, 0, 0, 0, 4,
        25, 24, 24, 24, 24, 24, 28, 8, 8, 8, 8, 25, 24, 24, 24, 24, 28, 8, 9, 8, 8, 8, 12
    };

    private final int validSpeeds[] = {1, 2, 3, 4, 6, 8};
    private final int maxSpeed = 8;

    private int currentSpeed = 4;
    private short[] screenData;
    private Timer timerr;

    public Board() {

        loadImages();
        initVariables();
        initBoard();
    }
    
    private void initBoard() {
        
        addKeyListener(new TAdapter());

        setFocusable(true);

        setBackground(Color.black);
    }

    private void initVariables() {

        screenData = new short[N_BLOCKS * N_BLOCKS];
          mazeColor2 = new Color(0, 100, 100);//sets the mazeColor to look like a frozen kind of color
          mazeColor = new Color(5, 100, 5);

        d = new Dimension(900, 900);//Changed the dimension so it would be big enough to fit the newly sized map
        ghost_x = new int[MAX_GHOSTS];
        ghost_dx = new int[MAX_GHOSTS];
        ghost_y = new int[MAX_GHOSTS];
        ghost_dy = new int[MAX_GHOSTS];
        ghostSpeed = new int[MAX_GHOSTS];
        dx = new int[4];
        dy = new int[4];
        
        timerr = new Timer(40, this);
        timerr.start();
    }

    @Override
    public void addNotify() {
        super.addNotify();

        initGame();
    }

    private void doAnim() {

        pacAnimCount--;

        if (pacAnimCount <= 0) {
            pacAnimCount = PAC_ANIM_DELAY;
            pacmanAnimPos = pacmanAnimPos + pacAnimDir;

            if (pacmanAnimPos == (PACMAN_ANIM_COUNT - 1) || pacmanAnimPos == 0) {
                pacAnimDir = -pacAnimDir;
            }
        }
    }

    private void playGame(Graphics2D g2d) {
        if (dying) {

            death();

        } else {
            percentageUntilFull(g2d);//add percentageUntilFull here so it can always update the percentage as well as printing the stuff on the sreen
            multiplier(g2d);//added the multiplier in playGame method so it would constantly check if there is a mulitplier or not
            movePacman();
            drawPacman(g2d);
            moveGhosts(g2d);
            checkMaze();
        }
    }

    private void showIntroScreen(Graphics2D g2d) {

        g2d.setColor(new Color(0, 32, 48));
        g2d.fillRect(50, SCREEN_SIZE / 2 - 30, SCREEN_SIZE - 100, 50);
        g2d.setColor(Color.white);
        g2d.drawRect(50, SCREEN_SIZE / 2 - 30, SCREEN_SIZE - 100, 50);

        String s = "Press s to start.";
        Font small = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = this.getFontMetrics(small);

        g2d.setColor(Color.white);
        g2d.setFont(small);
        g2d.drawString(s, (SCREEN_SIZE - metr.stringWidth(s)) / 2, SCREEN_SIZE / 2);


    }

    private void drawScore(Graphics2D g) {

        int i;
        String s;

        g.setFont(smallFont);
        g.setColor(new Color(96, 128, 255));
        s = "Score: " + score;
        g.drawString(s, SCREEN_SIZE / 2 + 18, SCREEN_SIZE + 16);
        for (i = 0; i < pacsLeft; i++) {
            g.drawImage(pacman3left, i * 28 + 8, SCREEN_SIZE + 1, this);
        }
    }

    private void checkMaze() {

        short i = 0;
        boolean finished = true;

        while (i < N_BLOCKS * N_BLOCKS && finished) {

            if ((screenData[i] & 48) != 0) {
                finished = false;
            }

            i++;
        }

        if (finished) {
            score += 50;
            score2 += 50;//Basically whatever points the original score gets, score2 will get the same thing as well
            if(pacsLeft > 0){//Made it so whenever pac man finished eating all of the dot, it will reset the dots but also give pac-man an extra life
              pacsLeft++;
            }
            if (N_GHOSTS < MAX_GHOSTS) {
                N_GHOSTS++;
            }
            
            if (currentSpeed < maxSpeed) {
                currentSpeed++;
            }

            initLevel();
        }
    }

    private void death() {

        pacsLeft--;

        if (pacsLeft == 0) {
            inGame = false;
        }

        continueLevel();
    }     

    private void moveGhosts(Graphics2D g2d) {

        short i;
        int pos;
        int count;
        if(countdown3 > 0){//it will always check if countdown3 is greater than zero to start the countdown
          countdown3--;
        }
        for (i = 0; i < N_GHOSTS; i++) {
            if (ghost_x[i] % BLOCK_SIZE == 0 && ghost_y[i] % BLOCK_SIZE == 0) {
                pos = ghost_x[i] / BLOCK_SIZE + N_BLOCKS * (int) (ghost_y[i] / BLOCK_SIZE);

                count = 0;

                if ((screenData[pos] & 1) == 0 && ghost_dx[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 2) == 0 && ghost_dy[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }

                if ((screenData[pos] & 4) == 0 && ghost_dx[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 8) == 0 && ghost_dy[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                if(countdown3 != 0){//checks if countdown is initially not zero
                  ghost_dx[i] = 0;//if true it will set all of the direction of the ghost to zero until this statement becomes false
                  ghost_dy[i] = 0;
                }else{//this is the original code that makes the ghost move randomly but was added into a else statement
                  if (count == 0) {

                      if ((screenData[pos] & 15) == 15) {
                          ghost_dx[i] = 0;
                          ghost_dy[i] = 0;
                      } else {
                          ghost_dx[i] = -ghost_dx[i];
                          ghost_dy[i] = -ghost_dy[i];
                      }

                  } else {

                      count = (int) (Math.random() * count);

                      if (count > 3) {
                          count = 3;
                      }

                      ghost_dx[i] = dx[count];
                      ghost_dy[i] = dy[count];
                  }
                }

            }
            ghost_x[i] = ghost_x[i] + (ghost_dx[i] * ghostSpeed[i]);
            ghost_y[i] = ghost_y[i] + (ghost_dy[i] * ghostSpeed[i]);
            drawGhost(g2d, ghost_x[i] + 1, ghost_y[i] + 1);

            if (pacman_x > (ghost_x[i] - 12) && pacman_x < (ghost_x[i] + 12)
                    && pacman_y > (ghost_y[i] - 12) && pacman_y < (ghost_y[i] + 12)
                    && inGame && (countdown2 != 0 || countdown3 != 0)) {//This code is basically similar to the original code but instead it also checks if the countdown2 or countdown 3 is not equal to 0. It checks for the coordinates of the ghost and pac-man and if they are in the same coordinate(meaning the pac-man has touched or ate the ghost and the countdown2 is still ongoing or not equal to zero) it will mean that the ghost is a different color which would let the pac-man eat the ghost without dying
                ghost_x[i] = 21 * BLOCK_SIZE;//changes the ghost position when pac-man eats the ghost
                ghost_y[i] = 20 * BLOCK_SIZE;
                score+=25;//adds 25 points to the score if pac-man is able to do this
                score2+=25;
                               
            }else if(pacman_x > (ghost_x[i] - 12) && pacman_x < (ghost_x[i] + 12)
                    && pacman_y > (ghost_y[i] - 12) && pacman_y < (ghost_y[i] + 12)
                    && inGame){//this is the original code and basically the ghost is it's original color and if pac-man touches it, pac-man will die
                      dying = true;
            }
        }
    }

    private void drawGhost(Graphics2D g2d, int x, int y) {//In this method I changed it so the ghost would turn into a different image so in this case a blue ghost if the countdown2 is not equal to zero meaning the pac-man has eaten the red dot.
        if(countdown2 != 0){
          g2d.drawImage(blueghost, x, y, this);
        }else if(countdown3 != 0){//checks if countdown3 is active
          g2d.drawImage(frozenghost, x, y, this);//if true it will draw the frozenghost image
        }else{//else the ghost would turn into it's original image
          g2d.drawImage(ghost, x, y, this);
        }
    }
    
    private void movePacman() {

        int pos;
        short ch;

        if (req_dx == -pacmand_x && req_dy == -pacmand_y) {
            pacmand_x = req_dx;
            pacmand_y = req_dy;
            view_dx = pacmand_x;
            view_dy = pacmand_y;
        }

        if (pacman_x % BLOCK_SIZE == 0 && pacman_y % BLOCK_SIZE == 0) {
            pos = pacman_x / BLOCK_SIZE + N_BLOCKS * (int) (pacman_y / BLOCK_SIZE);
            ch = screenData[pos];

            if ((ch & 16) != 0) {//in this case the number 16 is referred to as the yellow dot, and this checks if pac-man has eaten the yellow dot than it will do the following
                screenData[pos] = (short) (ch & 15);
                score += yellowPoints;//instead of manually adding 1 each time it would add the instance variable yellowPoints so whenever multiplier is on, it would also change the points of the yellow dot
                score2 += yellowPoints;
            }

            if (ch == -32) {//the number -32 represents the blue dots and this checks if pacman has eaten the blue dot than it would do the following
                screenData[pos] = (short) (ch & 15);
                score += bluePoints;//same thing with the yellowPoint
                score2 += bluePoints;

                countd();//calls the method countd whenever pac man eats the blue dot
            }
            if(ch == -16){// this number -16 representst eh red dots on the screen and this checks if pac has eaten the red dot than it would do the following
              screenData[pos] = (short) (ch & 15);
              countd2();//instead of adding points when eating this dot, it will call the method countd2
            }
            if (req_dx != 0 || req_dy != 0) {
                if (!((req_dx == -1 && req_dy == 0 && (ch & 1) != 0)
                        || (req_dx == 1 && req_dy == 0 && (ch & 4) != 0)
                        || (req_dx == 0 && req_dy == -1 && (ch & 2) != 0)
                        || (req_dx == 0 && req_dy == 1 && (ch & 8) != 0))) {
                    pacmand_x = req_dx;
                    pacmand_y = req_dy;
                    view_dx = pacmand_x;
                    view_dy = pacmand_y;
                }
            }

            // Check for standstill
            if ((pacmand_x == -1 && pacmand_y == 0 && (ch & 1) != 0)
                    || (pacmand_x == 1 && pacmand_y == 0 && (ch & 4) != 0)
                    || (pacmand_x == 0 && pacmand_y == -1 && (ch & 2) != 0)
                    || (pacmand_x == 0 && pacmand_y == 1 && (ch & 8) != 0)) {
                pacmand_x = 0;
                pacmand_y = 0;
            }
        }
        pacman_x = pacman_x + PACMAN_SPEED * pacmand_x;
        pacman_y = pacman_y + PACMAN_SPEED * pacmand_y;
    }

    private void drawPacman(Graphics2D g2d) {

        if (view_dx == -1) {
            drawPacnanLeft(g2d);
        } else if (view_dx == 1) {
            drawPacmanRight(g2d);
        } else if (view_dy == -1) {
            drawPacmanUp(g2d);
        } else {
            drawPacmanDown(g2d);
        }
    }

    private void drawPacmanUp(Graphics2D g2d) {

        switch (pacmanAnimPos) {
            case 1:
                g2d.drawImage(pacman2up, pacman_x + 1, pacman_y + 1, this);
                break;
            case 2:
                g2d.drawImage(pacman3up, pacman_x + 1, pacman_y + 1, this);
                break;
            case 3:
                g2d.drawImage(pacman4up, pacman_x + 1, pacman_y + 1, this);
                break;
            default:
                g2d.drawImage(pacman1, pacman_x + 1, pacman_y + 1, this);
                break;
        }
    }

    private void drawPacmanDown(Graphics2D g2d) {

        switch (pacmanAnimPos) {
            case 1:
                g2d.drawImage(pacman2down, pacman_x + 1, pacman_y + 1, this);
                break;
            case 2:
                g2d.drawImage(pacman3down, pacman_x + 1, pacman_y + 1, this);
                break;
            case 3:
                g2d.drawImage(pacman4down, pacman_x + 1, pacman_y + 1, this);
                break;
            default:
                g2d.drawImage(pacman1, pacman_x + 1, pacman_y + 1, this);
                break;
        }
    }

    private void drawPacnanLeft(Graphics2D g2d) {

        switch (pacmanAnimPos) {
            case 1:
                g2d.drawImage(pacman2left, pacman_x + 1, pacman_y + 1, this);
                break;
            case 2:
                g2d.drawImage(pacman3left, pacman_x + 1, pacman_y + 1, this);
                break;
            case 3:
                g2d.drawImage(pacman4left, pacman_x + 1, pacman_y + 1, this);
                break;
            default:
                g2d.drawImage(pacman1, pacman_x + 1, pacman_y + 1, this);
                break;
        }
    }

    private void drawPacmanRight(Graphics2D g2d) {

        switch (pacmanAnimPos) {
            case 1:
                g2d.drawImage(pacman2right, pacman_x + 1, pacman_y + 1, this);
                break;
            case 2:
                g2d.drawImage(pacman3right, pacman_x + 1, pacman_y + 1, this);
                break;
            case 3:
                g2d.drawImage(pacman4right, pacman_x + 1, pacman_y + 1, this);
                break;
            default:
                g2d.drawImage(pacman1, pacman_x + 1, pacman_y + 1, this);
                break;
        }
    }

    private void drawMaze(Graphics2D g2d) {
        short i = 0;
        int x, y;
        
        for (y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
            for (x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {
                if(countdown3 != 0){//inside the draw maze, this if statement will check if the countdown3 is active
                  g2d.setColor(mazeColor2);//if true it will set the maze color to a frozen color in the background
                }else{//else it will set the maze color to its original color
                  g2d.setColor(mazeColor);
                }
                g2d.setStroke(new BasicStroke(2));

                if ((screenData[i] & 1) != 0) { 
                    g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 2) != 0) { 
                    g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y);
                }

                if ((screenData[i] & 4) != 0) { 
                    g2d.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }
                                                             
                if ((screenData[i] & 8) != 0) { 
                    g2d.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }
                if ((screenData[i] & 16) != 0) { 
                    if(countdown3 != 0){//this if statement will do the same thing as the mazecolor but instead it changes the color of the yellow dots to look like a frozen color whenever countdown3 is active
                      g2d.setColor(frozenColor);
                      g2d.fillRoundRect(x + 11, y + 11, 6, 6, 7, 7);
                    }else{//else it will set the dotcolor to its original color
                      g2d.setColor(dotColor);
                      g2d.fillRoundRect(x + 11, y + 11, 6, 6, 7, 7);
                    }
                }
                if (screenData[i] == -32) { //this creates the dot color and the size of the bluedot so in this case I made it 10x10 on the screen
                      g2d.setColor(dotColor2);//sets the color to the instance variable dotColor2 which is blue
                      g2d.fillRoundRect(x + 8, y + 8, 12, 12, 12, 12);//fills the shape in and x & y determines where the dot would be positioned at
                }
                if (screenData[i] == -16) { //this creates the dot color and size of the reddot so in this case I made it the same as the one above
                      g2d.setColor(dotColor3);//sets teh color to the instance variable dotColor3 which is red
                      g2d.fillRoundRect(x + 8, y + 8, 12, 12, 12, 12);//does the same thing above
                }
                i++;
            }
        }
    }

    private void initGame() {

        pacsLeft = 5;//changed it so when starting up the game, pac man will initially have 5 lives instead of 3
        score = 0;
        score2 = 0;
        initLevel();
        N_GHOSTS = 6;
        currentSpeed = 3;
    }

    private void initLevel() {

        int i;
        for (i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
            screenData[i] = levelData[i];
        }

        continueLevel();
    }

    private void continueLevel() {

        short i;
        int dx = 1;
        int random;

        for (i = 0; i < N_GHOSTS; i++) {

            ghost_y[i] = 4 * BLOCK_SIZE;
            ghost_x[i] = 4 * BLOCK_SIZE;
            ghost_dy[i] = 0;
            ghost_dx[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (currentSpeed + 1));

            if (random > currentSpeed) {
                random = currentSpeed;
            }

            ghostSpeed[i] = validSpeeds[random];
        }

        pacman_x = 13 * BLOCK_SIZE;//change the coordinates of where pac man will start off at since I changed the map as well. This controls the x-axis of the pac-man
        pacman_y = 22 * BLOCK_SIZE;//this controls the y-axis of the pac-man
        pacmand_x = 0;
        pacmand_y = 0;
        req_dx = 0;
        req_dy = 0;
        view_dx = -1;
        view_dy = 0;
        dying = false;
    }
  
    private void loadImages() {

        ghost = new ImageIcon("images/ghost.png").getImage();
        blueghost = new ImageIcon("images/blueghost.png").getImage();//imported a new image called blueghost
        frozenghost = new ImageIcon("images/frozenghost.png").getImage();//imported a new image called frozenghost
        pacman1 = new ImageIcon("images/pacman.png").getImage();
        pacman2up = new ImageIcon("images/up1.png").getImage();
        pacman3up = new ImageIcon("images/up2.png").getImage();
        pacman4up = new ImageIcon("images/up3.png").getImage();
        pacman2down = new ImageIcon("images/down1.png").getImage();
        pacman3down = new ImageIcon("images/down2.png").getImage();
        pacman4down = new ImageIcon("images/down3.png").getImage();
        pacman2left = new ImageIcon("images/left1.png").getImage();
        pacman3left = new ImageIcon("images/left2.png").getImage();
        pacman4left = new ImageIcon("images/left3.png").getImage();
        pacman2right = new ImageIcon("images/right1.png").getImage();
        pacman3right = new ImageIcon("images/right2.png").getImage();
        pacman4right = new ImageIcon("images/right3.png").getImage();

    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        doDrawing(g);
    }

    private void doDrawing(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.black);
        g2d.fillRect(0, 0, d.width, d.height);

        drawMaze(g2d);
        drawScore(g2d);
        doAnim();

        if (inGame) {
            playGame(g2d);
        } else {
            showIntroScreen(g2d);
        }

        g2d.drawImage(ii, 5, 5, this);
        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }

    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (inGame) {
                if (key == KeyEvent.VK_LEFT) {
                    req_dx = -1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_RIGHT) {
                    req_dx = 1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_UP) {
                    req_dx = 0;
                    req_dy = -1;
                } else if (key == KeyEvent.VK_DOWN) {
                    req_dx = 0;
                    req_dy = 1;
                } else if (isFull && key == KeyEvent.VK_SPACE) {//this else if allows the pacman to use its special ability if the boolean isFull is true and the user has pressed space
                    isFull = false;//it will set isFull to be false again
                    score2 = 0;//set the score2 to be zero so the percentage will reset as well
                    countd3();//call the method countd3 which sets countdown3 to now be 350
                    maxNumber = maxNumber += 50;//made it so each time pacman unlocks his ability he has to get 50 more points than what he originally got so it makes it harder each time to reach his ability
                } else if (key == KeyEvent.VK_ESCAPE && timerr.isRunning()) {
                    inGame = false;
                } else if (key == KeyEvent.VK_PAUSE) {
                    if (timerr.isRunning()) {
                        timerr.stop();
                    } else {
                        timerr.start();
                    }
                }
            } else {
                if (key == 's' || key == 'S') {
                    inGame = true;
                    initGame();
                }
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {

            int key = e.getKeyCode();

            if (key == Event.LEFT || key == Event.RIGHT
                    || key == Event.UP || key == Event.DOWN) {
                req_dx = 0;
                req_dy = 0;
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        repaint();
    }
}
