package WebRTC.Proj

import io.micronaut.runtime.Micronaut.*

fun main(args: Array<String>) {
	build().args(*args)
		.packages("WebRTC.Server")
		.start()
}



