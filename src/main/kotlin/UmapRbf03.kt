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
import org.openrndr.extra.triangulation.delaunayTriangulation
import org.openrndr.extra.triangulation.voronoiDiagram
import org.openrndr.math.Vector2
import org.openrndr.shape.bounds
import org.openrndr.shape.map
import tagbio.umap.Umap
import kotlin.math.exp

fun main() {
    application {
        configure {
            width = 720
            height = 720
        }
        program {

            // sample from multimodal distribution
            val data = Array(240) {
                if (Double.uniform() < 0.5) {
                    doubleArrayOf(
                        Math.random() * 0.5,
                        Math.random() * 0.5,
                        Math.random() * 0.25,
                        Math.random(),
                        Math.random(),
                        Math.random()
                    )
                } else {
                    doubleArrayOf(
                        Math.random(),
                        Math.random(),
                        Math.random(),
                        Math.random() * 0.25f,
                        Math.random() * 0.5f,
                        Math.random() * 0.5f
                    )
                }
            }

            val rbf = data.umapRbf(
                0.0,
                drawer.bounds,
                15,
                100,
                0,
                rbfGaussian((0.001))
            )
            extend(Camera2D())
            extend {
                drawer.stroke = null
                drawer.circles(rbf.points, 2.0)
                val values = rbf.interpolate(mouse.position)

                drawer.rectangles {
                    for (y in 0 until height step 15) {
                        for (x in 0 until width step 15) {

                            val p = Vector2(x.toDouble(), y.toDouble())
                            val values = rbf.interpolate(p)

                            for (i in values.indices) {
                                val v = values[i]
                                fill = ColorRGBa.RED.shiftHue<OKHSV>(i * 60.0)
                                stroke = null
                                rectangle(p + Vector2(i * 2.0, 0.0), 1.0, -v * 10.0)
                                //drawer.lineSegment(p + Vector2(i * 2.0, 0.0), p + Vector2(i * 2.0, -v*10.0))
                            }
                        }
                    }
                }


                for (i in values.indices) {
                    val v = values[i]
                    drawer.stroke = ColorRGBa.RED
                    drawer.lineSegment(
                        mouse.position + Vector2(i * 5.0, 0.0),
                        mouse.position + Vector2(i * 5.0, -v * 100.0)
                    )
                }
            }
        }
    }
}