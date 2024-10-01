import java.lang.Math;

class Vector2
{
    double x;
    double y;
    double mag;

    public Vector2(double x, double y)
    {
        this.x = x;
        this.y = y;
        vecMag();
    }
    public void vecMag()
    {
        this.mag = Math.sqrt(this.x*this.x + this.y*this.y);
    }
}
class Vector3
{
    double x;
    double y;
    double z;
    double mag;
    public Vector3(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        vecMag();
    }
    public void vecMag()
    {
        this.mag = Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z);
    }
}

class Matrix
{
    int row;
    int col;
    double[][] matrix;
    public Matrix(int row, int column)
    {
        this.row = row;
        this.col = column;
        this.matrix = new double[row][column];
    }
    public void print()
    {
        for(int i = 0; i < this.row; ++i)
        {
            for(int j = 0; j < this.col; ++j)
            {
                System.out.print(this.matrix[i][j] + " ");
            }
            System.out.println();
        }
    }
    public void fillNum(int num)
    {
        for(int i = 0; i < this.row; ++i)
        {
            for(int j = 0; j < this.col; ++j)
            {
                this.matrix[i][j] = num;
            }
        }
    }
    public void fillAscending(int num)
    {
        for(int i = 0; i < this.row; ++i)
        {
            for(int j = 0; j < this.col; ++j)
            {
                this.matrix[i][j] = (this.col*i) + j + num;
            }
        }
    }
    // Ready to use 4x4 Matrix for 3D space
    public static Matrix identity()
    {
        Matrix matrix = new Matrix(4, 4);
        for(int i = 0; i < 4; ++i)
        {
            matrix.matrix[i][i] = 1;
        }
        return matrix;
    }
    public static Matrix point3D(double x, double y, double z)
    {
        Matrix matrix = new Matrix(4, 1);
        matrix.matrix[0][0] = x;
        matrix.matrix[1][0] = y;
        matrix.matrix[2][0] = z;
        matrix.matrix[3][0] = 1;
        return matrix;
    }
    public static Matrix point3D(Vector3 v)
    {
        Matrix matrix = new Matrix(4, 1);
        matrix.matrix[0][0] = v.x;
        matrix.matrix[1][0] = v.y;
        matrix.matrix[2][0] = v.z;
        matrix.matrix[3][0] = 1;
        return matrix;
    }
    public static Matrix rotateX(double angle)
    {
        Matrix rotate = identity();
        double radian = Math.toRadians(angle);
        rotate.matrix[1][1] = Math.cos(radian);
        rotate.matrix[1][2] = -Math.sin(radian);
        rotate.matrix[2][1] = Math.sin(radian);
        rotate.matrix[2][2] = Math.cos(radian);
        return rotate;
    }
    public static Matrix rotateY(double angle)
    {
        Matrix rotate = identity();
        double radian = Math.toRadians(angle);
        rotate.matrix[0][0] = Math.cos(radian);
        rotate.matrix[0][2] = Math.sin(radian);
        rotate.matrix[2][0] = -Math.sin(radian);
        rotate.matrix[2][2] = Math.cos(radian);
        return rotate;
    }
    public static Matrix rotateZ(double angle)
    {
        Matrix rotate = identity();
        double radian = Math.toRadians(angle);
        rotate.matrix[0][0] = Math.cos(radian);
        rotate.matrix[0][1] = -Math.sin(radian);
        rotate.matrix[1][0] = Math.sin(radian);
        rotate.matrix[1][1] = Math.cos(radian);
        return rotate;
    }
    public Vector3 toVector()
    {
        assert this.row == 4;
        assert this.col == 1;
        return new Vector3(this.matrix[0][0], this.matrix[1][0], this.matrix[2][0]);
    }
    public static Matrix scale(double x, double y, double z)
    {
        Matrix matrix = Matrix.identity();
        matrix.matrix[0][0] = x;
        matrix.matrix[1][1] = y;
        matrix.matrix[2][2] = z;
        return matrix;
    }
    public static Matrix transform(double x, double y, double z)
    {
        Matrix matrix = Matrix.identity();
        matrix.matrix[0][3] = x;
        matrix.matrix[1][3] = y;
        matrix.matrix[2][3] = z;
        return matrix;
    }
}
class Camera
{
    double fov;
    double aspect;
    double near;
    double far;
    Matrix perspective;
    Vector3 position;
    public Camera(double fov, double aspect, double near, double far)
    {
        this.fov = fov;
        this.aspect = aspect;
        this.near = near;
        this.far = far;
        this.position = new Vector3(0, 0, 0);
        this.perspective = camProject();
    }
    public Matrix camProject()
    {
        Matrix proj = new Matrix(4, 4);
        double f = 1.0 / Math.tan(Math.toRadians(this.fov) / 2);

        proj.matrix[0][0] = f / this.aspect;
        proj.matrix[1][1] = f;
        proj.matrix[2][2] = (far + near) / (near - far);
        proj.matrix[2][3] = (2 * far * near) / (near - far);
        proj.matrix[3][2] = - 1;
        proj.matrix[3][3] = 0;
        return proj;
    }
    public static Camera negativeCamera(Camera c)
    {
        return new Camera(-c.fov, -c.aspect, -c.near, -c.far);
    }

}

