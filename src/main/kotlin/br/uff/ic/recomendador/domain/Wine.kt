package br.uff.ic.recomendador.domain

import java.util.Date

data class Wine(
    val name: String,
    val colour: Colour,
    val flavors: List<Flavor>,
    val varieties: List<Variety>,
    val vintage: Date,
    val rating: Rating,
    val producers: List<Producer>,
    val regions: List<Region>,
)
