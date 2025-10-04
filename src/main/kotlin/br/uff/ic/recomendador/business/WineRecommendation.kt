package br.uff.ic.recomendador.business

import br.uff.ic.recomendador.domain.models.Name
import br.uff.ic.recomendador.domain.repositories.WineRepository
import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsQuery
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
@DgsComponent
class WineRecommendation(
    @param:Autowired private val wineRepository: WineRepository
) {
    @DgsQuery
    fun getWine(name: Name) = wineRepository.getWineByName(name)
}