class Mat
{
    public static Vector2 vecDot(Vector2 v1, Vector2 v2)
    {
        Vector2 res = new Vector2(0, 0);
        res.x = v1.x * v2.x;
        res.y = v1.y * v2.y;
        res.vecMag();
        return res;
    }
    public static Vector3 vecDot(Vector3 v1, Vector3 v2)
    {
        Vector3 res = new Vector3(0, 0, 0);
        res.x = v1.x * v2.x;
        res.y = v1.y * v2.y;
        res.z = v1.z * v2.z;
        res.vecMag();
        return res;
    }
    public static double vecDotNum(Vector3 v1, Vector3 v2)
    {
        return (v1.x * v2.x + v1.y * v2.y + v1.z * v2.z);
    }
    public static Vector3 vecCross(Vector2 v1, Vector2 v2)
    {
        Vector3 res = new Vector3(0,0,0);
        res.z = (v1.x * v2.y) - (v1.y * v2.x);
        res.vecMag();
        return res;
    }
    public static Vector3 vecCross(Vector3 v1, Vector3 v2)
    {
        Vector3 res = new Vector3(0,0,0);
        res.x = (v1.y * v2.z) - (v1.z * v2.y);
        res.y = (v1.z * v2.x) - (v1.x * v2.z);
        res.z = (v1.x * v2.y) - (v1.y * v2.x);
        res.vecMag();
        return res;
    }
    public static void vecNormalize(Vector3 v)
    {
        if(v.mag > 0)
        {
            v.x /= v.mag;
            v.y /= v.mag;
            v.z /= v.mag;
            v.vecMag();
        }
    }
    public static void vecNormalize(Vector2 v)
    {
        if(v.mag > 0)
        {
            v.x /= v.mag;
            v.y /= v.mag;
            v.vecMag();
        }
    }
    public static Vector2 vecAdd(Vector2 v1, Vector2 v2)
    {
        Vector2 res = new Vector2(0, 0);
        res.x = v1.x + v2.x;
        res.y = v1.y + v2.y;
        res.vecMag();
        return res;
    }
    public static Vector3 vecAdd(Vector3 v1, Vector3 v2)
    {
        Vector3 res = new Vector3(0, 0, 0);
        res.x = v1.x + v2.x;
        res.y = v1.y + v2.y;
        res.z = v1.z + v2.z;
        res.vecMag();
        return res;
    }
    public static Vector2 vecSub(Vector2 v1, Vector2 v2)
    {
        Vector2 res = new Vector2(0, 0);
        res.x = v1.x - v2.x;
        res.y = v1.y - v2.y;
        res.vecMag();
        return res;
    }
    public static Vector3 vecSub(Vector3 v1, Vector3 v2)
    {
        Vector3 res = new Vector3(0, 0, 0);
        res.x = v1.x - v2.x;
        res.y = v1.y - v2.y;
        res.z = v1.z - v2.z;
        res.vecMag();
        return res;
    }
    public static void vecScalar(Vector2 v, double val)
    {
        v.x *= val;
        v.y *= val;
        v.vecMag();
    }
    public static void vecScalar(Vector3 v, double val)
    {
        v.x *= val;
        v.y *= val;
        v.z *= val;
        v.vecMag();
    }
    public static Vector3 vecNormal(Vector3 v1, Vector3 v2)
    {
        Vector3 cross = vecCross(v1, v2);
        Vector3 dot = vecDot(v1, v2);
        double dotVal = dot.x + dot.y + dot.z;
        dotVal = Math.sqrt((v1.mag*v1.mag) * (v2.mag * v2.mag) - (dotVal * dotVal));
        return new Vector3(cross.x /dotVal, cross.y / dotVal, cross.z / dotVal);
    }

