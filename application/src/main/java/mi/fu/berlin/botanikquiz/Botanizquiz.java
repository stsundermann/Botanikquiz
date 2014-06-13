package mi.fu.berlin.botanikquiz;

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

import java.io.IOException;
import java.util.Random;

public class Botanizquiz extends HttpServlet {

    volatile Random rand;

    @Override
    public void doGet (HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        resp.setContentType("text/html");

        try {
            Document xsltDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getServletContext().getResourceAsStream("/quiz.xsl"));
            Document dummyRDF = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getServletContext().getResourceAsStream("/B200049849.rdf"));

            // TODO: Add baseX here
            Document doc1 = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getServletContext().getResourceAsStream("/B100146595.rdf"));
            Document doc2 = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getServletContext().getResourceAsStream("/B200049849.rdf"));
            Document doc3 = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getServletContext().getResourceAsStream("/B200049849.rdf"));
            Document doc4 = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getServletContext().getResourceAsStream("/B200049849.rdf"));

            TransformerFactory factory = new TransformerFactoryImpl();
            Transformer quizTrans = factory.newTransformer(new DOMSource(xsltDoc));

            Source source = new DOMSource(dummyRDF);
            quizTrans.setParameter("selectedNum", rand.nextInt(3) + 1);
            quizTrans.setParameter("doc1", doc1);
            quizTrans.setParameter("doc2", doc2);
            quizTrans.setParameter("doc3", doc3);
            quizTrans.setParameter("doc4", doc4);

            quizTrans.transform(source, new StreamResult(resp.getWriter()));
        } catch (Exception e) { e.printStackTrace(); ;resp.sendError(500);}
    }

    @Override
    public void init () throws ServletException {
        super.init();
        rand = new Random ();
    }

    @Override
    public void destroy () {}
}
