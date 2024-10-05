import java.awt.Graphics;
import java.awt.Color;

class Cube
{
    public final Vector3[] vertices;
    public final int[][] triangles;
    public Matrix rotationMatrix;
    public Matrix scaleMatrix;
    public Matrix transformMatrix;


    private static final double EPSILON = 1e-1;

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
        Matrix viewMatrix = camera.getViewMatrix();
        for(int i = 0; i < this.vertices.length; i++)
        {
            Matrix point = Matrix.point3D(this.vertices[i]);

            point = Mat.matrixMul(this.scaleMatrix, point);
            point = Mat.matrixMul(this.rotationMatrix, point);
            point = Mat.matrixMul(this.transformMatrix, point);

            // Apply view transformation
            point = Mat.matrixMul(viewMatrix, point);

            // Apply perspective projection
            point = Mat.matrixMul(camera.perspective, point);

            transformedVertices[i] = point.toVector();
        }
        return transformedVertices;
    }

    private void bresenham(Graphics g, Vector3 v0, Vector3 v1, double[][] zbuffer)
    {
        int x0 = (int) v0.x;
        int y0 = (int) v0.y;
        double z0 = v0.z;
        int x1 = (int) v1.x;
        int y1 = (int) v1.y;
        double z1 = v1.z;

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = (x0 < x1) ? 1 : -1;
        int sy = (y0 < y1) ? 1 : -1;

        int err = dx - dy;

        int steps = Math.max(dx, dy);
        double zStep = (z1 - z0) / (steps != 0 ? steps : 1);

        while(true)
        {
            if(checkZBuffer(zbuffer, x0, y0, z0)) {
                g.fillRect(x0, y0, 2, 2);
            }

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

            z0 += zStep;
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

            if (Mat.vecDotNum(normal, cameraFace) > 1) {
                for (int i = 0; i < 3; i++) {
                    int start = face[i];
                    int end = face[(i + 1) % 3];
                    g.drawLine((int) projectedPoints[start].x, (int) projectedPoints[start].y,
                            (int) projectedPoints[end].x, (int) projectedPoints[end].y);
                }
            }
        }
    }


    private boolean isTriangleBehind(Vector3[] vertices, double[][] zbuffer)
    {
        int minX = (int) Math.min(vertices[0].x, Math.min(vertices[1].x, vertices[2].x));
        int maxX = (int) Math.max(vertices[0].x, Math.max(vertices[1].x, vertices[2].x));
        int minY = (int) Math.min(vertices[0].y, Math.min(vertices[1].y, vertices[2].y));
        int maxY = (int) Math.max(vertices[0].y, Math.max(vertices[1].y, vertices[2].y));

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (x >= 0 && x < zbuffer.length && y >= 0 && y < zbuffer[0].length) {
                    double z = interpolateZ(vertices[0], vertices[1], vertices[2], x, y);
                    if (z < zbuffer[x][y]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private double interpolateZ(Vector3 v0, Vector3 v1, Vector3 v2, int x, int y) {
        double area = (v1.y - v2.y) * (v0.x - v2.x) + (v2.x - v1.x) * (v0.y - v2.y);
        double w0 = ((v1.y - v2.y) * (x - v2.x) + (v2.x - v1.x) * (y - v2.y)) / area;
        double w1 = ((v2.y - v0.y) * (x - v2.x) + (v0.x - v2.x) * (y - v2.y)) / area;
        double w2 = 1 - w0 - w1;

        return w0 * v0.z + w1 * v1.z + w2 * v2.z;
    }

    public void drawFilled(Graphics g, Camera camera, double scale, int center, double[][] zbuffer)
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

            newVertices[i].z = (newVertices[i].z - camera.near) / (camera.far - camera.near);
        }

        for(int[] triangle: triangles)
        {
            Vector3 v0 = new Vector3(projectedPoints[triangle[0]].x, projectedPoints[triangle[0]].y, newVertices[triangle[0]].z);
            Vector3 v1 = new Vector3(projectedPoints[triangle[1]].x, projectedPoints[triangle[1]].y, newVertices[triangle[1]].z);
            Vector3 v2 = new Vector3(projectedPoints[triangle[2]].x, projectedPoints[triangle[2]].y, newVertices[triangle[2]].z);

            Vector3 l0 = this.vertices[triangle[0]];
            Vector3 l1 = this.vertices[triangle[1]];
            Vector3 l2 = this.vertices[triangle[2]];

            Matrix L0 = Mat.matrixMul(this.scaleMatrix, Matrix.point3D(l0));
            Matrix L1 = Mat.matrixMul(this.scaleMatrix, Matrix.point3D(l1));
            Matrix L2 = Mat.matrixMul(this.scaleMatrix, Matrix.point3D(l2));

            Matrix rotatedPointV0 = Mat.matrixMul(this.rotationMatrix, L0);
            Matrix rotatedPointV1 = Mat.matrixMul(this.rotationMatrix, L1);
            Matrix rotatedPointV2 = Mat.matrixMul(this.rotationMatrix, L2);

            rotatedPointV0 = Mat.matrixMul(this.transformMatrix, rotatedPointV0);
            rotatedPointV1 = Mat.matrixMul(this.transformMatrix, rotatedPointV1);
            rotatedPointV2 = Mat.matrixMul(this.transformMatrix, rotatedPointV2);

            rotatedPointV0 = Mat.matrixMul(camera.getViewMatrix(), rotatedPointV0);
            rotatedPointV1 = Mat.matrixMul(camera.getViewMatrix(), rotatedPointV1);
            rotatedPointV2 = Mat.matrixMul(camera.getViewMatrix(), rotatedPointV2);

            rotatedPointV0 = Mat.matrixMul(camera.perspective, rotatedPointV0);
            rotatedPointV1 = Mat.matrixMul(camera.perspective, rotatedPointV1);
            rotatedPointV2 = Mat.matrixMul(camera.perspective, rotatedPointV2);

            Vector3 rv0 = rotatedPointV0.toVector();
            Vector3 rv1 = rotatedPointV1.toVector();
            Vector3 rv2 = rotatedPointV2.toVector();

            Vector3 edge1 = Mat.vecSub(rv1, rv0);
            Vector3 edge2 = Mat.vecSub(rv2, rv0);
            Vector3 normal = Mat.vecCross(edge1, edge2);
            Mat.vecNormalize(normal);


            // Relaxed back-face culling condition
            Vector3 cameraDir = Mat.vecSub(camera.forward, rv0);
            double faceAng = Mat.vecDotNum(normal, cameraDir);

            // Back-face culling condition
            if(faceAng < -1) {
                if(!isTriangleBehind(new Vector3[]{v0, v1, v2}, zbuffer)) {
                    Vector3 lightDir = new Vector3(0, 0, -1);
                    double intensity = Math.max(0, Mat.vecDotNum(normal, lightDir));

                    int colorVal = (int) (intensity * 220) + 35;
                    g.setColor(new Color(colorVal, colorVal, colorVal));

                    fillTriangle(g, v0, v1, v2, zbuffer);
                    bresenham(g, v0, v1, zbuffer);
                    bresenham(g, v1, v2, zbuffer);
                    bresenham(g, v2, v0, zbuffer);
                }
            }
        }
    }
    public void fillTriangle(Graphics g, Vector3 v0, Vector3 v1, Vector3 v2, double[][] zbuffer)
    {
        if(v1.y < v0.y) {Vector3 temp = v0; v0 = v1; v1 = temp;}
        if(v2.y < v0.y) {Vector3 temp = v0; v0 = v2; v2 = temp;}
        if(v2.y < v1.y) {Vector3 temp = v1; v1 = v2; v2 = temp;}

        //Flat-top triangle
        if(v1.y == v2.y)
        {
            fillFlatTop(g, v0, v1, v2, zbuffer);
        }
        else if(v0.y == v1.y)
        {
            fillFlatBottom(g, v0, v1, v2, zbuffer);
        }
        else
        {
            double z3 = v0.z + ((v1.y - v0.y) / (v2.y - v0.y) * (v2.z - v0.z));
            Vector3 v3 = new Vector3(v0.x + ((v1.y - v0.y) / (v2.y - v0.y) * (v2.x - v0.x)), v1.y, z3);
            fillFlatTop(g, v0, v1, v3, zbuffer);
            fillFlatBottom(g, v1, v3, v2, zbuffer);
        }
    }


    private boolean checkZBuffer(double[][] zbuffer, int x, int y, double z) {
        if (x < 0 || x >= zbuffer.length || y < 0 || y >= zbuffer[0].length) {
            return false;
        }
        if (z < zbuffer[x][y] - EPSILON) {
            zbuffer[x][y] = z;
            return true;
        }
        return false;
    }
    public void fillFlatTop(Graphics g, Vector3 v0, Vector3 v1, Vector3 v2, double[][] zbuffer) {
        double slope1 = (v1.x - v0.x) / (v1.y - v0.y);
        double slope2 = (v2.x - v0.x) / (v2.y - v0.y);
        double zSlope1 = (v1.z - v0.z) / (v1.y - v0.y);
        double zSlope2 = (v2.z - v0.z) / (v2.y - v0.y);

        double x1 = v0.x;
        double x2 = v0.x;
        double z1 = v0.z;
        double z2 = v0.z;

        int minY = (int) Math.ceil(v0.y);
        int maxY = (int) Math.floor(v2.y);

        for(int y = minY; y <= maxY; y++)
        {
            int minX = (int) Math.ceil(Math.min(x1, x2));
            int maxX = (int) Math.floor(Math.max(x1, x2));

            double zLeft = z1;
            double zRight = z2;
            double zStep = (zRight - zLeft) / (maxX - minX + 1);

            for(int x = minX; x <= maxX; x++) {
                if(checkZBuffer(zbuffer, x, y, zLeft)) {
                    g.drawLine(x, y, x, y);
                }
                zLeft += zStep;
            }
            x1 += slope1;
            x2 += slope2;
            z1 += zSlope1;
            z2 += zSlope2;
        }
    }

    public void fillFlatBottom(Graphics g, Vector3 v0, Vector3 v1, Vector3 v2, double[][] zbuffer) {
        double slope1 = (v2.x - v0.x) / (v2.y - v0.y);
        double slope2 = (v2.x - v1.x) / (v2.y - v1.y);
        double zSlope1 = (v2.z - v0.z) / (v2.y - v0.y);
        double zSlope2 = (v2.z - v1.z) / (v2.y - v1.y);

        double x1 = v0.x;
        double x2 = v1.x;
        double z1 = v0.z;
        double z2 = v1.z;

        int minY = (int) Math.ceil(v0.y);
        int maxY = (int) Math.floor(v2.y);

        for(int y = minY; y <= maxY; y++)
        {
            int minX = (int) Math.ceil(Math.min(x1, x2));
            int maxX = (int) Math.floor(Math.max(x1, x2));

            double zLeft = z1;
            double zRight = z2;
            double zStep = (zRight - zLeft) / (maxX - minX + 1);

            for(int x = minX; x <= maxX; x++) {
                if(checkZBuffer(zbuffer, x, y, zLeft)) {
                    g.drawLine(x, y, x, y);
                }
                zLeft += zStep;
            }
            x1 += slope1;
            x2 += slope2;
            z1 += zSlope1;
            z2 += zSlope2;
        }
    }
}
