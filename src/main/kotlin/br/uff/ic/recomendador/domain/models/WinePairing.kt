package br.uff.ic.recomendador.domain.models

import br.uff.ic.recomendador.main.codegen.types.Wine

data class WinePairing(
    val wine: Wine,
    val score: Float,
    val explanation: String?
)
