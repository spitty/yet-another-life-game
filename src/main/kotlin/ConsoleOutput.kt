import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.h1
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onMouseDownFunction
import kotlinx.html.js.onMouseMoveFunction
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import kotlin.browser.document
import kotlin.dom.clear
import kotlin.math.floor
import kotlin.math.round

fun main(args: Array<String>) {
    println("Hello JavaScript!")
    for (a in 1..5)
        println("var a=${a}")

    document.addEventListener("DOMContentLoaded", {
        Application().start()
    })
}

val Double.px get() = "${this}px"

private fun HTMLElement.setSize(w: Double, h: Double) {
    with(style) {
        width = w.px
        height = h.px
    }
}

private fun HTMLElement.setPosition(x: Double, y: Double) {
    with(style) {
        left = x.px
        top = y.px
    }
}

class Application {
    private val body get() = document.body!!
    private val scene get() = document.getElementById("scene") as HTMLElement

    private val gridHSize = 30
    private val gridVSize = 20
    private val cellSize = 30.0
    private val margin = cellSize / 2

    private val sw = cellSize * gridHSize
    private val sh = cellSize * gridVSize

    private var mouseX = 0.0
    private var mouseY = 0.0
    private var field = Array(gridVSize, { _ -> IntArray(gridHSize, { _ -> 0 }) })

    fun start() {
        body.append.div("content") {
            h1 {
                +"Kotlin JS Example"
            }
            div {
                button {
                    +"Say 'Hi!'"
                    onClickFunction = { sayHi() }
                }
            }
            div {
                id = "scene"
                onMouseMoveFunction = { event: Event -> updateCoords(event) }
                onMouseDownFunction = { event -> onClick(event) }
            }
        }
        scene.setSize(sw, sh)
        redraw()
    }

    private fun onClick(event: Event) {
        if (event !is MouseEvent) {
            return
        }
        val mousePoint = eventToCoords(event)
        val (x, y) = mousePoint
        println("Mouse clicked ($x, $y)")
        val (intX, intY) = mousePoint.floorTo(cellSize)
        println("Mouse clicked ($intX, $intY)")
        field[intY][intX] = 1 - field[intY][intX]
        redraw()
    }

    private fun redraw() {
        scene.clear()
        // draw field
        for (i in field.indices) {
            for (j in field[i].indices) {
                if (field[i][j] != 0) {
                    val rect = scene.append.div("rect")
                    rect.setSize(cellSize, cellSize)
                    rect.setPosition(j * cellSize, i * cellSize)
                }
            }
        }
        val aim = scene.append.div("aim")
        aim.setSize(cellSize, cellSize)
        // x, y define top left corner. Make corrections
        val realWidth = aim.offsetWidth
        val realHeight = aim.offsetHeight
        val newX = mouseX - realWidth / 2
        val newY = mouseY - realHeight / 2
        aim.setPosition(newX, newY)
    }

    private fun sayHi() {
        println("Hi!")
    }

    private fun updateCoords(event: Event) {
        if (event !is MouseEvent) {
            return
        }
        println("Hi, (${event.pageX}, ${event.pageY})")

        val (newX, newY) = eventToCoords(event)
        mouseX = newX
        mouseY = newY
        redraw()
    }

    private fun eventToCoords(event: MouseEvent): Point {
        val xRange = (0.0 + margin)..(sw - margin)
        val yRange = (0.0 + margin)..(sh - margin)
        var newX = event.pageX - scene.offsetLeft
        if (newX !in xRange) {
            newX = newX.coerceIn(xRange)
        }
        var newY = event.pageY - scene.offsetTop
        if (newY !in yRange) {
            newY = newY.coerceIn(yRange)
        }

        newX = round((newX - margin) / cellSize) * cellSize + margin
        newY = round((newY - margin) / cellSize) * cellSize + margin

        return Point(newX, newY)
    }
}

data class Point(val x: Double, val y: Double)
data class IntPoint(val x: Int, val y: Int)

fun Point.floorTo(cellSize: Double): IntPoint {
    val newX = floor(x / cellSize).toInt()
    val newY = floor(y / cellSize).toInt()
    return IntPoint(newX, newY)
}
