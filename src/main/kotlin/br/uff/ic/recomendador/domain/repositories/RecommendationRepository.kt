package br.uff.ic.recomendador.domain.repositories

import br.uff.ic.recomendador.domain.models.Name
import br.uff.ic.recomendador.main.codegen.types.Wine

interface RecommendationRepository {
    fun recommendWinesForCategory(categoryName: Name): List<Wine>
    fun recommendWinesForDish(dishName: Name): List<Wine>
}
