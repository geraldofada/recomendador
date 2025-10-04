package br.uff.ic.recomendador.domain.repositories

import br.uff.ic.recomendador.domain.models.Name
import br.uff.ic.recomendador.main.codegen.types.Food

interface FoodRepository {
    fun getFoodByName(name: Name): Food
}