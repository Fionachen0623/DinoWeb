import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class DinoGame extends JFrame {
    private GamePanel gamePanel;

    public DinoGame() {
        setTitle("小恐龙游戏");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        gamePanel = new GamePanel();
        add(gamePanel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        new Thread(gamePanel).start();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (!gamePanel.isSpacePressed()) {
                        if (gamePanel.isGameOver()) {
                            gamePanel.restart();
                        } else {
                            gamePanel.jump();
                        }
                        gamePanel.setSpacePressed(true);
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    System.exit(0);
                }
                else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    gamePanel.getDino().setSpeed(5); // 右加速
                } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                    gamePanel.getDino().setSpeed(-5); // 左加速
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    gamePanel.setSpacePressed(false);
                }else if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_LEFT) {
                    gamePanel.getDino().setSpeed(0); // 松开左右键停止移动
                }
            }
        });
    }

    public static void main(String[] args) {
        new DinoGame();
    }
}

class GamePanel extends JPanel implements Runnable {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 300;
    private static final int GROUND_Y = 220;

    private Dino dino;
    private ArrayList<Obstacle> obstacles;
    private ArrayList<FallingObstacle> fallingObstacles; // 添加掉落障碍物列表
    private Random random;
    private int score;
    private boolean gameOver;
    private int obstacleSpawnTimer;
    private int fallingSpawnTimer; // 添加掉落障碍物计时器
    private boolean spacePressed = false;
    
    // 课程障碍物类型定义
    public static final int TYPE_CS101 = 2;        // Python
    public static final int TYPE_CS201 = 3;        // Java
    public static final int TYPE_MATH105 = 4;      // Calculus
    public static final int TYPE_STATS211 = 5;     // Stochastic Processes
    public static final int TYPE_COMPSCI203 = 6;   // Discrete Math
    public static final int TYPE_MATH202 = 7;      // Linear Algebra
    public static final int TYPE_STATS302 = 8;     // Machine Learning
    public static final int TYPE_MATH206 = 9;      // Probability & Statistics
    public static final int TYPE_COMPSCI304 = 10;  // Speech Recognition
    public static final int TYPE_SYNTAX_ERROR = 11; // Syntax Error

    public boolean isSpacePressed() {
        return spacePressed;
    }

