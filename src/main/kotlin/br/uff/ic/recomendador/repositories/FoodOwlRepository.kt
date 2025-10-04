package br.uff.ic.recomendador.repositories

import br.uff.ic.recomendador.domain.models.Name
import br.uff.ic.recomendador.domain.repositories.FoodRepository
import br.uff.ic.recomendador.main.codegen.types.Food

class FoodOwlRepository: FoodRepository {
    override fun getFoodByName(name: Name) = Food(
        id = "teste",
        name = name,
        flavors = listOf(),
    )
}