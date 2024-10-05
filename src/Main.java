import javax.swing.*;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import javax.swing.Timer;

public class Main extends JPanel
{
    private final Cube[] cube;
    private final Cube c1;
    private final Cube c2;
    private double angle = 0;
    private static Camera camera;
    private double[][] zbuffer;
    public Main()
    {
        this.cube = new Cube[100];
        for (int i = 0; i < 100; i++)
        {
            this.cube[i] = new Cube();
        }
        this.c1 = new Cube();
        this.c2 = new Cube();
        this.zbuffer = new double[500][500];
        camera = new Camera( 90, 1, 0.01, 500);
        camera.position = new Vector3(0, 0, 10);
        Timer timer = new Timer(1, e -> {
            angle += Math.toRadians(3);
            repaint();
        });
        timer.start();
    }
    public void paint(Graphics g)
    {
        Image img = myImage();
        g.drawImage(img, 50, 50, this);
    }
    private void clearBuffer(double[][] zbuffer)
    {
      for(int i = 0; i < zbuffer.length; ++i)
      {
            java.util.Arrays.fill(zbuffer[i], Double.POSITIVE_INFINITY);
      }
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
        clearBuffer(zbuffer);

        double scale = 500;
        int screenWidth = 500;
        int center = screenWidth / 2;

        Matrix rotationMatrix =  Matrix.rotateY(angle);
        //rotationMatrix = Mat.matrixMul(rotationMatrix, Matrix.rotateZ(angle));
        //c1.rotate(rotationMatrix);
        //c2.transform(Matrix.transform(0, 0, 10));
        for(int i = 0; i < cube.length; ++i) {
            cube[i].rotate(Matrix.rotateY(angle));
            cube[i].transform(Matrix.transform( (int)(i / 10) * 5, 3,  (i % 10) * 5));
            cube[i].drawFilled(g, camera, scale, center, zbuffer);
        }
        //c1.transform(Matrix.transform(1,1,1));
        //c1.drawFilled(g, camera, scale, center, zbuffer);
        //c2.drawFilled(g, camera, scale, center, zbuffer);
        return buf;
    }

    public static void main(String[] args)
    {
        JFrame f = new JFrame();
        f.setSize(600,600);
        f.getContentPane().add(new Main());
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        f.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e)
            {
                switch(e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        camera.moveForward(-1);
                        break;
                    case KeyEvent.VK_S:
                        camera.moveForward(1);
                        break;
                    case KeyEvent.VK_D:
                        camera.moveRight(-1);
                        break;
                    case KeyEvent.VK_A:
                        camera.moveRight(1);
                        break;
                    case KeyEvent.VK_SPACE:
                        camera.moveUp(1);
                        break;
                    case KeyEvent.VK_K:
                        camera.moveUp(-1);
                        break;
                    case KeyEvent.VK_UP:
                        camera.rotateX(-10);
                        break;
                    case KeyEvent.VK_DOWN:
                        camera.rotateX(10);
                        break;
                    case KeyEvent.VK_LEFT:
                        camera.rotateY(-10);
                        break;
                    case KeyEvent.VK_RIGHT:
                        camera.rotateY(10);
                        break;
                }
                f.repaint();
            }
        });
    }
}
