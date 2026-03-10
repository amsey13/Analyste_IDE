package com.example.backend.modules.analysis.parser;

import com.example.backend.modules.analysis.model.Actor;
import com.example.backend.modules.analysis.model.Flux;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class BpmnParserStrategy implements ModelParserStrategy {

    private final Document document;
    private final XPath xpath;


    public BpmnParserStrategy(InputStream input) throws ParserConfigurationException, IOException, SAXException {
        this.document = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
                .parse(input);
        this.xpath = XPathFactory.newInstance().newXPath();

    }

    // Strategy methods implementation


    @Override
    public List<Actor> findActors() {
       try{
           return findBpmnActors(document,xpath);
       } catch (XPathExpressionException e) {
           throw new RuntimeException(e);
       }
    }

    @Override
    public List<Flux> findFluxs() {
        try{

            return findBpmnFlux(document,xpath);
        }catch (XPathExpressionException e){
            throw new RuntimeException(e);
        }
    }

    public Document getDocument() {
        return document;
    }

    public XPath getXpath() {
        return xpath;
    }

    /**
     * The `findActors` function extracts the names of actors from a given XML document using XPath.
     *
     * @param doc   The `doc` parameter in the `findActors` method is of type `Document`, which is likely
     *              an XML document that contains information about participants or actors. This document is used to
     *              extract data using XPath expressions.
     * @param xpath XPath is a language for selecting nodes from an XML document. It is commonly used
     *              to navigate through elements and attributes in an XML document to locate specific information.
     * @return This method returns a List of Strings containing the names of actors found in the given
     * Document object using the provided XPath expression.
     */
    private static List<Actor> findBpmnActors(Document doc, XPath xpath) throws XPathExpressionException {

        List<Actor> actors = new ArrayList<>();
        NodeList participants = (NodeList) xpath.evaluate("//*[local-name()='participant']", doc, XPathConstants.NODESET);

        for (int i = 0; i < participants.getLength(); i++) {
            String name = participants.item(i)
                    .getAttributes()
                    .getNamedItem("name")
                    .getNodeValue();

            actors.add(new Actor(name));

        }
        return actors;
    }


    /**
     * The function `findTasks` extracts task names from a given XML document using XPath expressions.
     *
     * @param doc   The `doc` parameter in the `findTasks` method is of type `Document`, which is likely
     *              an XML document that contains information about tasks or activities. This method uses an XPath
     *              expression to search for specific elements within the document that represent tasks or call
     *              activities. The method then extracts the name attribute
     * @param xpath XPath is a language used for selecting nodes from an XML document. In the provided
     *              method, the XPath object is used to evaluate an XPath expression on the given Document object
     *              (doc) to find specific tasks.
     * @return The method `findTasks` returns a List of Strings containing the names of tasks found in
     * the given Document `doc` using the provided XPath expression.
     */
    public static List<String> findTasks(Document doc, XPath xpath) throws XPathExpressionException {
        List<String> tasks = new ArrayList<>();
        NodeList taskNode = (NodeList) xpath.evaluate("//*[contains(local-name(),'Task') or local-name()='callActivity']", doc, XPathConstants.NODESET);

        for (int i = 0; i < taskNode.getLength(); i++) {

            Element task = (Element) taskNode.item(i);
            String name = task.getAttribute("name");

            if (name == null || name.isEmpty()) {
                name = task.getAttribute("id");
            }

            tasks.add(name);
        }
        return tasks;
    }


    /**
     * The function `findSubProcess` takes a `Document` and an `XPath` object, finds all sub-process
     * elements in the document using the XPath expression, extracts their names, and returns a list of
     * sub-process names that are empty or null.
     *
     * @param doc   The `doc` parameter is of type `Document`, which is likely an XML document that
     *              contains the information you want to extract sub-processes from.
     * @param xpath XPath is a query language used for selecting nodes from an XML document. In the
     *              provided code snippet, the `xpath` parameter is an instance of the `XPath` class, which is used
     *              to evaluate XPath expressions on the `doc` parameter, which is a `Document` object representing
     *              an XML document
     * @return The method `findSubProcess` returns a List of Strings containing the names of
     * subProcesses found in the given Document `doc` using the provided XPath expression.
     */
    public static List<String> findSubProcess(Document doc, XPath xpath) throws XPathExpressionException {

        List<String> subProcess = new ArrayList<>();
        NodeList processes = (NodeList) xpath.evaluate("//*[local-name()='subProcess']", doc, XPathConstants.NODESET);

        for (int i = 0; i < processes.getLength(); i++) {
            Element process = (Element) processes.item(i);
            String name = process.getAttribute("name");
            if (name == null || name.isEmpty()) {
                subProcess.add(name);
            }
        }
        return subProcess;
    }


    /**
     * The function `findFlux` parses a given XML document using XPath to extract message flow elements
     * and their attributes, creating a list of `Flux` objects with relevant information.
     * 
     * @param doc The `doc` parameter in the `findFlux` method is of type `Document` and represents an
     * XML document that contains information about message flows. This document is used to extract
     * data related to message flows.
     * @param xpath The `xpath` parameter in the `findFlux` method is an object of the `XPath` class.
     * It is used to compile and evaluate XPath expressions against the given XML document (`doc`). In
     * this method, the XPath expression is used to select specific elements from the XML document
     * based on the
     * @return The method `findFlux` returns a list of `Flux` objects. Each `Flux` object represents a
     * message flow with attributes such as name, sender, and recipient.
     */
    private static List<Flux> findBpmnFlux(Document doc, XPath xpath) throws XPathExpressionException {
        List<Flux> fluxList = new ArrayList<>();

        NodeList fluxs = (NodeList) xpath.evaluate("//*[local-name()='messageFlow']", doc, XPathConstants.NODESET);

        for (int i = 0; i < fluxs.getLength(); i++) {
            Element messageFlow = (Element) fluxs.item(i);
            String fluxName = messageFlow.getAttribute("name");
            String idSource = messageFlow.getAttribute("sourceRef");
            String idTarget = messageFlow.getAttribute("targetRef");

            String sender = findFluxActors(doc, xpath, idSource);
            String recipient = findFluxActors(doc, xpath, idTarget);

            fluxList.add(new Flux(fluxName, recipient, sender));
        }
        return fluxList;
    }


    /**
     * The function `findFluxActors` searches for a participant name based on a given element ID within
     * a specified XML document using XPath.
     * 
     * @param doc The `doc` parameter in the `findFluxActors` method is of type `Document` and
     * represents an XML document that contains the data you are working with. This document is used to
     * perform XPath queries to extract specific information from the XML structure.
     * @param xpath XPath is a language used for selecting nodes from an XML document. In the provided
     * Java method, the `xpath` parameter is an instance of the `XPath` class, which is used to
     * evaluate XPath expressions on the given `doc` (XML document).
     * @param elementId The `elementId` parameter in the `findFluxActors` method is used to identify a
     * specific element in the XML document. This method searches for a process element that is a
     * parent of the element with the given `elementId`. It then retrieves the participant associated
     * with that process and returns
     * @return If the specified elementId is found in the document and it has a parent element with the
     * local name 'process', then the name attribute of the participant element with the processRef
     * attribute matching the id of the process element is returned. If no such participant element is
     * found, then "Inconnu" (French for "Unknown") is returned.
     */
    private static String findFluxActors(Document doc, XPath xpath, String elementId) throws XPathExpressionException {

        String processes = "//*[@id='" + elementId + "']/parent::*[local-name()='process']";
        Element process = (Element) xpath.evaluate(processes, doc, XPathConstants.NODE);

        if (process != null) {

            String processId = process.getAttribute("id");
            String participants = "//*[local-name()='participant'][@processRef='" + processId + "']";
            Element participant = (Element) xpath.evaluate(participants, doc, XPathConstants.NODE);
            if (participant != null) {
                return participant.getAttribute("name");
            }
        }
        return "Inconnu";
    }


}


    





    





