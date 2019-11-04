import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitAnimationFrame
import kotlinx.coroutines.launch
import kotlinx.html.*
import kotlinx.html.dom.append
import kotlinx.html.js.*
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import kotlin.browser.document
import kotlin.browser.window
import kotlin.coroutines.CoroutineContext
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
val Int.switch get() = 1 - this

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

class Application : CoroutineScope {
    private val body get() = document.body!!
    private val scene get() = document.getElementById("scene") as HTMLElement
    private val startStopButton get() = document.getElementById("startStopButton") as HTMLButtonElement
    private val speedometer get() = document.getElementById("speedometer") as HTMLElement

    private val gridVSize = 40
    private val gridHSize = 40
    private val vRange = 0..(gridVSize - 1)
    private val hRange = 0..(gridHSize - 1)
    private val cellSize = 15.0
    private val margin = cellSize / 2

    private val sw = cellSize * gridHSize
    private val sh = cellSize * gridVSize

    private var mouseX = margin
    private var mouseY = margin
    private var field = GameField(gridVSize, gridHSize)

    private val topSpeed = 10
    private var speed = 0
    private var playState: PlayState = PlayState.PAUSE

    private var animation: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = animation

    enum class DragState {
        OFF, FILL, CLEAR
    }

    private var dragState = DragState.OFF

    fun start() {
        body.append.div("content") {
            h1 {
                +"Yet Another Game of Life (Kotlin JS Example)"
            }
            div {
                button {
                    +"Start"
                    id = "startStopButton"
                    onClickFunction = { startStopLife() }
                }
                button {
                    +"Clear"
                    id = "clearButton"
                    onClickFunction = { clear() }
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
                onMouseMoveFunction = { event -> updateCoords(event) }
                onMouseDownFunction = { event -> startDrag(event) }
                // todo: currently "mouseOut" occurs on moving to div "aim"
//                onMouseOutFunction = { event -> stopDrag(event) }
                onMouseUpFunction = { event -> stopDrag(event) }
            }
        }
        scene.setSize(sw, sh)
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

    private fun startStopLife() {
        if (playState != PlayState.PLAY) {
            startLife()
        } else {
            stopLife()
        }
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
        animation.cancel()
    }

    /**
     * Change playState, update 'start'/'stop' buttons states, and return 'true' if state can be changed.
     * Returns 'false' if nothing happened.
     */
    private fun trySetPlayState(state: PlayState): Boolean {
        playState = if (playState != state) state else return false
        updateButtonStates()
        return true
    }

    private fun updateButtonStates() {
        startStopButton.textContent = when (playState) {
            PlayState.PAUSE -> "Start"
            PlayState.PLAY -> "Stop"
        }
    }

    private fun clear() {
        for (i in 0..field.hSize) {
            for (j in 0..field.vSize) {
                field[i, j] = 0
            }
        }
        redraw()
        // nothing to do with empty field
        stopLife()
    }

    private fun calcNextState() {
        val newField = GameField(gridVSize, gridHSize)
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
                newField[cur] = newState
            }
        }

        field = newField
    }

    private fun currentState(p: IntPoint): Int {
        return field[p]
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
                if (field[x, y] != 0) {
                    count++
                }
            }
        }
        return count
    }

    private fun switchCellState(event: Event) {
        if (event !is MouseEvent) {
            return
        }
        val mousePoint = eventToCoords(event)
        switchStateOf(mousePoint)
        redraw()
    }

    private fun getStateOf(point: Point): Int {
        val intPoint = point.floorTo(cellSize)
        return field[intPoint]
    }

    private fun setStateOf(point: Point, newState: Int) {
        val intPoint = point.floorTo(cellSize)
        field[intPoint] = newState
    }

    private fun switchStateOf(point: Point) {
        val newState = getStateOf(point).switch
        setStateOf(point, newState)
    }

    private fun startDrag(event: Event) {
        if (event !is MouseEvent || dragState != DragState.OFF) {
            return
        }
        val point = eventToCoords(event)
        val filler = getStateOf(point).switch
        dragState = if (filler == 1) {
            DragState.FILL
        } else {
            DragState.CLEAR
        }
        println("startDrag($dragState)")
        switchCellState(event)
    }

    private fun stopDrag(event: Event) {
        if (event !is MouseEvent || dragState == DragState.OFF) {
            return
        }
        println("stopDrag($event)")
        dragState = DragState.OFF
    }

    private fun redraw() {
        scene.clear()
        // draw field
        for (x in 0..(field.vSize - 1)) {
            for (y in 0..(field.hSize - 1)) {
                if (field[x, y] != 0) {
                    val rect = scene.append.div("rect")
                    rect.setSize(cellSize, cellSize)
                    rect.setPosition(x * cellSize, y * cellSize)
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

        val point = eventToCoords(event)
        setMouseCoords(point)
        val newStateOfCell =
                when (dragState) {
                    DragState.CLEAR -> 0
                    DragState.FILL -> 1
                    else -> null
                }
        if (newStateOfCell != null) {
            setStateOf(point, newStateOfCell)
        }
        redraw()
    }

    private fun setMouseCoords(point: Point) {
        val (newX, newY) = point
        mouseX = newX
        mouseY = newY
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

class GameField(val vSize: Int, val hSize: Int) {
    private val internArray = Array(vSize, { _ -> IntArray(hSize, { _ -> 0 }) })

    operator fun get(x: Int, y: Int): Int {
        if ((x + 1) !in (1..hSize) || (y + 1) !in (1..vSize)) {
            println("Out of bound request")
            return 0
        }
        return internArray[y][x]
    }

    operator fun get(p: IntPoint): Int {
        return this[p.x, p.y]
    }

    operator fun set(x: Int, y: Int, value: Int) {
        internArray[y][x] = value
    }

    operator fun set(p: IntPoint, value: Int) {
        this[p.x, p.y] = value
    }
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
