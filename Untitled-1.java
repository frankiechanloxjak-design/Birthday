import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Happy Birthday celebration with multiple hearts.
 * Compile: javac BirthdayHearts.java
 * Run:     java BirthdayHearts
 * Tip: click anywhere on the window for a burst of hearts!
 */
public class BirthdayHearts extends JPanel {

    // ====== CHANGE THESE ======
    static final String NAME = "Mah Beybiee";
    static final String FROM = "Your Bebiii";
    // ==========================

    static final Random RND = new Random();
    static final Color[] COLORS = {
        new Color(255, 64, 129), new Color(255, 105, 180), new Color(244, 67, 54),
        new Color(255, 138, 128), new Color(233, 30, 99), new Color(255, 182, 193),
        new Color(255, 215, 0), new Color(186, 104, 200)
    };
    static final Path2D HEART = buildHeart();

    // Unit heart, roughly spanning -1..1 horizontally
    static Path2D buildHeart() {
        Path2D p = new Path2D.Double();
        for (int i = 0; i <= 120; i++) {
            double t = 2 * Math.PI * i / 120;
            double x = Math.pow(Math.sin(t), 3);
            double y = -(13 * Math.cos(t) - 5 * Math.cos(2 * t)
                    - 2 * Math.cos(3 * t) - Math.cos(4 * t)) / 16.0;
            if (i == 0) p.moveTo(x, y); else p.lineTo(x, y);
        }
        p.closePath();
        return p;
    }

    static class Heart {
        double x, y, baseX, size, speed, phase, sway, vx, vy, alpha = 1;
        Color color;
        boolean burst;

        Heart(int w, int h, boolean randomY) {
            baseX = RND.nextDouble() * w;
            x = baseX;
            y = randomY ? RND.nextDouble() * h : h + 40;
            size = 14 + RND.nextDouble() * 40;
            speed = 0.6 + RND.nextDouble() * 1.6;
            phase = RND.nextDouble() * Math.PI * 2;
            sway = 10 + RND.nextDouble() * 30;
            color = COLORS[RND.nextInt(COLORS.length)];
        }

        Heart(double cx, double cy) { // burst heart
            burst = true;
            x = cx; y = cy;
            double ang = RND.nextDouble() * Math.PI * 2;
            double spd = 2 + RND.nextDouble() * 5;
            vx = Math.cos(ang) * spd;
            vy = Math.sin(ang) * spd - 2;
            size = 10 + RND.nextDouble() * 26;
            color = COLORS[RND.nextInt(COLORS.length)];
        }
    }

    private final List<Heart> hearts = new ArrayList<>();
    private double tick = 0;

    public BirthdayHearts() {
        setPreferredSize(new Dimension(900, 600));
        for (int i = 0; i < 45; i++) hearts.add(new Heart(900, 600, true));

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                for (int i = 0; i < 30; i++) hearts.add(new Heart(e.getX(), e.getY()));
            }
        });

        new Timer(16, e -> { update(); repaint(); }).start();
    }

    private void update() {
        tick += 0.03;
        int w = Math.max(getWidth(), 1), h = Math.max(getHeight(), 1);
        for (int i = hearts.size() - 1; i >= 0; i--) {
            Heart ht = hearts.get(i);
            if (ht.burst) {
                ht.x += ht.vx;
                ht.y += ht.vy;
                ht.vy += 0.05;
                ht.alpha -= 0.012;
                if (ht.alpha <= 0) hearts.remove(i);
            } else {
                ht.y -= ht.speed;
                ht.x = ht.baseX + Math.sin(tick * 2 + ht.phase) * ht.sway;
                if (ht.y < -ht.size * 2) {
                    hearts.set(i, new Heart(w, h, false));
                }
            }
        }
    }

    private void drawHeart(Graphics2D g, double cx, double cy, double size, Color c, double alpha) {
        AffineTransform old = g.getTransform();
        g.translate(cx, cy);
        g.scale(size / 2, size / 2);
        int a = (int) Math.max(0, Math.min(255, alpha * 255));
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), a));
        g.fill(HEART);
        g.setTransform(old);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // Background gradient
        g.setPaint(new GradientPaint(0, 0, new Color(40, 10, 50), 0, h, new Color(150, 30, 90)));
        g.fillRect(0, 0, w, h);

        // Floating + burst hearts
        for (Heart ht : hearts) {
            drawHeart(g, ht.x, ht.y, ht.size, ht.color, ht.burst ? ht.alpha : 0.85);
        }

        // Big pulsing heart in the center
        double pulse = 1 + 0.08 * Math.sin(tick * 5);
        double bigSize = Math.min(w, h) * 0.55 * pulse;
        drawHeart(g, w / 2.0, h / 2.0 - 20, bigSize, new Color(255, 40, 100), 0.35);
        drawHeart(g, w / 2.0, h / 2.0 - 20, bigSize * 0.75, new Color(255, 80, 130), 0.35);

        // Text
        drawCentered(g, "Happy Birthday,", new Font("Serif", Font.BOLD | Font.ITALIC, 52), w / 2, h / 2 - 50);
        drawCentered(g, NAME + "!", new Font("Serif", Font.BOLD, 84), w / 2, h / 2 + 35);
        drawCentered(g, "Wishing you the sweetest day ever!", new Font("SansSerif", Font.PLAIN, 22), w / 2, h / 2 + 85);
        drawCentered(g, "- with all my love, " + FROM, new Font("Serif", Font.ITALIC, 20), w / 2, h - 40);
    }

    private void drawCentered(Graphics2D g, String s, Font f, int cx, int y) {
        g.setFont(f);
        int x = cx - g.getFontMetrics().stringWidth(s) / 2;
        g.setColor(new Color(0, 0, 0, 120));
        g.drawString(s, x + 3, y + 3);
        g.setColor(Color.WHITE);
        g.drawString(s, x, y);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("Happy Birthday, " + NAME + "!");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setContentPane(new BirthdayHearts());
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}