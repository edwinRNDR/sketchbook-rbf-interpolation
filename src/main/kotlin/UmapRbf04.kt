import lib.rbfGaussian
import lib.rbfInterpolator
import lib.umapRbf
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.rgb
import org.openrndr.extra.camera.Camera2D
import org.openrndr.extra.color.spaces.OKHSV
import org.openrndr.extra.color.tools.shiftHue
import org.openrndr.extra.noise.primitives.random
import org.openrndr.extra.noise.uniform
import org.openrndr.extra.shapes.rectify.rectified
import org.openrndr.extra.triangulation.delaunayTriangulation
import org.openrndr.extra.triangulation.voronoiDiagram
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.bounds
import org.openrndr.shape.map
import tagbio.umap.Umap
import kotlin.math.exp
import kotlin.random.Random

fun main() {
    application {
        configure {
            width = 720
            height = 720
        }
        program {

            val data = List(200) {
                if (Double.uniform() < 0.5) {
                    drawer.bounds.offsetEdges(-Double.uniform(100.0, 200.0)).contour
                } else {
                    Circle(drawer.bounds.center, Double.uniform(100.0, 360.0)).contour
                }
            }.map {
                it.rectified().let { c ->
                    (0 until 32).flatMap {
                        c.position(it / 32.0).let {
                            listOf(it.x, it.y)
                        }
                    }
                }
            }.map { it.toDoubleArray() }.toTypedArray()



            val rbf = data.umapRbf(
                0.1,
                drawer.bounds,
                120,
                100,
                0,
                rbfGaussian((0.001))
            )
            extend(Camera2D())
            extend {
                drawer.stroke = null
                drawer.circles(rbf.points, 2.0)
                val values = rbf.interpolate(mouse.position)

                val points = values.toList().windowed(2,2).map {
                    Vector2(it[0], it[1])
                }
                val contour = ShapeContour.fromPoints(points, true)

                drawer.stroke = ColorRGBa.WHITE
                drawer.fill = null
                drawer.contour(contour)


            }
        }
    }
}