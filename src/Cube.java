import java.awt.Graphics;
import java.awt.Color;

class Cube
{
    public final Vector3[] vertices;
    public final int[][] triangles;
    public Matrix rotationMatrix;
    public Matrix scaleMatrix;
    public Matrix transformMatrix;

    public Cube()
    {
        this.vertices = new Vector3[]
                {
                        new Vector3(-1, -1, -1), new Vector3(1, -1, -1),
                        new Vector3(1, 1, -1), new Vector3(-1, 1, -1),
                        new Vector3(-1, -1, 1), new Vector3(1, -1, 1),
                        new Vector3(1, 1, 1), new Vector3(-1, 1, 1)
                };
        this.triangles = new int[][] {
                {0, 1, 2}, {0, 2, 3},  // Front face
                {1, 5, 6}, {6, 2, 1},  // Right face
                {5, 4, 7}, {5, 7, 6},  // Back face
                {4, 0, 3}, {4, 3, 7},  // Left face
                {3, 2, 6}, {3, 6, 7},  // Top face
                {4, 5, 1}, {4, 1, 0}   // Bottom face
        };
        this.rotationMatrix = Matrix.identity();
        this.scaleMatrix = Matrix.identity();
        this.transformMatrix = Matrix.identity();
    }
    public void rotate(Matrix rotation)
    {
        this.rotationMatrix = rotation;
    }
    public void scale(Matrix scale)
    {
        this.scaleMatrix = scale;
    }
    public void transform(Matrix transform)
    {
        this.transformMatrix = transform;
    }
    public int[][] getTriangles()
    {
        return this.triangles;
    }
    public Vector3[] transformedVertices(Camera camera)
    {
        Vector3[] transformedVertices = new Vector3[this.vertices.length];
        for(int i = 0; i < this.vertices.length; i++)
        {
            Matrix point = Matrix.point3D(this.vertices[i]);

            point = Mat.matrixMul(this.scaleMatrix, point);
            point = Mat.matrixMul(this.transformMatrix, point);

            Matrix rotatedPoint = Mat.matrixMul(this.rotationMatrix, point);

            Matrix finalPoint = Mat.matrixTrans(rotatedPoint, camera.position.x, camera.position.y, camera.position.z);
            transformedVertices[i] = finalPoint.toVector();
        }
        return transformedVertices;
    }
    public void bresenham(Graphics g, int x0, int y0, int x1, int y1)
    {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = (x0 < x1) ? 1 : -1;
        int sy = (y0 < y1) ? 1 : -1;
        int err = dx - dy;
        while(true)
        {
            g.fillRect(x0, y0, 2, 2);
            if(x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }
    public void draw(Graphics g, Camera camera, double scale, int center)
    {
        Vector3[] newVertices = transformedVertices(camera);
        Vector2[] projectedPoints = new Vector2[newVertices.length];

        for(int i = 0; i < newVertices.length; i++)
        {
            Matrix point = Matrix.point3D(newVertices[i]);
            Matrix projected = Mat.matrixMul(camera.perspective, point);
            Vector2 screenPoint = Mat.project2D(projected);
            screenPoint.x = screenPoint.x * scale + center;
            screenPoint.y = screenPoint.y * scale + center;
            projectedPoints[i] = screenPoint;
        }

        int[][] cubeFaces = getTriangles();

        for(int[] face : cubeFaces)
        {
            Vector3 v0 = this.vertices[face[0]];
            Vector3 v1 = this.vertices[face[1]];
            Vector3 v2 = this.vertices[face[2]];

            Matrix rotatedPointV0 = Mat.matrixMul(this.rotationMatrix, Matrix.point3D(v0));
            Matrix rotatedPointV1 = Mat.matrixMul(this.rotationMatrix, Matrix.point3D(v1));
            Matrix rotatedPointV2 = Mat.matrixMul(this.rotationMatrix, Matrix.point3D(v2));

            Vector3 rv0 = rotatedPointV0.toVector();
            Vector3 rv1 = rotatedPointV1.toVector();
            Vector3 rv2 = rotatedPointV2.toVector();

            Vector3 edge1 = Mat.vecSub(rv1, rv0);
            Vector3 edge2 = Mat.vecSub(rv2, rv0);
            Vector3 normal = Mat.vecCross(edge1, edge2);
            Mat.vecNormalize(normal);

            Vector3 cameraFace = Mat.vecSub(camera.position, rv0);

            if (Mat.vecDotNum(normal, cameraFace) > 2.61) {
                for (int i = 0; i < 3; i++) {
                    int start = face[i];
                    int end = face[(i + 1) % 3];
                    g.drawLine((int) projectedPoints[start].x, (int) projectedPoints[start].y,
                            (int) projectedPoints[end].x, (int) projectedPoints[end].y);
                }
            }
        }
    }
    public void drawFilled(Graphics g, Camera camera, double scale, int center)
    {
        Vector3[] newVertices = transformedVertices(camera);
        Vector2[] projectedPoints = new Vector2[newVertices.length];

        for(int i = 0; i < newVertices.length; i++)
        {
            Matrix point = Matrix.point3D(newVertices[i]);
            Matrix projected = Mat.matrixMul(camera.perspective, point);
            Vector2 screenPoint = Mat.project2D(projected);
            screenPoint.x = screenPoint.x * scale + center;
            screenPoint.y = screenPoint.y * scale + center;
            projectedPoints[i] = screenPoint;
        }

        java.util.Arrays.sort(triangles, (a, b) -> {
            double zA = (newVertices[a[0]].z + newVertices[a[1]].z + newVertices[a[2]].z) / 3;
            double zB = (newVertices[b[0]].z + newVertices[b[1]].z + newVertices[b[2]].z) / 3;
            return Double.compare(zB, zA);
        });

        for(int[] triangle: triangles)
        {
            Vector2 v0 = projectedPoints[triangle[0]];
            Vector2 v1 = projectedPoints[triangle[1]];
            Vector2 v2 = projectedPoints[triangle[2]];

            Vector3 l0 = this.vertices[triangle[0]];
            Vector3 l1 = this.vertices[triangle[1]];
            Vector3 l2 = this.vertices[triangle[2]];

            Matrix rotatedPointV0 = Mat.matrixMul(this.rotationMatrix, Matrix.point3D(l0));
            Matrix rotatedPointV1 = Mat.matrixMul(this.rotationMatrix, Matrix.point3D(l1));
            Matrix rotatedPointV2 = Mat.matrixMul(this.rotationMatrix, Matrix.point3D(l2));

            Vector3 rv0 = rotatedPointV0.toVector();
            Vector3 rv1 = rotatedPointV1.toVector();
            Vector3 rv2 = rotatedPointV2.toVector();

            Vector3 edge1 = Mat.vecSub(rv1, rv0);
            Vector3 edge2 = Mat.vecSub(rv2, rv0);
            Vector3 normal = Mat.vecCross(edge1, edge2);
            Mat.vecNormalize(normal);

            Vector3 cameraDir = Mat.vecSub(camera.position, rv0);
            double faceAng = Mat.vecDotNum(normal, cameraDir);
            if(faceAng > 2) {
                Vector3 lightDir = new Vector3(0, 0, 1);
                double intensity = Math.max(0, Mat.vecDotNum(normal, lightDir));

                int colorVal = (int) (intensity * 220) + 35;
                g.setColor(new Color(colorVal, colorVal, colorVal));

                fillTriangle(g, v0, v1, v2);
                bresenham(g, (int)v0.x, (int)v0.y, (int)v1.x, (int)v1.y);
                bresenham(g, (int)v1.x, (int)v1.y, (int)v2.x, (int)v2.y);
                bresenham(g, (int)v2.x, (int)v2.y, (int)v0.x, (int)v0.y);

            }
        }
    }
    public void fillTriangle(Graphics g, Vector2 v0, Vector2 v1, Vector2 v2)
    {
        if(v1.y < v0.y) {Vector2 temp = v0; v0 = v1; v1 = temp;}
        if(v2.y < v0.y) {Vector2 temp = v0; v0 = v2; v2 = temp;}
        if(v2.y < v1.y) {Vector2 temp = v1; v1 = v2; v2 = temp;}

        //Flat-top triangle
        if(v1.y == v2.y)
        {
            fillFlatTop(g, v0, v1, v2);
        }
        else if(v0.y == v1.y)
        {
            fillFlatBottom(g, v0, v1, v2);
        }
        else
        {
            Vector2 v3 = new Vector2(v0.x + ((v1.y - v0.y) / (v2.y - v0.y) * (v2.x - v0.x)), v1.y);
            fillFlatTop(g, v0, v1, v3);
            fillFlatBottom(g, v1, v3, v2);
        }
    }

    public void fillFlatTop(Graphics g, Vector2 v0, Vector2 v1, Vector2 v2) {
        double slope1 = (v1.x - v0.x) / (v1.y - v0.y);
        double slope2 = (v2.x - v0.x) / (v2.y - v0.y);

        double x1 =  v0.x;
        double x2 =  v0.x;

        int minY = (int) Math.ceil(v0.y);
        int maxY = (int) Math.floor(v2.y);

        for(int y = minY; y<= maxY; y++)
        {
            int minX = (int) Math.ceil(Math.min(x1, x2));
            int maxX = (int) Math.floor(Math.max(x1, x2));

            minX = Math.max(minX, (int) Math.ceil(Math.min(v0.x, Math.min(v1.x, v2.x))));
            maxX = Math.min(maxX, (int) Math.floor(Math.max(v0.x, Math.max(v1.x, v2.x))));

            if(minX <= maxX) {
                g.drawLine(minX, y, maxX, y);
            }
            x1 += slope1;
            x2 += slope2;
        }
    }

    public void fillFlatBottom(Graphics g, Vector2 v0, Vector2 v1, Vector2 v2) {
        double slope1 = (v2.x - v0.x) / (v2.y - v0.y);
        double slope2 = (v2.x - v1.x) / (v2.y - v1.y);

        double x1 = v0.x;
        double x2 = v0.x;

        int minY = (int) Math.ceil(v0.y);
        int maxY = (int) Math.floor(v2.y);

        for(int y = minY; y<= maxY; y++)
        {
            int minX = (int) Math.ceil(Math.min(x1, v1.x + (y - v1.y) * slope2));
            int maxX = (int) Math.floor(Math.max(x1, v1.x + (y - v1.y) * slope2));

            minX = Math.max(minX, (int) Math.ceil(Math.min(v0.x, Math.min(v1.x, v2.x))));
            maxX = Math.min(maxX, (int) Math.floor(Math.max(v0.x, Math.max(v1.x, v2.x))));

            if(minX <= maxX) {
                g.drawLine(minX, y, maxX, y);
            }
            x1 += slope1;
        }
    }
}
