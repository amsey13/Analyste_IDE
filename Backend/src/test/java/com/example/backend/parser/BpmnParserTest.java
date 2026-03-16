package com.example.backend.parser;

import com.example.backend.modules.analysis.model.Actor;
import com.example.backend.modules.analysis.parser.BpmnParserStrategy;
import com.example.backend.modules.analysis.model.Flux;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BpmnParserTest {


    BpmnParserStrategy parser;
    String output;




    @BeforeEach
    public void setUp() throws ParserConfigurationException, IOException, SAXException {
        InputStream in = getClass().getResourceAsStream("/resourcesparser/test-bpmn.bpmn");
        if (in == null) {
            throw new RuntimeException("BPMN resource not found");
        }

        parser = new BpmnParserStrategy(in);
        output = parser.parse();
    }

    @Test
    public void testShoudContainsBpmnHeader() {
        assertTrue(output.contains("--- DÉTAILS DU MODÈLE BPMN"),
                "The output should begin with the BPMN model header");
    }


    @Test
    public void testShouldlistDefinedActorsCorrectly() {

        assertTrue(output.contains("[ACTEURS / POOLS]"), "The “Actors” section is missing");
        assertTrue(output.contains("Utilisateur"), "The ‘User’ actor should be listed");
    }

    @Test
    public void testShouldformatTaskswithCorrectprefix() {

        assertTrue(output.contains("[ACTIVITÉS ET TÂCHES]"), "La section Tâches est manquante");
        assertTrue(output.contains("Tâche :"), "Each task should begin with “Task: ");
    }

    @Test
    public void testShouldContainInteractionSectionforMessageflows() {
        assertTrue(output.contains("[INTERACTIONS / FLUX DE MESSAGES]"),
                "The message flow section is required for the AI");
    }



    @Test
    public void testShouldnotReturnEmptyString() {
        assertNotNull(output);
        assertFalse(output.trim().isEmpty(), "The parser's output must not be empty");
    }








}
