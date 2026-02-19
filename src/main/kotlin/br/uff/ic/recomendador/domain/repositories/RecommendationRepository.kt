package br.uff.ic.recomendador.domain.repositories

import br.uff.ic.recomendador.domain.models.FoodPairing
import br.uff.ic.recomendador.domain.models.FoodSensoryProfile
import br.uff.ic.recomendador.domain.models.WinePairing
import br.uff.ic.recomendador.domain.models.WineSensoryProfile
import br.uff.ic.recomendador.domain.models.Name

interface RecommendationRepository {
    fun recommendWinesForDish(dishName: Name): List<WinePairing>
    fun recommendDishesForWine(wineName: Name): List<FoodPairing>
    fun getWineSensoryProfile(wineName: Name): WineSensoryProfile?
    fun getFoodSensoryProfile(foodName: Name): FoodSensoryProfile?
}
