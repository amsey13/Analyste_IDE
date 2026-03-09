package com.example.backend.parser;

import com.example.backend.modules.analysis.parser.BpmnParser;
import com.example.backend.modules.analysis.parser.Flux;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BpmnParserTest {


    XPath xpath = XPathFactory.newInstance().newXPath();
    Document doc;




    @BeforeEach
    public void setUp() throws ParserConfigurationException, IOException, SAXException {
        InputStream is = getClass().getResourceAsStream("/resourcesparser/test-bpmn.bpmn");
        if (is == null) {
            throw new RuntimeException("BPMN resource not found");
        }
        doc = BpmnParser.loadDocument(is);
    }

    @Test
    public void testParseBpmnAndFindACtors() throws XPathExpressionException {

        List<String> actors = BpmnParser.findActors(doc,xpath);
        assertTrue(!actors.isEmpty());
        assertEquals(2,actors.size());
        assertTrue(actors.contains("Utilisateur"));

    
    }

    @Test
    public void testParseBpmnAndFindTasks() throws XPathExpressionException {
        List<String> tasks = BpmnParser.findTasks(doc,xpath);
        assertTrue(!tasks.isEmpty());
        assertEquals(2,tasks.size());
    }


    @Test
    public void testParseBpmnAndFindEvents() throws XPathExpressionException {
        List<Flux> fluxs = BpmnParser.findFlux(doc,xpath);
        assertTrue(!fluxs.isEmpty());
        assertEquals(2,fluxs.size());
    }





}
