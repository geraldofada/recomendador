package br.uff.ic.recomendador.domain.repositories

import br.uff.ic.recomendador.domain.models.Name
import br.uff.ic.recomendador.main.codegen.types.Wine

interface WineRepository {
    fun getWineByName(name: Name): Wine
}