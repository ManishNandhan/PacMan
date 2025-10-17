import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.GeneralPath;
import javax.swing.*;

public class App {
    private static Image pacmanMenuImage = new ImageIcon(App.class.getResource("./pacmanRight.png")).getImage();
    // helper to draw a simple Pac-Man icon (angle in degrees for mouth)
    private static void drawPacman(Graphics2D g2, int x, int y, int size, int angle) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int pad = 2;
        int cx = x + size/2;
        int cy = y + size/2;
        // body
        g2.setColor(new Color(255, 210, 60));
        g2.fillArc(x+pad, y+pad, size-pad*2, size-pad*2, angle + 30, 300 - angle);
        // eye
        g2.setColor(Color.BLACK);
        int ex = cx + size/6;
        int ey = cy - size/6;
        g2.fillOval(ex, ey, Math.max(2, size/8), Math.max(2, size/8));
    }

    // helper to draw a simple ghost shape
    private static void drawGhost(Graphics2D g2, int x, int y, int size, Color color) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = size;
        int headH = (int)(size * 0.6);
        int skirtH = size - headH;

        // body base
        g2.setColor(color);
        g2.fillRoundRect(x, y, w, headH, w/2, w/2);
        g2.fillRect(x, y + headH/2, w, skirtH);

        // scalloped skirt (three semicircles)
        int scallopW = w/3;
        int scallopY = y + headH;
        for (int i = 0; i < 3; i++) {
            int sx = x + i * scallopW;
            g2.fillOval(sx, scallopY - scallopW/2, scallopW, scallopW);
        }

        // subtle top shading
        GradientPaint gp = new GradientPaint(x, y, new Color(255,255,255,60), x, y+headH, color.darker());
        g2.setPaint(gp);
        g2.fillRoundRect(x, y, w, headH, w/2, w/2);

        // eyes (classic large white eyes with black pupils)
        int eyeW = Math.max(6, w/5);
        int eyeH = Math.max(8, w/4);
        int eye1x = x + w/6;
        int eye2x = x + w/2;
        int ey = y + headH/3;
        g2.setColor(Color.WHITE);
        g2.fillOval(eye1x, ey, eyeW, eyeH);
        g2.fillOval(eye2x, ey, eyeW, eyeH);

        // pupils (slightly offset so they look leftwards by default)
        g2.setColor(Color.BLACK);
        int px = eyeW/3;
        int py = eyeH/3;
        g2.fillOval(eye1x + px - 2, ey + py, eyeW/3, eyeH/3);
        g2.fillOval(eye2x + px - 2, ey + py, eyeW/3, eyeH/3);

        // outline for clarity
        g2.setColor(color.darker().darker());
        g2.setStroke(new BasicStroke(2f));
        GeneralPath outline = new GeneralPath();
        outline.append(new java.awt.geom.RoundRectangle2D.Float(x, y, w, headH, w/2, w/2), false);
        outline.append(new java.awt.geom.Rectangle2D.Float(x, y + headH/2, w, skirtH), false);
        g2.draw(outline);
    }

    public static void main(String[] args) throws Exception {
        int rowCount = 21;
        int columnCount = 19;
        int tileSize = 32;
        int boardWidth = columnCount * tileSize;
        int boardHeight = rowCount * tileSize;

        JFrame frame = new JFrame("Pac Man");
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Enhanced menu panel with banner and styled Start button + characters
        JPanel menuPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                int w = getWidth();
                int h = getHeight();
                // subtle gradient background
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(8, 8, 30), w, h, new Color(0, 0, 0)));
                g2.fillRect(0, 0, w, h);

                // Title center
                String logo = "PAC-MAN";
                Font logoFont = new Font("Arial", Font.BOLD, 64);
                g2.setFont(logoFont);
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(logo)) / 2;
                int ty = h / 4;

                // Title rendering with stroked GlyphVector, gradient fill and glow outline
                FontRenderContext frc = g2.getFontRenderContext();
                GlyphVector gv = logoFont.createGlyphVector(frc, logo);
                Shape textShape = gv.getOutline(tx, ty);

                // glow: stroke the shape multiple times with increasing width and alpha
                g2.setColor(new Color(255, 230, 80, 48));
                for (int i = 12; i >= 4; i -= 2) {
                    g2.setStroke(new BasicStroke(i, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.draw(textShape);
                }

                // fill  title with golden gradient
                Rectangle bounds = textShape.getBounds();
                GradientPaint textGP = new GradientPaint(bounds.x, bounds.y, new Color(255, 250, 180), bounds.x, bounds.y + bounds.height, new Color(255, 180, 40));
                g2.setPaint(textGP);
                g2.fill(textShape);

                // outline
                g2.setColor(new Color(120, 60, 0));
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(textShape);

                // Draw a highlighted Pac-Man image to the left of the title and remove ghosts
                int iconSize = 96;
                int imgX = tx - iconSize - 32;
                int imgY = ty - iconSize/2;
                // halo
                RadialGradientPaint rgp = new RadialGradientPaint(new Point(imgX + iconSize/2, imgY + iconSize/2), iconSize, new float[]{0f, 1f}, new Color[]{new Color(255,220,80,180), new Color(0,0,0,0)});
                g2.setPaint(rgp);
                g2.fillOval(imgX - 10, imgY - 10, iconSize + 20, iconSize + 20);
                // pacman image centered
                g2.drawImage(pacmanMenuImage, imgX, imgY, iconSize, iconSize, null);

                // subtitle
                String subtitle = "Classic Arcade Maze";
                Font subFont = new Font("Arial", Font.ITALIC, 20);
                g2.setFont(subFont);
                fm = g2.getFontMetrics();
                int sx = (w - fm.stringWidth(subtitle)) / 2;
                g2.setColor(new Color(220, 220, 220, 200));
                g2.drawString(subtitle, sx, ty + 56);

                // small footer hint
                String footer = "Use arrow keys to move — Eat all the dots!";
                Font f2 = new Font("Arial", Font.PLAIN, 14);
                g2.setFont(f2);
                fm = g2.getFontMetrics();
                int fx = (w - fm.stringWidth(footer)) / 2;
                g2.setColor(new Color(180, 180, 180, 160));
                g2.drawString(footer, fx, h - 40);

                g2.dispose();
            }
        };
        //layout to position the start button nicely in center
        menuPanel.setPreferredSize(new Dimension(boardWidth, boardHeight));
        menuPanel.setBackground(Color.BLACK);
        menuPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(40, 0, 0, 0);

        // Custom rounded gradient Start button
        class GradientButton extends JButton {
            private Color start = new Color(255, 200, 60);
            private Color end = new Color(255, 140, 40);
            private boolean hover = false;
            private boolean pressed = false;

            GradientButton(String text) {
                super(text);
                setContentAreaFilled(false);
                setFocusPainted(false);
                setForeground(new Color(40, 14, 0));
                setFont(new Font("Arial", Font.BOLD, 30));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                setBorder(BorderFactory.createEmptyBorder(12, 28, 12, 28));
                getModel().addChangeListener(e -> {
                    hover = getModel().isRollover();
                    pressed = getModel().isPressed();
                    repaint();
                });
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(300, 80);
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth();
                int h = getHeight();

                // drop shadow
                g2.setColor(new Color(0, 0, 0, pressed ? 80 : 120));
                g2.fillRoundRect(4, 6, w - 8, h - 6, 28, 28);

                // gradient
                Color s = hover ? start.brighter() : start;
                Color e = hover ? end.brighter() : end;
                if (pressed) {
                    s = s.darker();
                    e = e.darker();
                }
                g2.setPaint(new GradientPaint(0, 0, s, 0, h, e));
                g2.fillRoundRect(0, 0, w - 8, h - 8, 28, 28);

                // border
                g2.setColor(new Color(180, 120, 30));
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(0, 0, w - 8, h - 8, 28, 28);

                // text
                FontMetrics fm = g2.getFontMetrics(getFont());
                int tx = (w - fm.stringWidth(getText())) / 2 - 4;
                int ty = (h + fm.getAscent() - fm.getDescent()) / 2 - 4;
                g2.setColor(new Color(40, 14, 0));
                g2.setFont(getFont());
                g2.drawString(getText(), tx, ty);

                g2.dispose();
            }
        }
       // creates start button and adds it to panel
        GradientButton startButton = new GradientButton("START");

        menuPanel.add(startButton, gbc);

        frame.setContentPane(menuPanel);
        frame.pack();
        frame.setVisible(true);

        startButton.addActionListener(e -> {
            PacMan pacmanGame = new PacMan();
            frame.setContentPane(pacmanGame);
            frame.pack();
            pacmanGame.requestFocusInWindow();
        });
    }
}