    public static Matrix matrixAdd(Matrix m1, Matrix m2)
    {
        assert m1.row == m2.row && m1.col == m2.col;
        Matrix res = new Matrix(m1.row, m1.col);
        res.fillNum(0);
        for(int i = 0; i < m1.row; ++i){
            for(int j = 0; j < m1.col; ++j)
            {
                res.matrix[i][j] = m1.matrix[i][j] + m2.matrix[i][j];
            }
        }
        return res;
    }

    public static Matrix matrixSub(Matrix m1, Matrix m2)
    {
        assert m1.row == m2.row && m1.col == m2.col;
        Matrix res = new Matrix(m1.row, m1.col);
        res.fillNum(0);
        for(int i = 0; i < m1.row; ++i){
            for(int j = 0; j < m1.col; ++j)
            {
                res.matrix[i][j] = m1.matrix[i][j] - m2.matrix[i][j];
            }
        }
        return res;
    }
    public static Matrix matrixMul(Matrix m1, Matrix m2)
    {
        assert m1.col == m2.row;
        Matrix res = new Matrix(m1.row, m2.col);
        res.fillNum(0);
        for(int i = 0; i < res.row; ++i)
        {
            for(int j = 0; j < res.col; ++j)
            {
                for(int k = 0; k < m1.col; ++k)
                {
                    res.matrix[i][j] += m1.matrix[i][k] * m2.matrix[k][j];
                }
            }
        }
        return res;
    }
    public static Matrix matrixScale(Matrix m, double sx, double sy, double sz)
    {
        assert m.row == 4 && m.col == 1;
        Matrix scale = Matrix.identity();
        scale.matrix[0][0] = sx;
        scale.matrix[1][1] = sy;
        scale.matrix[2][2] = sz;
        m = matrixMul(scale, m);
        return m;
    }
    public static Matrix matrixTrans(Matrix m, double tx, double ty, double tz)
    {
        assert m.row == 4 && m.col == 1;
        Matrix trans = Matrix.identity();
        trans.matrix[0][3] = tx;
        trans.matrix[1][3] = ty;
        trans.matrix[2][3] = tz;
        m = matrixMul(trans, m);
        return m;
    }
    public static Matrix matrixRotateX(Matrix m, double angle)
    {
        assert m.row == 4 && m.col == 1;
        Matrix rotate = Matrix.identity();
        double radian = Math.toRadians(angle);
        rotate.matrix[1][1] = Math.cos(radian);
        rotate.matrix[1][2] = -Math.sin(radian);
        rotate.matrix[2][1] = Math.sin(radian);
        rotate.matrix[2][2] = Math.cos(radian);
        m = matrixMul(rotate, m);
        return m;
    }
    public static Matrix matrixRotateY(Matrix m, double angle)
    {
        assert m.row == 4 && m.col == 1;
        Matrix rotate = Matrix.identity();
        double radian = Math.toRadians(angle);
        rotate.matrix[0][0] = Math.cos(radian);
        rotate.matrix[0][2] = Math.sin(radian);
        rotate.matrix[2][0] = -Math.sin(radian);
        rotate.matrix[2][2] = Math.cos(radian);
        m = matrixMul(rotate, m);
        return m;
    }
    public static Matrix matrixRotateZ(Matrix m, double angle)
    {
        assert m.row == 4 && m.col == 1;
        Matrix rotate = Matrix.identity();
        double radian = Math.toRadians(angle);
        rotate.matrix[0][0] = Math.cos(radian);
        rotate.matrix[0][1] = -Math.sin(radian);
        rotate.matrix[1][0] = Math.sin(radian);
        rotate.matrix[1][1] = Math.cos(radian);
        m = matrixMul(rotate, m);
        return m;
    }
    public static Vector2 project2D(Matrix point3D)
    {
        double pointZ = point3D.matrix[2][0];
        double x = point3D.matrix[0][0] / pointZ;
        double y = point3D.matrix[1][0] / pointZ;
        return new Vector2(x, y);
    }
}