    public void setSpacePressed(boolean pressed) {
        this.spacePressed = pressed;
    }
    public Dino getDino() { return dino; }

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.WHITE);

        dino = new Dino(50, GROUND_Y);
        obstacles = new ArrayList<>();
        fallingObstacles = new ArrayList<>(); // 初始化掉落障碍物列表
        random = new Random();
        score = 0;
        gameOver = false;
        obstacleSpawnTimer = 0;
        fallingSpawnTimer = 0; // 初始化掉落障碍物计时器
    }

    public void jump() {
        if (!dino.isJumping() && !gameOver) {
            dino.jump();
        }
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void restart() {
        dino = new Dino(50, GROUND_Y);
        obstacles.clear();
        fallingObstacles.clear(); // 清空掉落障碍物
        score = 0;
        gameOver = false;
        obstacleSpawnTimer = 0;
        fallingSpawnTimer = 0; // 重置掉落障碍物计时器
    }
    
    private String getLetterGrade() {
        if (score >= 250) {
            return "A+";
        } else if (score >= 30) {
            return "A";
        } else if (score >= 28) {
            return "A-";
        } else if (score >= 25) {
            return "B+";
        } else if (score >= 23) {
            return "B";
        } else if (score >= 20) {
            return "B-";
        } else if (score >= 18) {
            return "C+";
        } else if (score >= 15) {
            return "C";
        } else if (score >= 13) {
            return "C-";
        } else if (score >= 10) {
            return "D+";
        } else if (score >= 8) {
            return "D";
        } else if (score >= 5) {
            return "D-";
        } else { 
            return "F";
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.GRAY);
        g.fillRect(0, GROUND_Y + 50, WIDTH, HEIGHT - (GROUND_Y + 50));

        dino.draw(g);

        for (Obstacle obs : obstacles) {
            obs.draw(g);
        }
        
        // 绘制掉落障碍物
        for (FallingObstacle fObs : fallingObstacles) {
            fObs.draw(g);
        }

        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        g.drawString("Score: " + score, 20, 30);
        g.drawString("Letter Grade: " + getLetterGrade(), 200, 30);

    if (gameOver) {
        g.setColor(Color.WHITE);
        g.fillRoundRect(WIDTH / 2 - 200, HEIGHT / 2 - 100, 400, 200, 20, 20);
    
        g.setColor(Color.BLACK);
        g.drawRoundRect(WIDTH / 2 - 200, HEIGHT / 2 - 100, 400, 220, 20, 20);
    
        g.setColor(new Color(0, 100, 0)); 
        g.setFont(new Font("SansSerif", Font.BOLD, 30));
        int titleY = HEIGHT / 2 - 40; 
        g.drawString("Game Over!", WIDTH / 2 - 100, titleY);

        g.setColor(new Color(0, 0, 100)); 
        g.setFont(new Font("SansSerif", Font.PLAIN, 24));
        g.drawString("Score: " + score, WIDTH / 2 - 150, HEIGHT / 2 + 20); 
        g.drawString("Grade: " + getLetterGrade(), WIDTH / 2 - 150, HEIGHT / 2 + 60);
    
        g.setColor(new Color(0, 0, 100)); 
        g.setFont(new Font("SansSerif", Font.ITALIC, 20));
        g.drawString("Press the space bar to restart!", WIDTH / 2 - 120, HEIGHT / 2 + 100);
}
    
    }

    @Override
    public void run() {
        while (true) {
            if (!gameOver) {
                dino.update();

                obstacleSpawnTimer++;
                
                // 随着分数增加，生成频率提高（最小值为30）
                int spawnRate = Math.max(120 - score/3, 30);
                
                if (obstacleSpawnTimer >= spawnRate) { 
                    int obstacleType;
                    
                    int rand = random.nextInt(10);
                    
                    if (rand < 2) { // 20%概率：原有仙人掌
                        obstacleType = random.nextInt(2);
                    } else if (rand < 5) { // 30%概率：语法错误bug
                        obstacleType = TYPE_SYNTAX_ERROR;
                    } else { // 50%概率：其他课程障碍物
                        // 调整课程出现时机：3分开始出现高阶课程，8分进入下一阶段
                        if (score < 3) {
                            // 初始阶段：基础课程
                            obstacleType = random.nextInt(2) + 2; // 2-3 (CS101, CS201)
                        } else if (score < 8) {
                            // 3分后：开始出现中等难度课程
                            obstacleType = random.nextInt(5) + 2; // 2-6
                        } else {
                            // 8分后：所有高阶课程都可能出现
                            obstacleType = random.nextInt(9) + 2; // 2-10
                        }
                    }
                    
                    obstacles.add(new Obstacle(WIDTH, GROUND_Y, obstacleType));
                    obstacleSpawnTimer = 0;
                }

                for (int i = 0; i < obstacles.size(); i++) {
                    Obstacle obs = obstacles.get(i);
                    obs.update();

                    if (obs.getX() + obs.getWidth() < 0) {
                        obstacles.remove(i);
                        i--;
                        score++;
                    }
                }
                
                // 掉落障碍物生成
                fallingSpawnTimer++;
                if (fallingSpawnTimer >= 150) {
                    int xPos = random.nextInt(WIDTH - 30);
                    fallingObstacles.add(new FallingObstacle(xPos, -30));
                    fallingSpawnTimer = 0;
                }

                // 更新掉落障碍物
                for (int i = 0; i < fallingObstacles.size(); i++) {
                    FallingObstacle fObs = fallingObstacles.get(i);
                    fObs.update();
                    if (fObs.getY() > HEIGHT) {
                        fallingObstacles.remove(i);
                        i--;
                    }
                }

                // 碰撞检测
                for (Obstacle obs : obstacles) {
                    if (dino.collidesWith(obs)) {
                        gameOver = true;
                        break;
                    }
                }
                
                // 检测与掉落障碍物的碰撞
                for (FallingObstacle fObs : fallingObstacles) {
                    if (dino.collidesWith(fObs)) {
                        gameOver = true;
                        break;
                    }
                }
            }

            repaint();

            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Dino {
    private int x, y;
    private int width = 50;
    private int height = 60;
    private int jumpSpeed;
    private boolean jumping;
    private int speed = 0; // 添加速度变量
    private static final int GRAVITY = 1;

    public Dino(int x, int y) {
        this.x = x;
        this.y = y;
        this.jumpSpeed = 0;
        this.jumping = false;
    }

    public void jump() {
        jumping = true;
        jumpSpeed = -18;
    }

    public void update() {
        if (jumping) {
            y += jumpSpeed;
            jumpSpeed += GRAVITY;

            if (y >= 220) {
                y = 220;
                jumping = false;
                jumpSpeed = 0;
            }
        }
        x += speed;
        if (x < 0) x = 0;
        if (x > 750) x = 750;
    }
    
    public void setSpeed(int speed) { this.speed = speed; }

    public void draw(Graphics g) {
        // 身体
        g.setColor(new Color(0, 102, 204));
        g.fillRect(x, y + 20, width, height - 20);

        // T恤文字
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("DKU", x + 8, y + 50);

        // 头
        int headWidth = width - 12;
        int headX = x + (width - headWidth) / 2;
        int headY = y - 10;
        g.setColor(new Color(0, 153, 0));
        g.fillRect(headX, headY, headWidth, 30);

        // 眼睛
        g.setColor(Color.WHITE);
        g.fillOval(headX + 5, headY + 8, 10, 10);
        g.fillOval(headX + headWidth - 15, headY + 8, 10, 10);

        g.setColor(Color.BLACK);
        g.fillOval(headX + 8, headY + 10, 5, 5);
        g.fillOval(headX + headWidth - 12, headY + 10, 5, 5);

        // 手臂
        g.setColor(new Color(0, 153, 0));
        g.fillRect(x - 10, y + 25, 10, 10);
        g.fillRect(x + width, y + 25, 10, 10);

        // 腿
        g.fillRect(x + 5, y + height, 10, 10);
        g.fillRect(x + 30, y + height, 10, 10);
    }

    public boolean isJumping() {
        return jumping;
    }

    public boolean collidesWith(Obstacle obs) {
        Rectangle dinoRect = new Rectangle(x, y, width, height);
        Rectangle obsRect = new Rectangle(obs.getX(), obs.getY(), obs.getWidth(), obs.getHeight());
        return dinoRect.intersects(obsRect);
    }

    public boolean collidesWith(FallingObstacle fObs) {
        Rectangle dinoRect = new Rectangle(x, y, width, height);
        Rectangle fRect = new Rectangle(fObs.getX(), fObs.getY(), fObs.getWidth(), fObs.getHeight());
        return dinoRect.intersects(fRect);
    }
}

class Obstacle {
    private int x, y;
    private int width, height;
    private int speed = 6;
    private int type;
    private String courseName;
    private String courseCode;
    private int rotationAngle = 0;
    private Color obstacleColor; // 障碍物颜色
    
    // 课程信息映射：[课程编号, 课程内容]
    private static final String[][] COURSE_INFO = {
        {"", ""}, {"", ""},  // 索引0和1留空，对应原有仙人掌类型
        {"CS101", "Python"},
        {"CS201", "Java"},
        {"MATH 105", "Calculus"},
        {"STATS 211", "Stochastic Processes"},
        {"COMPSCI 203", "Discrete Math"},
        {"MATH 202", "Linear Algebra"},
        {"STATS 302", "Machine Learning"},
        {"MATH 206", "Probability & Stats"},
        {"COMPSCI 304", "Speech Recognition"},
        {"ERROR", "Syntax Error!"}
    };
    
    // 定义三种障碍物颜色
    private static final Color[] OBSTACLE_COLORS = {
        new Color(34, 139, 34),   // 深绿色
        new Color(144, 238, 144), // 浅绿色
        new Color(60, 100, 180),   // 深蓝色
    };

    public Obstacle(int x, int groundY, int type) {
        this.x = x;
        this.type = type;
        this.courseCode = COURSE_INFO[type][0];
        this.courseName = COURSE_INFO[type][1];
        
        // 随机选择三种颜色之一
        Random random = new Random();
        this.obstacleColor = OBSTACLE_COLORS[random.nextInt(OBSTACLE_COLORS.length)];
        
        // 设置障碍物大小
        if (type == 0) { // 小仙人掌
            width = 30;
            height = 40;
        } else if (type == 1) { // 大仙人掌
            width = 40;
            height = 60;
        } else if (type == GamePanel.TYPE_SYNTAX_ERROR) { // 语法错误bug
            width = 70;
            height = 50;
        } else { // 课程障碍物
            width = 60;  // 窄宽度
            height = 70; // 高长度
        }

        // 保证障碍物都立在地上
        this.y = groundY + 50 - height;
    }

    public void update() {
        x -= speed;
        rotationAngle = (rotationAngle + 3) % 360;
    }

    public void draw(Graphics g) {
        if (type == 0 || type == 1) { // 原有仙人掌
            g.setColor(obstacleColor);
            g.fillRect(x, y, width, height);

            g.setColor(obstacleColor.darker());
            g.fillRect(x - 5, y + 10, 10, 10);
            g.fillRect(x + width - 5, y + 20, 10, 10);
        } else if (type == GamePanel.TYPE_SYNTAX_ERROR) { // 语法错误bug
            // 黑色bug形状
            g.setColor(Color.BLACK);
            g.fillOval(x, y, width, height);
            
            // 绘制bug头部
            g.fillOval(x + width - 15, y + height/2 - 10, 20, 20);
            
            // 绘制bug的腿
            g.drawLine(x + 10, y + height, x + 5, y + height + 10);
            g.drawLine(x + 20, y + height, x + 15, y + height + 12);
            g.drawLine(x + 40, y + height, x + 45, y + height + 12);
            g.drawLine(x + 50, y + height, x + 55, y + height + 10);
            
            // 绘制bug的触角
            g.drawLine(x + width - 5, y + height/2 - 10, x + width + 10, y + height/2 - 20);
            g.drawLine(x + width, y + height/2 - 5, x + width + 10, y + height/2 - 15);
            
            // 绘制白色文字
            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospace", Font.BOLD, 12));
            drawCenteredString(g, courseName, x, y, width, height);
        } else { // 课程障碍物
            // 绘制课程方块（使用选定的颜色）
            g.setColor(obstacleColor);
            g.fillRect(x, y, width, height);
            
            // 绘制边框
            g.setColor(Color.BLACK);
            g.drawRect(x, y, width, height);
            
            // 绘制课程内容文字
            g.setColor(Color.BLACK);
            g.setFont(new Font("Monospace", Font.BOLD, 14));
            drawCenteredString(g, courseName, x, y, width, height);
        }
        
        // 绘制上方旋转的课程编号
        if (type >= 2 && !courseCode.isEmpty()) {
            drawRotatedString(g, courseCode, x + width/2, y - 15, rotationAngle);
        }
    }
    
    private void drawCenteredString(Graphics g, String text, int x, int y, int width, int height) {
        if (text == null || text.isEmpty()) return;
        
        FontMetrics metrics = g.getFontMetrics();
        int textX = x + (width - metrics.stringWidth(text)) / 2;
        int textY = y + ((height - metrics.getHeight()) / 2) + metrics.getAscent();
        g.drawString(text, textX, textY);
    }
    
    private void drawRotatedString(Graphics g, String text, int x, int y, int angle) {
        if (text == null || text.isEmpty()) return;
        
        Graphics2D g2d = (g instanceof Graphics2D) ? (Graphics2D) g : null;
        if (g2d == null) {
            g.drawString(text, x, y);
            return;
        }
        
        AffineTransform saveTransform = g2d.getTransform();
        
        try {
            g2d.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2d.setColor(Color.RED);
            
            g2d.translate(x, y);
            g2d.rotate(Math.toRadians(angle));
            
            FontMetrics metrics = g2d.getFontMetrics();
            int textX = -metrics.stringWidth(text) / 2;
            int textY = metrics.getAscent() / 2;
            
            g2d.drawString(text, textX, textY);
        } finally {
            g2d.setTransform(saveTransform);
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}

// 将FallingObstacle类移到外部，使其成为独立类
class FallingObstacle {
    private int x, y;
    private int width = 30, height = 30;
    private int speed = 4;

    public FallingObstacle(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void update() { y += speed; }

    public void draw(Graphics g) {
        // 绘制红色方块
        g.setColor(Color.RED);
        g.fillRect(x, y, width, height);

        // 在方块上方绘制白色文字 "DDL"
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 12));
        g.drawString("DDL", x + 3, y + 15); // x+3, y+15 让文字在方块中央偏上
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}