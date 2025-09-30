package br.uff.ic.recomendador.domain

data class Region(
    val name: String,
    val address: String,
    val country: String,
    val zipcode: String,
    val city: String,
    val state: String,
)
