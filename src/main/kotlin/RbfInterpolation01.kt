import org.openrndr.application
import org.openrndr.math.Vector2
import lib.Matrix
import lib.columnMean
import lib.invertMatrixCholesky
import lib.minus
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.shadeStyle
import org.openrndr.extensions.Screenshots
import org.openrndr.extra.camera.Camera2D
import org.openrndr.extra.color.spaces.OKHSV
import org.openrndr.extra.color.spaces.OKLab
import org.openrndr.extra.color.tools.shadeLuminosity
import org.openrndr.extra.color.tools.shiftHue
import org.openrndr.extra.noise.phrases.fhash12
import org.openrndr.extra.noise.uniform
import org.openrndr.math.Vector3
import kotlin.collections.indices
import kotlin.collections.map
import kotlin.collections.toTypedArray
import kotlin.math.exp
import kotlin.ranges.until
import kotlin.text.trimIndent
import kotlin.text.trimMargin

fun main() {
    application {
        configure {
            width = 720
            height = 720
        }
        program {
            val scale = 0.00004

            // A Gaussian RBG
            fun rbf(p0: Vector2, p1: Vector2): Double {
                return exp(-p0.squaredDistanceTo(p1) * scale)
            }

            //val pts = drawer.bounds.scatter(9.0)
            val pts = drawer.bounds.offsetEdges(-100.0).uniform(20)
            val rmat = Matrix(pts.size, pts.size)

            // build the rbf matrix and add regularization terms on the diagonal
            for (j in pts.indices) {
                for (i in pts.indices) {
                    rmat[i, j] = rbf(pts[i], pts[j]) + (if (i == j) 0.1 else 0.0)
                }
            }

            // invert the rbf matrix using Cholesky decomposition
            val imat = invertMatrixCholesky(rmat)

            val ogcmat = Matrix(pts.size, 3)

            val colors = (0 until pts.size).map {
                ColorRGBa.PINK.shiftHue<OKHSV>(Double.uniform(-180.0, 45.0)).shadeLuminosity<OKLab>(Double.uniform(0.7, 1.0))
            }
            for (j in pts.indices) {
                val c = colors[j]
                ogcmat[j, 0] = c.r
                ogcmat[j, 1] = c.g
                ogcmat[j, 2] = c.b
            }

            val mean = ogcmat.columnMean()
            println(mean.data[0].joinToString(", "))
            val cmat = ogcmat - mean


            // find the weights by multiplying colors by the rbf inverse
            val wmat = imat * cmat

            val weights = (0 until pts.size).map {
                Vector3(wmat[it, 0], wmat[it, 1], wmat[it, 2])
            }.toTypedArray()
            val ss = shadeStyle {
                fragmentPreamble = """${fhash12}
                    |float rbf(vec2 p, vec2 q) { 
                    |float d = distance(p, q);
                    |return exp(-d*d*$scale); }""".trimMargin()
                fragmentTransform = """
                    vec3 c = p_mean;
                    for (int i=0; i<p_weights_SIZE; ++i) {
                    
                        vec2 p = c_boundsPosition.xy;
                        vec2 o = vec2(fhash12(p), fhash12(-p)) * 10.0;     
                        float r = rbf(p_points[i], c_boundsPosition.xy * vec2(720.0, 720.0) + o);
                        
                        c.r += p_weights[i].r * r;
                        c.g += p_weights[i].g * r;
                        c.b += p_weights[i].b * r;
                                                                  
                    }
                    x_fill.rgb = c;
                    
                """.trimIndent()
                parameter("weights", weights)
                parameter("points", pts.toTypedArray())
                parameter("mean", Vector3(mean[0, 0], mean[0, 1], mean[0, 2]))
            }
            extend(Screenshots())
            extend(Camera2D())
            extend {
                drawer.shadeStyle = ss
                drawer.rectangle(drawer.bounds)

                drawer.shadeStyle = null
                drawer.fill = ColorRGBa.WHITE
                drawer.circles {
                    for (i in pts.indices) {
                        fill = colors[i]
                        circle(pts[i], 2.0)
                    }
                }
            }
        }
    }
}