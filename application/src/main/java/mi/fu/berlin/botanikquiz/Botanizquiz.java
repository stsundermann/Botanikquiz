package mi.fu.berlin.botanikquiz;

import com.hp.hpl.jena.query.*;
import net.sf.saxon.TransformerFactoryImpl;
import org.w3c.dom.Document;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.basex.core.*;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.data.*;
import org.basex.query.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Random;

public class Botanizquiz extends HttpServlet {

    volatile Random rand;

    final ParameterizedSparqlString sparqlImage = new ParameterizedSparqlString ("PREFIX edm: <http://www.europeana.eu/schemas/edm/>" +
            "PREFIX ore: <http://www.openarchives.org/ore/terms/>" +
            "PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
            "SELECT ?image WHERE {" +
            "?resource ore:proxyFor ?item;" +
            "dc:title ?title ." +
            "?resource ore:proxyIn ?proxy." +
            "?proxy edm:object ?image." +
            "}" +
            "LIMIT 100");

    final ParameterizedSparqlString sparqlInformation = new ParameterizedSparqlString("PREFIX rdf: <http://dbpedia.org/ontology/Plant>" +
            "SELECT * WHERE {" +
            "?e rdf:type" +
            "rdfs:label ?x." +
            "FILTER regex(?x, ?title)" +
            "}");

    @Override
    public void doGet (HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        resp.setContentType("text/html");

        try {
            Document xsltDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getServletContext().getResourceAsStream("/quiz.xsl"));
            Document dummyRDF = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getServletContext().getResourceAsStream("/B200049849.rdf"));

            int beg;
            final int count=1;
            int size = 200853;
            String [] resultList = new String[4];
            String docGen = "http://herbarium.bgbm.org/data/rdf/";
            URL[] url = new URL[4];

            Context context = new Context();
            String contextPath = getServletContext().getRealPath(File.separator);

            new CreateDB("Botanik_DB", contextPath + "\\catalog.xml").execute(context);

            for (int i = 0; i < 4; i++) {

                // Take 1 RDF at the position beg == ...query.get(beg)
                beg = rand.nextInt(200853);

                String query =
                        "declare default element namespace \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\";\n"
                                + "declare namespace dwc = \"http://rs.tdwg.org/dwc/terms/\";\n"
                                + "declare namespace dc  = \"http://purl.org/dc/terms/\";\n"
                                + "declare namespace rdf = \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\";\n"
                                + "let $i := for $item in //RDF/Description/dwc:Occurrence/Bag/li/Description\n"
                                + "return $item\n"
                                + "let $beg := " + beg + "\n"
                                + "let $end := " + count + "\n"
                                + "for $item in subsequence($i,$beg,$end)\n"
                                + "return string($item/@rdf:about)";

                QueryProcessor processor = new QueryProcessor(query, context);

                Result result = processor.execute();

                // Now we start building the URL from result.
                String u_result = result.toString();

                // delete unnecessary part from query result. We only need the name of the rdf-object Bxxxx
                String[] u_result_split = u_result.split("http://herbarium.bgbm.org/object/");

                // We build a path to the rdf location by taking the rdf-Name and the path http://herbarium.bgbm.org/data/rdf/
                String u_result_url = u_result_split[1];
                resultList[i] = u_result_url;
                url[i] = new URL(docGen + u_result_url);
                processor.close(); // Make a new request to the database
            }
            new DropDB("Botanik_DB").execute(context);
            context.close();


            // TODO: Add baseX here
            Document[] docs = new Document[4];
            docs[0] = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url[0].openStream());
            docs[1] = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url[1].openStream());
            docs[2] = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url[2].openStream());
            docs[3] = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url[3].openStream());

            TransformerFactory factory = new TransformerFactoryImpl();
            Transformer quizTrans = factory.newTransformer(new DOMSource(xsltDoc));

            Source source = new DOMSource(dummyRDF);
            int selectedDocument = rand.nextInt(4) + 1;


            Document queryDocument = docs[selectedDocument - 1];

            quizTrans.setParameter("imageLink", getImageLink(queryDocument));
            quizTrans.setParameter("selectedNum", selectedDocument);
            quizTrans.setParameter("doc1", docs[0]);
            quizTrans.setParameter("doc2", docs[1]);
            quizTrans.setParameter("doc3", docs[2]);
            quizTrans.setParameter("doc4", docs[3]);

            quizTrans.transform(source, new StreamResult(resp.getWriter()));
        } catch (Exception e) { e.printStackTrace(); ;resp.sendError(500);}
    }

    private String getImageLink (Document doc) throws Exception {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath titlePath = xPathFactory.newXPath();
        XPathExpression titlePathExpression = titlePath.compile("//*[local-name()='title']");
        String title = (String) titlePathExpression.evaluate(doc, XPathConstants.STRING);

        sparqlImage.setLiteral("title", title);
        Query queryImage = QueryFactory.create(sparqlImage.toString());

        QueryExecution qe = QueryExecutionFactory.sparqlService("http://europeana.ontotext.com/sparql", queryImage);
        ResultSet results = qe.execSelect();
        String link = results.next().get("image").asResource().getURI();
        qe.close();

        return link;
    }

    @Override
    public void init () throws ServletException {
        super.init();
        rand = new Random ();

    }

    @Override
    public void destroy () {}
}
