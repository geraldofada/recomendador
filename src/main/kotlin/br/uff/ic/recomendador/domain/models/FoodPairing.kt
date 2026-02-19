package br.uff.ic.recomendador.domain.models

import br.uff.ic.recomendador.main.codegen.types.Food

data class FoodPairing(
    val food: Food,
    val score: Float,
    val explanation: String?
)
