import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.awaitAnimationFrame
import kotlinx.coroutines.experimental.launch
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.dom.append
import kotlinx.html.h1
import kotlinx.html.id
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onMouseDownFunction
import kotlinx.html.js.onMouseMoveFunction
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import kotlin.browser.document
import kotlin.browser.window
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

enum class PlayState {
    PLAY, PAUSE
}

class Application {
    private val body get() = document.body!!
    private val scene get() = document.getElementById("scene") as HTMLElement
    private val startButton get() = document.getElementById("startButton") as HTMLButtonElement
    private val stopButton get() = document.getElementById("stopButton") as HTMLButtonElement
    private val speedometer get() = document.getElementById("speedometer") as HTMLElement

    private val gridVSize = 20
    private val gridHSize = 30
    private val vRange = 0..(gridVSize - 1)
    private val hRange = 0..(gridHSize - 1)
    private val cellSize = 30.0
    private val margin = cellSize / 2

    private val sw = cellSize * gridHSize
    private val sh = cellSize * gridVSize

    private var mouseX = margin
    private var mouseY = margin
    private var field = Array(gridVSize, { _ -> IntArray(gridHSize, { _ -> 0 }) })

    private val topSpeed = 10
    private var speed = 0
    private var animation: Job? = null
    private var playState: PlayState = PlayState.PAUSE

    fun start() {
        body.append.div("content") {
            h1 {
                +"Yet Another Game of Life (Kotlin JS Example)"
            }
            div {
                button {
                    +"Start"
                    id = "startButton"
                    onClickFunction = { startLife() }
                }
                button {
                    +"Stop"
                    id = "stopButton"
                    onClickFunction = { stopLife() }
                }
            }
            div {
                +"Speed:"
                div {
                    id = "speedometer"
                }
                button {
                    +"-"
                    onClickFunction = { speedDown() }
                }
                button {
                    +"+"
                    onClickFunction = { speedUp() }
                }
            }
            div {
                id = "scene"
                onMouseMoveFunction = { event: Event -> updateCoords(event) }
                onMouseDownFunction = { event -> onClick(event) }
            }
        }
        scene.setSize(sw, sh)
        stopButton.disabled = true
        updateSpeed(5)
        redraw()
    }

    private fun updateSpeed(newSpeed: Int) {
        if (newSpeed !in (0..topSpeed)) {
            return
        }
        speed = newSpeed
        speedometer.innerText = "$speed"
    }

    private fun speedUp() {
        updateSpeed(speed + 1)
    }

    private fun speedDown() {
        if (speed <= 1) {
            return
        }
        updateSpeed(speed - 1)
    }

    private fun startLife() {
        if (!trySetPlayState(PlayState.PLAY)) {
            return
        }
        println("startLife")
        animation = launch {
            val timer = AnimationTimer()
            while (true) {
                val dt = timer.await(1000.0 / speed)
                println("Spent ${dt}ms")
                calcNextState()
                redraw()
            }
        }
    }

    private fun stopLife() {
        if (!trySetPlayState(PlayState.PAUSE)) {
            return
        }
        println("stopLife")
        if (animation != null) {
            // double check for null
            animation!!.cancel()
        }
    }

    /**
     * Change playState, update 'start'/'stop' buttons states, and return 'true' if state can be changed.
     * Returns 'false' if nothing happened.
     */
    private fun trySetPlayState(state: PlayState): Boolean {
        if (playState == state) {
            return false
        }
        playState = state
        updateButtonStates()
        return true
    }

    private fun updateButtonStates() {
        startButton.disabled = playState == PlayState.PLAY
        stopButton.disabled = playState == PlayState.PAUSE
    }

    private fun calcNextState() {
        val newField = Array(gridVSize, { _ -> IntArray(gridHSize, { _ -> 0 }) })
        for (y in vRange) {
            for (x in hRange) {
                val cur = IntPoint(x, y)
                val neighbors = calcNeighbors(cur)
                val state = currentState(cur)
                val newState =
                        if (state == 0 && neighbors == 3) {
                            1
                        } else if (state == 1 && neighbors in 2..3) {
                            1
                        } else {
                            0
                        }
                newField[y][x] = newState
            }
        }

        field = newField
    }

    private fun currentState(p: IntPoint): Int {
        return field[p.y][p.x]
    }

    private fun calcNeighbors(p: IntPoint): Int {
        val localXRange = (p.x - 1)..(p.x + 1)
        val localYRange = (p.y - 1)..(p.y + 1)
        var count = 0
        for (x in localXRange) {
            for (y in localYRange) {
                if (x !in hRange || y !in vRange || (x == p.x && y == p.y)) {
                    continue
                }
                if (field[y][x] != 0) {
                    count++
                }
            }
        }
        return count
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

    /**
     * Translate MouseEvent coordinates to coordinates related to 'scene'
     */
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

class AnimationTimer {
    var time = window.performance.now()

    suspend fun await(): Double {
        val newTime = window.awaitAnimationFrame()
        val dt = newTime - time
        time = newTime
        return dt
    }

    suspend fun await(interval: Double): Double {
        var newTime = window.awaitAnimationFrame()
        while (newTime - time < interval) {
            newTime = window.awaitAnimationFrame()
        }
        val dt = newTime - time
        time = newTime
        return dt
    }

    fun reset(): Double {
        time = window.performance.now()
        return time
    }
}
