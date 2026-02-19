package br.uff.ic.recomendador.repositories

import br.uff.ic.recomendador.domain.models.Name
import br.uff.ic.recomendador.domain.repositories.FoodRepository
import br.uff.ic.recomendador.main.codegen.types.Food
import org.apache.jena.ontology.OntModel
import org.apache.jena.query.QueryExecution
import org.apache.jena.query.QueryFactory
import org.springframework.stereotype.Repository

@Repository
class FoodOwlRepository(
    private val recommendationOntologyModel: OntModel
) : FoodRepository {

    override fun getFoodByName(name: Name): Food? {
        val foodUri = "http://uff.ic.br/ontologias/recomendador/food/${name.value}"

        val query = QueryFactory.create(
            """
            PREFIX food: <http://uff.ic.br/ontologias/recomendador/food/>
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            
            SELECT ?type
            WHERE {
                <$foodUri> rdf:type ?type .
            }
            LIMIT 1
            """.trimIndent()
        )

        QueryExecution.create(query, recommendationOntologyModel).use { qexec ->
            val results = qexec.execSelect()
            if (results.hasNext()) {
                return Food(
                    id = name.value,
                    name = name,
                    flavors = emptyList()
                )
            }
        }

        return null
    }
}
