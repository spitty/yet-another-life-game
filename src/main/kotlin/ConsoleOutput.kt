import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.h1
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onMouseMoveFunction
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import kotlin.browser.document
import kotlin.dom.clear
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
    private val sw = 600.0
    private val sh = 600.0

    private val aimSize = 30.0

    private var mouseX = 0.0
    private var mouseY = 0.0

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
                onClickFunction = { event -> onClick(event) }
            }
        }
        scene.setSize(sw, sh)
        redraw()
    }

    private fun onClick(event: Event) {
        if (event !is MouseEvent) {
            return
        }
        val (x, y) = eventToCoords(event)
        println("Mouse clicked ($x, $y)")
    }

    private fun redraw() {
        scene.clear()
        val elem = scene.append.div("aim")
        elem.setSize(aimSize, aimSize)
        // x, y define top left corner. Make corrections
        val realWidth = elem.offsetWidth
        val realHeight = elem.offsetHeight
        val newX = mouseX - realWidth / 2
        val newY = mouseY - realHeight / 2
        elem.setPosition(newX, newY)
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
        val margin = aimSize / 2
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

        newX = round((newX - margin) / aimSize) * aimSize + margin
        newY = round((newY - margin) / aimSize) * aimSize + margin

        return Point(newX, newY)
    }
}

data class Point(val x: Double, val y: Double)
