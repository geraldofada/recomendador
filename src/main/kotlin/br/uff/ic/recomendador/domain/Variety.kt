package br.uff.ic.recomendador.domain

data class Variety(
    val name: String,
    val colour: Colour,
    val flavors: List<Flavor>,
)
