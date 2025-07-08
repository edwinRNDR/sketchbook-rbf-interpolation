package lib

class Matrix(val rows: Int, val cols: Int) {
    val data = Array(rows) { DoubleArray(cols) }

    operator fun get(i: Int, j: Int) = data[i][j]
    operator fun set(i: Int, j: Int, value: Double) {
        data[i][j] = value
    }

    fun copy(): Matrix {
        val result = Matrix(rows, cols)
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                result[i, j] = this[i, j]
            }
        }
        return result
    }


    /**
     * Checks if the given matrix is symmetric within a specified tolerance.
     *
     * A matrix is considered symmetric if it is square (same number of rows and columns)
     * and satisfies the condition matrix[i, j] == matrix[j, i] for all i and j, within
     * the given tolerance.
     *
     * @param matrix The matrix to be checked for symmetry.
     * @param tolerance The maximum allowable difference between matrix[i, j] and matrix[j, i]
     *                  to still consider the matrix symmetric. Default is 1e-10.
     * @return `true` if the matrix is symmetric within the specified tolerance, `false` otherwise.
     */
    fun isSymmetric(matrix: Matrix, tolerance: Double = 1e-10): Boolean {
        if (matrix.rows != matrix.cols) return false

        for (i in 0 until matrix.rows) {
            for (j in 0 until matrix.cols) {
                if (kotlin.math.abs(matrix[i, j] - matrix[j, i]) > tolerance) {
                    return false
                }
            }
        }
        return true
    }

    companion object {
        /**
         * Generates an identity matrix of the specified size.
         *
         * An identity matrix is a square matrix with ones on the diagonal
         * and zeros elsewhere.
         *
         * @param n The size of the identity matrix (number of rows and columns).
         *          Must be a positive integer.
         * @return A square matrix of size n x n, where all diagonal elements are 1
         *         and all non-diagonal elements are 0.
         */
        fun identity(n: Int): Matrix {
            val result = Matrix(n, n)
            for (i in 0 until n) {
                result[i, i] = 1.0
            }
            return result
        }

        /**
         * Creates a new matrix with the specified number of rows and columns, filled with zeros.
         *
         * @param rows The number of rows in the matrix.
         * @param cols The number of columns in the matrix.
         * @return A matrix with dimensions [rows] x [cols], initialized to zero.
         */
        fun zeros(rows: Int, cols: Int): Matrix {
            return Matrix(rows, cols)
        }
    }


    /**
     * Multiplies two matrices and returns the resulting matrix.
     *
     * @param a The first matrix to be multiplied.
     * @param b The second matrix to be multiplied.
     * @return The resulting matrix after multiplying A and B.
     * @throws IllegalArgumentException If the number of columns in A does not match the number of rows in B.
     */
    fun multiply(a: Matrix, b: Matrix): Matrix {
        if (a.cols != b.rows) {
            throw IllegalArgumentException("Matrix dimensions don't match for multiplication")
        }

        val result = Matrix.zeros(a.rows, b.cols)
        for (i in 0 until a.rows) {
            for (j in 0 until b.cols) {
                for (k in 0 until a.cols) {
                    result[i, j] += a[i, k] * b[k, j]
                }
            }
        }
        return result
    }

    /**
     * Multiplies this matrix with another matrix and returns the resulting matrix.
     *
     * @param other The matrix to multiply with this matrix.
     * @return The resulting matrix after multiplication.
     * @throws IllegalArgumentException If the number of columns in this matrix does not match the number of rows in the other matrix.
     */
    operator fun times(other: Matrix): Matrix {
        return multiply(this, other)
    }

}