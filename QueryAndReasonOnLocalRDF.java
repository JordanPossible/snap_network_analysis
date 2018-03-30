

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.GenericRuleReasonerFactory;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.PrintUtil;



public class QueryAndReasonOnLocalRDF {

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		
		// Chemin vers l'ontologie de collaboration
		String pathToOntology = "./rdf_output.rdf";

		// Requetes
		// Le nombre total de triplets
		String howManyTriples = "select (count(*) as ?total_number_of_triples) where {?s ?p ?o}";
		
		
		String zero =
				"SELECT ?p "
			+	"WHERE { "
			+		"?s ?p ?o . "
			+	"} ";
		
		//Combien de noeuds dans les graph
		String nombre_noeuds =
				"PREFIX wss: <http://www.une_uri.org/HMIN209Social.rdf#> "
			+	"PREFIX sor: <http://purl.org/net/soron> "
			+	"SELECT (COUNT(DISTINCT ?s) AS ?collaborates) (COUNT(DISTINCT ?t) AS ?well_collaborates) (COUNT(DISTINCT ?x) AS ?bad_collaborates) "
			+	"WHERE { "
			+		"?s sor:collaboratesWith ?o . "
			+		"?t wss:collaboratesWellWith ?v . "
			+		"?x wss:collaboratesBadWith ?z . "
			+	"} ";
		
		//Combien d'aretes dans le graph wss:collaboratesBadWith
		String nombre_arete =
				"PREFIX wss: <http://www.une_uri.org/HMIN209Social.rdf#> "
			+	"PREFIX sor: <http://purl.org/net/soron> "
			+	"SELECT (count(?s) as ?number_of_bad_collaborates) "
			+	"WHERE "
			+	"{ "
			+	"?s wss:collaboratesBadWith ?o . "
			+	"FILTER(str(?s) <str(?o)) . "
			+	"} ";
		
		//S'agit t-il d'un graph connexe (NE PAS OUBLIÉ DE COMMENTER ET DECOMMENTER LES DEUX LIGNES PLUS BAS POUR LE ASK)
		String connexe =
				"PREFIX wss: <http://www.une_uri.org/HMIN209Social.rdf#> "
			+	"PREFIX sor: <http://purl.org/net/soron> "
			+	"ASK "
			+	"WHERE "
			+	"{ "
			+	"?x (sor:collaboratesWith|^sor:collaboratesWith)+ ?y . "
			+	"MINUS { wss:tarder_1  (sor:collaboratesWith|^sor:collaboratesWith) + ?y }"
			+	"} ";
		
		//La taille de la plus grande composante connexe 
		String plus_grande_compo_co =
				"PREFIX wss: <http://www.une_uri.org/HMIN209Social.rdf#> "
			+	"PREFIX sor: <http://purl.org/net/soron> "
			+	"SELECT (count (?y) as ?result) "
			+	"WHERE "
			+	"{ "
			+	"?x (sor:collaboratesWith|^sor:collaboratesWith)+ ?y  "
			+	"} "
			+ 	"GROUP BY ?y "
			+ 	"ORDER BY DESC(?result) LIMIT 1 ";
		
		//Le degrés de chaque noeuds entrent
		String deg_all_nodes =
				"PREFIX wss: <http://www.une_uri.org/HMIN209Social.rdf#> "
			+	"PREFIX sor: <http://purl.org/net/soron> "
			+	"SELECT DISTINCT ?o (COUNT (DISTINCT ?s) as ?bad) (COUNT (DISTINCT ?t) as ?well) (COUNT (DISTINCT ?u) as ?normal) "
			+	"WHERE "
			+	"{ "
			+	"?s wss:collaboratesBadWith ?o .  "
			+	"?t wss:collaboratesWellWith ?o . "
			+	"?u sor:collaboratesWith ?o . "
			+	"} "
			+ 	"GROUP BY ?o "
			+ 	"ORDER BY DESC(?bad) "
			+	"LIMIT 20";
		
