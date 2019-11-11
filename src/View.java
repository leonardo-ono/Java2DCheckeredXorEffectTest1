
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;


/**
 * Java 2D Checkered XOR Effect Test #1
 * 
 * @author Leonardo Ono (ono.leo@gmail.com)
 */
public class View extends JPanel {
    
    private static final int SCREEN_WIDTH = 300;
    private static final int SCREEN_HEIGHT = 240;
    private static final int SCREEN_SCALE = 2;

    private final BufferedImage frameBuffer;
    private final int[] screen;
    
    // timer for main loop
    private final Timer timer = new Timer();
    
    private final AffineTransform at = new AffineTransform();
    private double angle = 0;
    private double scale = 1;
    
    public View() {
        int sw = SCREEN_WIDTH;
        int sh = SCREEN_HEIGHT;
        frameBuffer = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB);
        Raster raster = frameBuffer.getRaster();
        screen = ((DataBufferInt) raster.getDataBuffer()).getData();
    }
    
    public void start() {
        // main loop, something close to 30 fps
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                update();
                repaint();
            }
        }, 100, 1000 / 30);
    }
    
    private void update() {
        angle += 0.01;
        scale = 2.5 + 0.5 * Math.sin(8 * angle);
    }
        
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); 
        Graphics2D fbg = (Graphics2D) frameBuffer.getGraphics();
        drawOffscreen(fbg);
        g.drawImage(frameBuffer, 0, 0, 
            SCREEN_WIDTH * SCREEN_SCALE, SCREEN_HEIGHT * SCREEN_SCALE, 
            0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, null);
    }
    
    private void drawOffscreen(Graphics2D g) {
        g.setBackground(Color.BLACK);
        g.clearRect(0, 0, getWidth(), getHeight());

        draw(g, SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 
            2.5 - scale * 0.5, 2.5 - scale * 0.5, angle, 3, false);
        
        draw(g, SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 
            2.5 - scale * 0.5, 2.5 - scale * 0.5, angle + Math.toRadians(90), 
            3, true);
        
        draw(g, SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 
            scale, scale, -angle, 4, true);
        
        draw(g, SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2, 
            scale, scale, -angle + Math.toRadians(90), 4, true);
    }

    private void draw(Graphics2D g, double tx, double ty, double sx, double sy, 
        double a, int shiftRight, boolean useXor) {
        
        try {
            at.setToIdentity();
            at.translate(tx, ty);
            at.scale(sx, sy);
            at.rotate(a);
            AffineTransform it = at.createInverse();
            Point2D ptSrc = new Point2D.Double();
            Point2D ptDst = new Point2D.Double();
            for (int y = 0; y < SCREEN_HEIGHT; y++) {
                for (int x = 0; x < SCREEN_WIDTH; x++) {
                    ptSrc.setLocation(x, y);
                    it.transform(ptSrc, ptDst);
                    int dx = (int) ptDst.getX();
                    if ((dx >> shiftRight) % 2 == 0) {
                        if (useXor) {
                            screen[x + y * SCREEN_WIDTH] ^= 0x0ff;
                        }
                        else {
                            screen[x + y * SCREEN_WIDTH] = 0xff0000ff;
                        }
                    }
                }
            }
        } catch (NoninvertibleTransformException ex) {
            throw new RuntimeException("matrix not invertible !");
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            View view = new View();
            view.setPreferredSize(new Dimension(SCREEN_WIDTH * SCREEN_SCALE, 
                    SCREEN_HEIGHT * SCREEN_SCALE));
            
            JFrame frame = new JFrame();
            frame.setTitle("Java 2D Checkered XOR Effect Demo Test #1");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(view);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            view.start();
        });
    }
    
}
