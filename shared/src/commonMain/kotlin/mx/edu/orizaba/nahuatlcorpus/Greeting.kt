package mx.edu.orizaba.nahuatlcorpus

class Greeting {
    private val platform = getPlatform()

    fun greet(): String {
        return sayHello(platform.name)
    }
}