		//combien y a t-il de trinagle dans le graphe
		String nb_triangle =
				"PREFIX wss: <http://www.une_uri.org/HMIN209Social.rdf#> "
			+	"PREFIX sor: <http://purl.org/net/soron> "
			+	"SELECT (COUNT(DISTINCT *) AS ?nbTriangles) "
			+	"WHERE "
			+	"{ "
			+	"?x (wss:collaboratesBadWith|^wss:collaboratesBadWith) ?y .  "
			+	"?y (wss:collaboratesBadWith|^wss:collaboratesBadWith) ?z . "
			+	"?z (wss:collaboratesBadWith|^wss:collaboratesBadWith) ?x . "
			+	"FILTER (str(?x)<str(?y) && str(?y)<str(?z)) "
			+	"} ";

		
//		la liste de toutes les cliques de cardinalité 4 et 5 dans le graph
		String cliques =
				"PREFIX wss: <http://www.une_uri.org/HMIN209Social.rdf#> "
			+	"PREFIX sor: <http://purl.org/net/soron> "
			+	"SELECT ?x (count (DISTINCT ?y) as ?bad) "
			+	"WHERE "
			+	"{ "
			+	"?x (wss:collaboratesBadWith|^wss:collaboratesBadWith)* ?y . "
			+	"} "
			+ 	"GROUP BY ?x "
			+ 	"HAVING (?bad = 4 || ?bad= 5) ";
		
//		Calculez le nombre de voisins (différents) de chaque noeud
		String nb_voisins =
				"PREFIX wss: <http://www.une_uri.org/HMIN209Social.rdf#> "
			+	"PREFIX sor: <http://purl.org/net/soron> "
			+	"SELECT ?x (COUNT(DISTINCT *) AS ?bad) "
			+	"WHERE "
			+	"{ "
			+	"?x (wss:collaboratesBadWith|^wss:collaboratesBadWith) ?y . "
			+	"} "
			+ 	"GROUP BY ?x "
			+ 	"ORDER BY DESC(?bad) "
			+ 	"LIMIT 20 ";
				
//		nombre de couples-de-voisins (différentes) de tous les participants.
		String nb_couple_voisins_diff =
				"PREFIX wss: <http://www.une_uri.org/HMIN209Social.rdf#> "
			+	"PREFIX sor: <http://purl.org/net/soron> "
			+	"SELECT ?x (COUNT(*) as ?bad)  "
			+	"WHERE "
			+	"{ "
			+		"SELECT DISTINCT ?x (concat(str(?y), str(?z)) as ?res)"
			+		"WHERE {"
			+		"?x (wss:collaboratesBadWith|^wss:collaboratesBadWith) ?y . "
			+		"?x (wss:collaboratesBadWith|^wss:collaboratesBadWith) ?z . "
			+		"FILTER (str(?y) < str(?z)) "
			+		"} "
			+	"ORDER BY ASC(?x) "
			+	"} "
			+ 	"GROUP BY ?x ";
				
				
		
		
						

		// Définition des règles 
		StringBuilder rules = new StringBuilder();
		
//		rules.append("[rule1:  (?x mcf:PartOf ?y), (?y mcf:PartOf ?z) -> (?x mcf:PartOf ?z)] ");
//		rules.append("[rule2:  (?x rdfs:subClassOf ?y), (?y rdfs:subClassOf ?z) -> (?x rdfs:subClassOf ?z)] ");

		// Création d'un modèle RDF et chargement des triplets à partir d'un fichier
		
		Model model = ModelFactory.createDefaultModel();
		
		// Lecture du fichier 
		InputStream in = FileManager.get().open(pathToOntology);

		long start = System.currentTimeMillis();
		
		// chargement du fichier dans le modèle
		
		model.read(in, null);

		System.out.println("Import time : " + (System.currentTimeMillis() - start));

		// Instantiation d'un raisonneur de type 'GenericRuleReasoner'

		GenericRuleReasoner reasoner = (GenericRuleReasoner) GenericRuleReasonerFactory.theInstance().create(null);

		reasoner.setRules(Rule.parseRules(rules.toString()));

		// Changement du mode du raisonnement
		
		reasoner.setMode(GenericRuleReasoner.HYBRID);

		start = System.currentTimeMillis();

		InfModel inf = ModelFactory.createInfModel(reasoner, model);

		System.out.println("Rules pre-processing time : " + (System.currentTimeMillis() - start));

		// **************************************** //
		// Création d'une instance de la classe Query
		
		Query query = QueryFactory.create(nb_couple_voisins_diff);

		start = System.currentTimeMillis();

		QueryExecution qexec = QueryExecutionFactory.create(query, inf);

		System.out.println("Query pre-processing time : " + (System.currentTimeMillis() - start));
		
//		boolean b = qexec.execAsk();
//		System.out.println("Ask result = " + ((b)?"TRUE":"FALSE"));

		// Éxécution de la requête et affichage des résultats
		start = System.currentTimeMillis();

		try {

			ResultSet rs = qexec.execSelect();

			ResultSetFormatter.out(System.out, rs, query);

		} finally {

			qexec.close();
		}

		System.out.println("Query + Display time : " + (System.currentTimeMillis() - start));

	}
}
