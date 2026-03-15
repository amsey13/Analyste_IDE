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
                "La sortie devrait commencer par le header du modèle BPMN");
    }


    @Test
    public void testShouldlistDefinedActorsCorrectly() {

        assertTrue(output.contains("[ACTEURS / POOLS]"), "La section Acteurs est manquante");
        assertTrue(output.contains("Utilisateur"), "L'acteur 'Utilisateur' devrait être listé");
    }

    @Test
    public void testShouldformatTaskswithCorrectprefix() {

        assertTrue(output.contains("[ACTIVITÉS ET TÂCHES]"), "La section Tâches est manquante");
        assertTrue(output.contains("Tâche :"), "Chaque tâche devrait être préfixée par 'Tâche :'");
    }

    @Test
    public void testShouldContainInteractionSectionforMessageflows() {
        assertTrue(output.contains("[INTERACTIONS / FLUX DE MESSAGES]"),
                "La section des flux de messages est obligatoire pour l'IA");
    }



    @Test
    public void testShouldnotReturnEmptyString() {
        assertNotNull(output);
        assertFalse(output.trim().isEmpty(), "La sortie du parseur ne doit pas être vide");
    }








}
