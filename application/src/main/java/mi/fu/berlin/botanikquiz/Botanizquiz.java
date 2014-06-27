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
import javax.xml.parsers.ParserConfigurationException;
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
import org.basex.core.cmd.Open;
import org.basex.data.*;
import org.basex.query.*;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Random;

public class Botanizquiz extends HttpServlet {

    volatile Random rand;
    volatile Context context;

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
            String imageLink = "";
            Transformer quizTrans = null;
            TransformerFactory factory = new TransformerFactoryImpl();
            Source source = new DOMSource(dummyRDF);


            int beg;
            final int count=1;
            int size = 200853;
            String [] resultList = new String[4];
            String docGen = "http://herbarium.bgbm.org/data/rdf/";
            URL[] url = new URL[4];
            Document[] docs = new Document[4];
            int selectedDocument = 0;

            while (imageLink.equals("")) {

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

                // TODO: Add baseX here

                Thread[] threads = new Thread[4];
                DownloadHelper[] downloadHelpers = new DownloadHelper[4];

                for (int i = 0; i < threads.length; i++) {
                    downloadHelpers[i] = new DownloadHelper(url[i]);
                    threads[i] = new Thread(downloadHelpers[i]);
                    threads[i].start();
                }

                for (Thread t : threads)
                    t.join();

                docs[0] = downloadHelpers[0].result;
                docs[1] = downloadHelpers[1].result;
                docs[2] = downloadHelpers[2].result;
                docs[3] = downloadHelpers[3].result;


                quizTrans = factory.newTransformer(new DOMSource(xsltDoc));

                selectedDocument = rand.nextInt(4) + 1;


                Document queryDocument = docs[selectedDocument - 1];
                imageLink = getImageLink(queryDocument);
            }

            quizTrans.setParameter("imageLink", imageLink);
            quizTrans.setParameter("selectedNum", selectedDocument);
            quizTrans.setParameter("doc1", docs[0]);
            quizTrans.setParameter("doc2", docs[1]);
            quizTrans.setParameter("doc3", docs[2]);
            quizTrans.setParameter("doc4", docs[3]);

            quizTrans.transform(source, new StreamResult(resp.getWriter()));
        } catch (Exception e) { e.printStackTrace(); ;resp.sendError(500);}
    }

    private class DownloadHelper implements Runnable {

        private URL url;
        public Document result;

        public DownloadHelper(URL url){
            this.url = url;
        }

        @Override
        public void run() {
            try {
                result = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openStream());
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
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

        String link = "";
        while (results.hasNext() && link.equals(""))
             link = results.next().get("image").asResource().getURI();
        qe.close();

        return URLDecoder.decode(link, "UTF-8");
    }

    @Override
    public void init () throws ServletException {
        super.init();
        rand = new Random ();
        try {
            context = new Context();
            String filename = getServletContext().getRealPath(File.separator) + "\\catalog.xml";
            try {
                new Open("Botanik_DB").execute(context);
            } catch (IOException e) {
                new CreateDB("Botanik_DB", filename).execute(context);
            }
        } catch (BaseXException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void destroy () {
        context.closeDB();
        context.close();
    }
}
