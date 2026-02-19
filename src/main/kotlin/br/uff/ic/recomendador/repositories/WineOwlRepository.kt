package br.uff.ic.recomendador.repositories

import br.uff.ic.recomendador.domain.models.Name
import br.uff.ic.recomendador.domain.repositories.WineRepository
import br.uff.ic.recomendador.domain.utils.WineColourMapper
import br.uff.ic.recomendador.main.codegen.types.Wine
import org.apache.jena.ontology.OntModel
import org.apache.jena.query.QueryExecution
import org.apache.jena.query.QueryFactory
import org.springframework.stereotype.Repository

@Repository
class WineOwlRepository(
    private val recommendationOntologyModel: OntModel
) : WineRepository {

    override fun getWineByName(name: Name): Wine? {
        val wineUri = "http://uff.ic.br/ontologias/recomendador/wine/${name.value}"

        val query = QueryFactory.create(
            """
            PREFIX vin: <http://uff.ic.br/ontologias/recomendador/wine/>
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            
            SELECT ?color
            WHERE {
                <$wineUri> rdf:type ?type .
                OPTIONAL { <$wineUri> vin:hasColor ?color }
            }
            LIMIT 1
            """.trimIndent()
        )

        QueryExecution.create(query, recommendationOntologyModel).use { qexec ->
            val results = qexec.execSelect()
            if (results.hasNext()) {
                val solution = results.next()
                val colorUri = solution.getResource("color")?.uri

                return Wine(
                    id = name.value,
                    name = name,
                    colour = WineColourMapper.fromColorUri(colorUri),
                    flavors = emptyList()
                )
            }
        }

        return null
    }
}