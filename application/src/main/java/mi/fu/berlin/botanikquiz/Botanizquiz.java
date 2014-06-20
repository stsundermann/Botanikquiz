package mi.fu.berlin.botanikquiz;

import com.hp.hpl.jena.query.*;
import net.sf.saxon.TransformerFactoryImpl;
import org.w3c.dom.Document;

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

import java.io.IOException;
import java.util.Random;

public class Botanizquiz extends HttpServlet {

    volatile Random rand;

    final ParameterizedSparqlString sparqlImage = new ParameterizedSparqlString ("PREFIX edm: <http://www.europeana.eu/schemas/edm/>" +
            "PREFIX ore: <http://www.openarchives.org/ore/terms/>" +
            "PREFIX dc: <http://purl.org/dc/elements/1.1/>" +
            "SELECT ?image WHERE {" +
            "?resource ore:proxyFor ?item;" +
            "dc:title ?title ." +
            "?resource ore:proxyFor ?item;" +
            "dc:source \"Herbarium Berolinense\" ." +
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

            // TODO: Add baseX here
            Document[] docs = new Document[4];
            docs[0] = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getServletContext().getResourceAsStream("/B100146595.rdf"));
            docs[1] = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getServletContext().getResourceAsStream("/BGT0000287.rdf"));
            docs[2] = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getServletContext().getResourceAsStream("/B200049849.rdf"));
            docs[3] = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getServletContext().getResourceAsStream("/BW17980020.rdf"));

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
