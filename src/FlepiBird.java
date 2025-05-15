import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.*;

public class FlepiBird extends JFrame {
    CardLayout cardLayout;
    JPanel mainPanel;
    GamePanelImpl gamePanel;

    public FlepiBird() {
        setTitle("FlepiBird");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // CardLayout
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Menu dan Game Panel
        MainMenuPanel menuPanel = new MainMenuPanel(this);
        gamePanel = new GamePanelImpl(this);

        mainPanel.add(menuPanel, "Menu");
        mainPanel.add(gamePanel, "Game");

        add(mainPanel);
        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }

    public void showGame() {
        cardLayout.show(mainPanel, "Game");
        gamePanel.requestFocusInWindow();
    }

    public void showMenu() {
        cardLayout.show(mainPanel, "Menu");
    }

    public static void main(String[] args) {
        new FlepiBird();
    }
}

class MainMenuPanel extends JPanel {
    public MainMenuPanel(FlepiBird frame) {
        setPreferredSize(new Dimension(1920, 1080));
        setBackground(Color.ORANGE);
        setLayout(null);

        JLabel title = new JLabel("FLEPIBIRD");
        title.setFont(new Font("Arial", Font.BOLD, 100));
        title.setBounds(600, 200, 600, 100);
        add(title);

        JButton startButton = new JButton("Mulai");
        startButton.setFont(new Font("Arial", Font.BOLD, 40));
        startButton.setBounds(750, 400, 250, 80);
        startButton.addActionListener(e -> frame.showGame());
        add(startButton);

        JButton helpButton = new JButton("Petunjuk");
        helpButton.setFont(new Font("Arial", Font.BOLD, 40));
        helpButton.setBounds(750, 500, 250, 80);
        helpButton.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Tekan SPASI untuk melompat\nTekan R untuk mengulang saat Game Over",
                "Petunjuk", JOptionPane.INFORMATION_MESSAGE));
        add(helpButton);

        JButton exitButton = new JButton("Keluar");
        exitButton.setFont(new Font("Arial", Font.BOLD, 40));
        exitButton.setBounds(750, 600, 250, 80);
        exitButton.addActionListener(e -> System.exit(0));
        add(exitButton);
    }
}

abstract class GamePanel extends JPanel implements ActionListener, KeyListener {
    final int WIDTH = 1920;
    final int HEIGHT = 1080;
}

class GamePanelImpl extends GamePanel {
    Timer timer;
    int birdY = HEIGHT / 2;
    int velocity = 0;
    int gravity = 2;
    int jumpStrength = -20;
    boolean gameOver = false;
    Image birdImage;

    ArrayList<Rectangle> pipes = new ArrayList<>();
    int pipeSpacing = 300;
    int pipeWidth = 100;
    int pipeGap = 300;
    int pipeSpeed = 8;
    int score = 0;

    Random rand = new Random();
    FlepiBird frame;  // referensi ke JFrame utama

    GamePanelImpl(FlepiBird frame) {
        this.frame = frame;
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.CYAN);
        this.setFocusable(true);
        this.addKeyListener(this);

        try {
            birdImage = ImageIO.read(new File("bird.png"));
        } catch (IOException ex) {
            System.out.println("No image.");
        }

        for (int i = 0; i < 3; i++) {
            spawnPipe(i * pipeSpacing + 800);
        }

        timer = new Timer(20, this);
        timer.start();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    void draw(Graphics g) {
        g.setColor(Color.CYAN);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        if (birdImage != null) {
            g.drawImage(birdImage, 300, birdY, 50, 50, null);
        } else {
            g.setColor(Color.RED);
            g.fillOval(300, birdY, 50, 50);
        }

        g.setColor(Color.GREEN.darker());
        for (Rectangle pipe : pipes) {
            g.fillRect(pipe.x, pipe.y, pipe.width, pipe.height);
        }

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        g.drawString("Score: " + score, 50, 100);

        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 72));
            g.drawString("Game Over!", WIDTH / 2 - 200, HEIGHT / 2);
            g.setFont(new Font("Arial", Font.PLAIN, 36));
            g.drawString("Tekan R untuk restart atau ESC untuk kembali ke menu", WIDTH / 2 - 400, HEIGHT / 2 + 100);
        }
    }

    void spawnPipe(int x) {
        int pipeHeight = 100 + rand.nextInt(HEIGHT / 2);
        pipes.add(new Rectangle(x, 0, pipeWidth, pipeHeight));
        pipes.add(new Rectangle(x, pipeHeight + pipeGap, pipeWidth, HEIGHT - pipeHeight - pipeGap));
    }

    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            velocity += gravity;
            birdY += velocity;

            for (Rectangle pipe : pipes) {
                pipe.x -= pipeSpeed;
            }

            if (pipes.get(0).x + pipeWidth < 0) {
                pipes.remove(0);
                pipes.remove(0);
                spawnPipe(WIDTH);
                score++;
            }

            for (Rectangle pipe : pipes) {
                if (pipe.intersects(new Rectangle(300, birdY, 50, 50))) {
                    gameOver = true;
                }
            }

            if (birdY > HEIGHT || birdY < 0) {
                gameOver = true;
            }
        }

        repaint();
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE && !gameOver) {
            velocity = jumpStrength;
        } else if (e.getKeyCode() == KeyEvent.VK_R && gameOver) {
            resetGame();
        } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE && gameOver) {
            frame.showMenu();
        }
    }

    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    void resetGame() {
        birdY = HEIGHT / 2;
        velocity = 0;
        score = 0;
        pipes.clear();
        for (int i = 0; i < 3; i++) {
            spawnPipe(i * pipeSpacing + 800);
        }
        gameOver = false;
    }
}