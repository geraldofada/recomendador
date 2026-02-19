package br.uff.ic.recomendador.business

import br.uff.ic.recomendador.domain.models.FoodPairing
import br.uff.ic.recomendador.domain.models.FoodSensoryProfile
import br.uff.ic.recomendador.domain.models.Name
import br.uff.ic.recomendador.domain.models.WinePairing
import br.uff.ic.recomendador.domain.models.WineSensoryProfile
import br.uff.ic.recomendador.domain.repositories.FoodRepository
import br.uff.ic.recomendador.domain.repositories.RecommendationRepository
import br.uff.ic.recomendador.domain.repositories.WineRepository
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import com.netflix.graphql.dgs.InputArgument
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@DgsComponent
class WineRecommendation(
    @param:Autowired private val wineRepository: WineRepository,
    @param:Autowired private val foodRepository: FoodRepository,
    @param:Autowired private val recommendationRepository: RecommendationRepository
) {
    init {
        println("=== WineRecommendation initialized ===")
        println("recommendationRepository: $recommendationRepository")
    }

    @DgsQuery(field = "wine")
    fun getWineByName(@InputArgument name: Name) = wineRepository.getWineByName(name)

    @DgsQuery(field = "food")
    fun getFoodByName(@InputArgument name: Name) = foodRepository.getFoodByName(name)

    @DgsQuery(field = "dish")
    fun getDishByName(@InputArgument name: Name) = foodRepository.getFoodByName(name)

    @DgsQuery(field = "recommendWinesForDish")
    fun recommendWinesForDish(@InputArgument dish: Name): List<WinePairing> {
        println(">>> recommendWinesForDish called with: ${dish.value}")
        val result = recommendationRepository.recommendWinesForDish(dish)
        println(">>> recommendWinesForDish returned: ${result.size} items")
        return result
    }

    @DgsQuery(field = "recommendDishesForWine")
    fun recommendDishesForWine(@InputArgument wine: Name): List<FoodPairing> {
        return recommendationRepository.recommendDishesForWine(wine)
    }

    @DgsQuery(field = "wineSensoryProfile")
    fun getWineSensoryProfile(@InputArgument wine: Name): WineSensoryProfile? {
        return recommendationRepository.getWineSensoryProfile(wine)
    }

    @DgsQuery(field = "foodSensoryProfile")
    fun getFoodSensoryProfile(@InputArgument food: Name): FoodSensoryProfile? {
        return recommendationRepository.getFoodSensoryProfile(food)
    }
}
