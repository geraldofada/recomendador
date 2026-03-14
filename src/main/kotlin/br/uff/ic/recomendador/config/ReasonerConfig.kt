package br.uff.ic.recomendador.config

import org.apache.jena.ontology.OntDocumentManager
import org.apache.jena.ontology.OntModel
import org.apache.jena.ontology.OntModelSpec
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.reasoner.ReasonerRegistry
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner
import org.apache.jena.reasoner.rulesys.Rule
import org.semanticweb.HermiT.ReasonerFactory
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.formats.TurtleDocumentFormat
import org.semanticweb.owlapi.io.StreamDocumentSource
import org.semanticweb.owlapi.model.IRI
import org.semanticweb.owlapi.reasoner.InferenceType
import org.semanticweb.owlapi.util.InferredClassAssertionAxiomGenerator
import org.semanticweb.owlapi.util.InferredOntologyGenerator
import org.semanticweb.owlapi.util.InferredPropertyAssertionGenerator
import org.semanticweb.owlapi.util.InferredSubClassAxiomGenerator
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@Configuration
class ReasonerConfig {

    @Value("classpath:ontologies/wine/wine-schema.owl")
    private lateinit var wineSchemaResource: Resource

    @Value("classpath:ontologies/wine/wine-instances.ttl")
    private lateinit var wineInstancesResource: Resource

    @Value("classpath:ontologies/food/food-schema.owl")
    private lateinit var foodSchemaResource: Resource

    @Value("classpath:ontologies/food/food-instances.ttl")
    private lateinit var foodInstancesResource: Resource

    @Value("classpath:ontologies/recommendation/recommendation-schema.owl")
    private lateinit var recommendationSchemaResource: Resource

    @Value("classpath:ontologies/recommendation/recommendation-instances.ttl")
    private lateinit var recommendationInstancesResource: Resource

    @Value("classpath:ontologies/pairing-rules.jena")
    private lateinit var pairingRulesResource: Resource

    @Value("\${reasoner.type:JENA_OWL}")
    private lateinit var reasonerTypeName: String

    @Bean
    fun recommendationOntologyModel(): OntModel {
        val type = ReasonerType.valueOf(reasonerTypeName.uppercase())
        println("=== Creating OntModel with reasoner: $type ===")
        val start = System.currentTimeMillis()

        val model = when (type) {
            ReasonerType.JENA_OWL -> buildJenaOwlModel()
            ReasonerType.HERMIT -> buildHermiTModel()
        }

        println("=== OntModel ready (${System.currentTimeMillis() - start}ms), size: ${model.size()} triples ===")
        return model
    }

    private fun buildJenaOwlModel(): OntModel {
        val docManager = OntDocumentManager()
        docManager.setCacheModels(false)
        docManager.reset()

        val spec = OntModelSpec(OntModelSpec.OWL_MEM)
        spec.documentManager = docManager
        spec.reasoner = ReasonerRegistry.getOWLReasoner()

        return ModelFactory.createOntologyModel(spec).also {
            loadOntologiesIntoJena(it)
            applyJenaPairingRules(it)
        }
    }

    private fun buildHermiTModel(): OntModel {
        // 1. Load all ontology files with OWL API into a single merged ontology
        val manager = OWLManager.createOWLOntologyManager()
        val merged = manager.createOntology(IRI.create("http://uff.ic.br/ontologias/recomendador/merged"))

        listOf(
            wineSchemaResource to "urn:wine-schema",
            wineInstancesResource to "urn:wine-instances",
            foodSchemaResource to "urn:food-schema",
            foodInstancesResource to "urn:food-instances",
            recommendationSchemaResource to "urn:recommendation-schema",
            recommendationInstancesResource to "urn:recommendation-instances",
        ).forEach { (resource, urn) ->
            resource.inputStream.use { stream ->
                val source = StreamDocumentSource(stream, IRI.create(urn))
                val loaded = manager.loadOntologyFromOntologyDocument(source)
                for (axiom in loaded.axioms) {
                    manager.addAxiom(merged, axiom)
                }
            }
        }

        // 2. Run HermiT and precompute inferences
        val hermit = ReasonerFactory().createReasoner(merged)
        hermit.precomputeInferences(
            InferenceType.CLASS_HIERARCHY,
            InferenceType.CLASS_ASSERTIONS,
            InferenceType.OBJECT_PROPERTY_ASSERTIONS
        )

        // 3. Materialize inferred axioms into the merged ontology
        val generators = listOf(
            InferredSubClassAxiomGenerator(),
            InferredClassAssertionAxiomGenerator(),
            InferredPropertyAssertionGenerator()
        )
        val inferredOntology = manager.createOntology()
        InferredOntologyGenerator(hermit, generators).fillOntology(manager.owlDataFactory, inferredOntology)
        for (axiom in inferredOntology.axioms) {
            manager.addAxiom(merged, axiom)
        }
        hermit.dispose()

        // 4. Serialize merged+inferred ontology and load into Jena model
        val output = ByteArrayOutputStream()
        manager.saveOntology(merged, TurtleDocumentFormat(), output)

        val jenaModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM)
        jenaModel.read(ByteArrayInputStream(output.toByteArray()), null, "TURTLE")

        // 5. Apply Jena pairing rules on top of HermiT-materialized model
        applyJenaPairingRules(jenaModel)

        return jenaModel
    }

    private fun applyJenaPairingRules(model: OntModel) {
        val rules = pairingRulesResource.inputStream.bufferedReader().use { reader ->
            Rule.parseRules(Rule.rulesParserFromReader(reader))
        }

        // Use baseModel to bypass OntModel's union-graph wrapper and expose raw triples to the rule engine
        val baseModel = model.baseModel
        println("=== [JenaRules] Base model size: ${baseModel.size()} triples ===")

        val reasoner = GenericRuleReasoner(rules)
        reasoner.setMode(GenericRuleReasoner.FORWARD_RETE)
        val infModel = ModelFactory.createInfModel(reasoner, baseModel)

        // Materialize only the pairing-specific properties back into the OntModel
        val recNS = "http://uff.ic.br/ontologias/recomendador/recommendation/"
        listOf("recommendsPairing", "notRecommended").forEach { propName ->
            val prop = infModel.createProperty("$recNS$propName")
            infModel.listStatements(null, prop, null as RDFNode?).toList()
                .forEach { stmt -> model.add(stmt) }
        }

        println("=== [JenaRules] Applied pairing rules: ${
            model.listStatements(null, model.createProperty("${recNS}recommendsPairing"), null as RDFNode?).toList().size
        } recommendsPairing triples ===")
    }

    private fun loadOntologiesIntoJena(model: OntModel) {
        model.read(wineSchemaResource.inputStream, "http://uff.ic.br/ontologias/recomendador/wine/", "RDF/XML")
        model.read(wineInstancesResource.inputStream, "http://uff.ic.br/ontologias/recomendador/wine/", "TURTLE")
        model.read(foodSchemaResource.inputStream, "http://uff.ic.br/ontologias/recomendador/food/", "RDF/XML")
        model.read(foodInstancesResource.inputStream, "http://uff.ic.br/ontologias/recomendador/food/", "TURTLE")
        model.read(recommendationSchemaResource.inputStream, "http://uff.ic.br/ontologias/recomendador/recommendation/", "RDF/XML")
        model.read(recommendationInstancesResource.inputStream, "http://uff.ic.br/ontologias/recomendador/recommendation/", "TURTLE")
    }
}
