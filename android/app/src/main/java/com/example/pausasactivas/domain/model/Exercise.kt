package com.example.pausasactivas.domain.model

/**
 * Ejercicio de calentamiento de la demo.
 *
 * La regla postural de cada ejercicio vive en [com.example.pausasactivas.domain.rules.PostureRules]
 * y se identifica con [codigo]. Por eso el catálogo es local: la app siempre muestra
 * exactamente estos tres ejercicios, haya o no datos precargados en MongoDB.
 */
data class Exercise(
    val codigo: String,
    val nombre: String,
    val resumen: String,
    val instrucciones: List<String>,
    val regla: String
)

object ExerciseCatalog {
    const val CUELLO_LATERAL = "CUELLO_LATERAL"
    const val ELEVACION_BRAZOS = "ELEVACION_BRAZOS"
    const val FLEXION_LATERAL_TRONCO = "FLEXION_LATERAL_TRONCO"

    val demo: List<Exercise> = listOf(
        Exercise(
            codigo = CUELLO_LATERAL,
            nombre = "Movilidad de cuello",
            resumen = "Inclinación lateral de la cabeza para aliviar la tensión del cuello.",
            instrucciones = listOf(
                "Ponte de frente a la cámara, con la espalda recta y los hombros relajados.",
                "Inclina lentamente la cabeza hacia un hombro, sin subir el hombro.",
                "Vuelve al centro y repite hacia el otro lado."
            ),
            regla = "Se mide la inclinación de la cabeza (entre 12° y 40°) y que los hombros " +
                "se mantengan nivelados."
        ),
        Exercise(
            codigo = ELEVACION_BRAZOS,
            nombre = "Elevación de brazos y hombros",
            resumen = "Sube y baja los brazos estirados para movilizar hombros y espalda alta.",
            instrucciones = listOf(
                "Ponte de frente a la cámara, lo bastante lejos para que se vean tus brazos.",
                "Sube ambos brazos estirados hasta pasar la altura de los hombros.",
                "Baja despacio y repite."
            ),
            regla = "Se mide que ambas muñecas pasen la altura de los hombros y que los codos " +
                "estén casi estirados (ángulo de 150° o más)."
        ),
        Exercise(
            codigo = FLEXION_LATERAL_TRONCO,
            nombre = "Flexión lateral de tronco",
            resumen = "Inclinación del tronco hacia los lados para estirar la cintura y la espalda.",
            instrucciones = listOf(
                "De pie o sentado con la espalda recta; aléjate para que se vean hombros y caderas.",
                "Inclina el tronco hacia un lado sin girar y sin mover la cadera.",
                "Vuelve al centro y cambia de lado."
            ),
            regla = "Se mide la inclinación del tronco respecto a la vertical (entre 12° y 40°) " +
                "y que la cadera permanezca nivelada."
        )
    )
}
