import lib.rbfGaussian
import lib.umapRbf
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.font.loadFace
import org.openrndr.extra.camera.Camera2D
import org.openrndr.extra.shapes.rectify.rectified
import org.openrndr.extra.shapes.text.shapesFromText
import org.openrndr.math.Vector2
import org.openrndr.shape.ShapeContour

fun main() {
    application {
        configure {
            width = 720
            height = 720
        }
        program {

            val face = loadFace("data/fonts/default.otf")

            val alphabet = "ABCDEFGHIJLKMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz"
            val data = List(alphabet.length) {

                shapesFromText(face, alphabet[it].toString(), 1000.0, drawer.bounds.position(0.2,0.8)).first().contours.first()

            }.map {
                it.rectified().let { c ->
                    (0 until 128).flatMap {
                        c.position(it / 128.0).let {
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
                rbfGaussian((0.0001))
            )
            extend(Camera2D())
            extend {
                drawer.stroke = null
                drawer.circles(rbf.points, 2.0)

                for (j in -3..3)
                for (i in -3..3) {
                    val values = rbf.interpolate(mouse.position + Vector2(i.toDouble(), j.toDouble())*0.1)

                    val points = values.toList().windowed(2, 2).map {
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
}