package br.uff.ic.recomendador.business

import br.uff.ic.recomendador.domain.models.Name
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
    @param:Autowired private val recommendationRepository: RecommendationRepository
) {
    @DgsQuery(field = "wine")
    fun getWineByName(@InputArgument name: Name) = wineRepository.getWineByName(name)

    @DgsQuery(field = "recommendWineByCategory")
    fun recommendWineByCategory(@InputArgument category: Name) = 
        recommendationRepository.recommendWinesForCategory(category)

    @DgsQuery(field = "recommendWineByDish")
    fun recommendWineByDish(@InputArgument dish: Name) = 
        recommendationRepository.recommendWinesForDish(dish)
}