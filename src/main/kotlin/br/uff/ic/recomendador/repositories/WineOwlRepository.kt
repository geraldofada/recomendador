package br.uff.ic.recomendador.repositories

import br.uff.ic.recomendador.domain.models.Name
import br.uff.ic.recomendador.domain.repositories.WineRepository
import br.uff.ic.recomendador.main.codegen.types.Colour
import br.uff.ic.recomendador.main.codegen.types.Wine
import org.springframework.stereotype.Repository

@Repository
class WineOwlRepository : WineRepository {
    override fun getWineByName(name: Name) = Wine(
        id = "teste",
        name = name,
        colour = Colour.WHITE,
        flavors = listOf(),
    )
}