package mx.edu.orizaba.nahuatlcorpus

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform