package br.uff.ic.recomendador.domain.models

data class FoodSensoryProfile(
    val fatness: String,
    val saltiness: String,
    val sweetness: String,
    val spiciness: String,
    val umami: String
)
