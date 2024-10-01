import javax.swing.*;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.Timer;

public class Main extends JPanel
{
    private final Cube cube;
    private double angle = 0;
    public Main()
    {
        this.cube = new Cube();
        Timer timer = new Timer(1, e -> {
            angle += Math.toRadians(10);
            repaint();
        });
        timer.start();
    }
    public void paint(Graphics g)
    {
        Image img = myImage();
        g.drawImage(img, 50, 50, this);
    }
    private Vector3 calculateCenter(Vector3[] vertices)
    {
        double sumX = 0, sumY = 0, sumZ = 0;
        int numVertices = vertices.length;
        for(Vector3 v : vertices)
        {
            sumX += v.x;
            sumY += v.y;
            sumZ += v.z;
        }
        return new Vector3(sumX / numVertices, sumY / numVertices, sumZ / numVertices);
    }


    private Image myImage()
    {

        BufferedImage buf = new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB);
        Graphics g = buf.getGraphics();
        g.setColor(java.awt.Color.BLACK);
        g.fillRect(0, 0, buf.getWidth(), buf.getHeight());
        g.setColor(java.awt.Color.WHITE);

        Camera camera = new Camera( 90, 1, 0.01, 500);
        double scale = 500;
        int screenWidth = 500;
        int center = screenWidth / 2;
        camera.position = new Vector3(0, 0, 10);



        Matrix rotationMatrix =  Matrix.rotateY(angle);
        cube.rotate(rotationMatrix);

        cube.drawFilled(g, camera, scale, center);
        return buf;
    }

    public static void main(String[] args)
    {
        JFrame f = new JFrame();
        f.setSize(600,600);
        f.getContentPane().add(new Main());
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


    }
}