import kotlinx.coroutines.experimental.*
import kotlinx.html.*
import kotlinx.html.div
import kotlinx.html.dom.*
import kotlinx.html.js.onClickFunction
import org.w3c.dom.*
import kotlin.browser.*
import kotlin.math.*

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

class Application {
    private val body get() = document.body!!
    private val scene get() = document.getElementById("scene") as HTMLElement
    private val sw = 800.0
    private val sh = 600.0

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
            }
        }
        scene.setSize(sw, sh)
    }

    private fun sayHi() {
    	println("Hi!")
    }
}
