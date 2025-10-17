import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
import javax.swing.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U'; // U D L R
        int velocityX = 0;
        int velocityY = 0;

        Block(Image image, int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                }
            }
        }

        // Set direction without immediately moving (useful when choosing a new direction at tile centers)
        void setDirection(char direction) {
            this.direction = direction;
            updateVelocity();
        }

        void updateVelocity() {
            if (this.direction == 'U') {
                this.velocityX = 0;
                this.velocityY = -tileSize/4;
            }
            else if (this.direction == 'D') {
                this.velocityX = 0;
                this.velocityY = tileSize/4;
            }
            else if (this.direction == 'L') {
                this.velocityX = -tileSize/4;
                this.velocityY = 0;
            }
            else if (this.direction == 'R') {
                this.velocityX = tileSize/4;
                this.velocityY = 0;
            }
        }

        void reset() {
            this.x = this.startX;
            this.y = this.startY;
        }
    }

    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    //X = wall, O = skip, P = pac man, ' ' = food
    //Ghosts: b = blue, o = orange, p = pink, r = red
    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "O       bpo       O",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX" 
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Block> ghosts;
    Block pacman;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'}; //up down left right
    Random random = new Random();
    int score = 0;
    int lives = 3;
    boolean gameOver = false;

    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        //load images
        wallImage = new ImageIcon(getClass().getResource("./wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./redGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./pacmanRight.png")).getImage();

        loadMap();
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(4)];
            ghost.setDirection(newDirection);
        }
        //how long it takes to start timer, milliseconds gone between frames
        gameLoop = new Timer(50, this); //20fps (1000/50)
        gameLoop.start();

    }

    public void loadMap() {
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                String row = tileMap[r];
                char tileMapChar = row.charAt(c);

                int x = c*tileSize;
                int y = r*tileSize;

                if (tileMapChar == 'X') { //block wall
                    Block wall = new Block(wallImage, x, y, tileSize, tileSize);
                    walls.add(wall);
                }
                else if (tileMapChar == 'b') { //blue ghost
                    Block ghost = new Block(blueGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'o') { //orange ghost
                    Block ghost = new Block(orangeGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'p') { //pink ghost
                    Block ghost = new Block(pinkGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'r') { //red ghost
                    Block ghost = new Block(redGhostImage, x, y, tileSize, tileSize);
                    ghosts.add(ghost);
                }
                else if (tileMapChar == 'P') { //pacman
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                }
                else if (tileMapChar == ' ') { //food
                    Block food = new Block(null, x + 14, y + 14, 4, 4);
                    foods.add(food);
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        // Use Graphics2D for nicer effects (overlay, gradients, alpha)
        Graphics2D g2 = (Graphics2D) g.create();

        // Draw world (background scene)
        g2.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        for (Block ghost : ghosts) {
            g2.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
        }

        for (Block wall : walls) {
            g2.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        g2.setColor(Color.WHITE);
        for (Block food : foods) {
            g2.fillRect(food.x, food.y, food.width, food.height);
        }

        // Draw HUD or Game Over overlay
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        if (!gameOver) {
            g2.setColor(Color.WHITE);
            g2.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score), tileSize/2, tileSize/2);
        } else {
            // Dim the whole scene
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
            g2.setColor(new Color(0, 0, 0, 200));
            g2.fillRect(0, 0, boardWidth, boardHeight);

            // Add a soft spotlight (lighting) behind the game over panel
            int cx = boardWidth / 2;
            int cy = boardHeight / 2 - 40;
            int radius = Math.max(boardWidth, boardHeight) / 3;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.18f));
            g2.setPaint(Color.WHITE);
            g2.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);

            // Reset composite for panel drawing
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            // Centered panel with gradient background
            int panelW = Math.min(500, boardWidth - 80);
            int panelH = 220;
            int px = (boardWidth - panelW) / 2;
            int py = (boardHeight - panelH) / 2;
            GradientPaint gp = new GradientPaint(px, py, new Color(60, 60, 60, 230), px, py + panelH, new Color(20, 20, 20, 230));
            g2.setPaint(gp);
            g2.fillRoundRect(px, py, panelW, panelH, 24, 24);

            // Panel border (glow)
            g2.setStroke(new BasicStroke(4f));
            g2.setColor(new Color(255, 200, 60, 200));
            g2.drawRoundRect(px, py, panelW, panelH, 24, 24);

            // Title: GAME OVER
            Font titleFont = new Font("Arial", Font.BOLD, 48);
            g2.setFont(titleFont);
            FontMetrics fm = g2.getFontMetrics();
            String title = "GAME OVER";
            int tx = (boardWidth - fm.stringWidth(title)) / 2;
            int ty = py + 70;
            g2.setColor(Color.WHITE);
            g2.drawString(title, tx, ty);

            // Score text
            Font scoreFont = new Font("Arial", Font.PLAIN, 28);
            g2.setFont(scoreFont);
            fm = g2.getFontMetrics();
            String scoreText = "Final Score: " + score;
            int sx = (boardWidth - fm.stringWidth(scoreText)) / 2;
            int sy = ty + 50;
            g2.setColor(new Color(220, 220, 220));
            g2.drawString(scoreText, sx, sy);

            // Restart hint
            Font hintFont = new Font("Arial", Font.PLAIN, 18);
            g2.setFont(hintFont);
            fm = g2.getFontMetrics();
            String hint = "Press any key to restart";
            int hx = (boardWidth - fm.stringWidth(hint)) / 2;
            int hy = py + panelH - 28;
            g2.setColor(new Color(200, 200, 200, 200));
            g2.drawString(hint, hx, hy);
        }

          g2.dispose();
      }

    public void move() {
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        //check wall collisions
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        // check ghost collisions and move ghosts
        for (Block ghost : ghosts) {
            if (collision(ghost, pacman)) {
                lives -= 1;
                if (lives == 0) {
                    gameOver = true;
                    return;
                }
                resetPositions();
                break;
            }

            // If the ghost is exactly aligned with the tile grid, pick a new direction among valid moves
            boolean atTile = (ghost.x % tileSize == 0) && (ghost.y % tileSize == 0);
            if (atTile) {
                // compute tile-aligned position
                int gx = ghost.x;
                int gy = ghost.y;
                // opposite direction map to avoid immediate backtracking
                char opposite = ' ';
                if (ghost.direction == 'U') opposite = 'D';
                else if (ghost.direction == 'D') opposite = 'U';
                else if (ghost.direction == 'L') opposite = 'R';
                else if (ghost.direction == 'R') opposite = 'L';

                java.util.ArrayList<Character> choices = new java.util.ArrayList<Character>();
                // check each direction for wall presence one tile ahead
                // Up
                if (!isWallAt(gx, gy - tileSize) && opposite != 'U') choices.add('U');
                // Down
                if (!isWallAt(gx, gy + tileSize) && opposite != 'D') choices.add('D');
                // Left
                if (!isWallAt(gx - tileSize, gy) && opposite != 'L') choices.add('L');
                // Right
                if (!isWallAt(gx + tileSize, gy) && opposite != 'R') choices.add('R');

                if (choices.size() == 0) {
                    // no choice except possibly backtracking
                    if (!isWallAt(gx, gy - tileSize)) ghost.setDirection('U');
                    else if (!isWallAt(gx, gy + tileSize)) ghost.setDirection('D');
                    else if (!isWallAt(gx - tileSize, gy)) ghost.setDirection('L');
                    else if (!isWallAt(gx + tileSize, gy)) ghost.setDirection('R');
                    // else stuck — keep current direction
                } else {
                    char pick = choices.get(random.nextInt(choices.size()));
                    ghost.setDirection(pick);
                }
            }

            // move by velocity
            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            // if collisions with walls or world bounds after moving, step back and choose a new valid direction
            boolean bumped = false;
            for (Block wall : walls) {
                if (collision(ghost, wall)) {
                    bumped = true;
                    break;
                }
            }
            if (ghost.x <= 0 || ghost.x + ghost.width >= boardWidth) bumped = true;
            if (bumped) {
                ghost.x -= ghost.velocityX;
                ghost.y -= ghost.velocityY;
                // force choose new direction (allow backtracking now)
                java.util.ArrayList<Character> choices2 = new java.util.ArrayList<Character>();
                if (!isWallAt(ghost.x, ghost.y - tileSize)) choices2.add('U');
                if (!isWallAt(ghost.x, ghost.y + tileSize)) choices2.add('D');
                if (!isWallAt(ghost.x - tileSize, ghost.y)) choices2.add('L');
                if (!isWallAt(ghost.x + tileSize, ghost.y)) choices2.add('R');
                if (!choices2.isEmpty()) {
                    ghost.setDirection(choices2.get(random.nextInt(choices2.size())));
                }
            }
        }

        //check food collision
        Block foodEaten = null;
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foodEaten = food;
                score += 10;
            }
        }
        foods.remove(foodEaten);

        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
        }
    }

    public boolean collision(Block a, Block b) {
        return  a.x < b.x + b.width &&
                a.x + a.width > b.x &&
                a.y < b.y + b.height &&
                a.y + a.height > b.y;
    }

    // Return true if there is a wall occupying the tile at pixel coordinate (x,y)
    private boolean isWallAt(int x, int y) {
        for (Block wall : walls) {
            if (x >= wall.x && x < wall.x + wall.width && y >= wall.y && y < wall.y + wall.height) return true;
        }
        return false;
    }

    public void resetPositions() {
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(4)];
            ghost.updateDirection(newDirection);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }
        // System.out.println("KeyEvent: " + e.getKeyCode());
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            pacman.updateDirection('U');
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            pacman.updateDirection('D');
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            pacman.updateDirection('L');
        }
        else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            pacman.updateDirection('R');
        }

        if (pacman.direction == 'U') {
            pacman.image = pacmanUpImage;
        }
        else if (pacman.direction == 'D') {
            pacman.image = pacmanDownImage;
        }
        else if (pacman.direction == 'L') {
            pacman.image = pacmanLeftImage;
        }
        else if (pacman.direction == 'R') {
            pacman.image = pacmanRightImage;
        }
    }
}