package br.uff.ic.recomendador.domain

enum class RatingType(val value: String, val minimum: Int, val maximum: Int) {
    FiveStar("Five Star rating", 1, 5),
    TwentyPoints("Twenty-points rating", 1, 20),
}

data class Rating(
    val type: RatingType,
    val rating: Int,
    val minimum: Int,
    val maximum: Int,
) {
    constructor(rating: Int) : this(
        type = RatingType.FiveStar,
        rating = rating,
        minimum = 1,
        maximum = 5,
    )

    constructor(type: RatingType, rating: Int) : this(
        type = type,
        rating = rating,
        minimum = type.minimum,
        maximum = type.maximum,
    )
}