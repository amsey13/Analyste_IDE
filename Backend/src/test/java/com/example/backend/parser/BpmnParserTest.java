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




    @BeforeEach
    public void setUp() throws ParserConfigurationException, IOException, SAXException {
        InputStream in = getClass().getResourceAsStream("/resourcesparser/test-bpmn.bpmn");
        if (in == null) {
            throw new RuntimeException("BPMN resource not found");
        }
        parser = new BpmnParserStrategy(in);
    }

    @Test
    public void testParseBpmnAndFindACtors() throws XPathExpressionException {

        List<Actor> actors = parser.findActors();

        assertNotNull(actors);
        assertFalse(actors.isEmpty());
        assertEquals(2, actors.size());

        // On vérifie le nom via l'accesseur du record
        assertTrue(actors.stream().anyMatch(a -> a.getName().equals("Utilisateur")));

    
    }

    @Test
    public void testParseBpmnAndFindTasks() throws XPathExpressionException {
        List<String> tasks = parser.findTasks(parser.getDocument(), parser.getXpath());
        assertNotNull(tasks);
        assertFalse(tasks.isEmpty());
        assertEquals(2, tasks.size());
    }


    @Test
    public void testParseBpmnAndFindFlux() throws XPathExpressionException {
        // On utilise la méthode de l'interface (findFluxs) plutôt que la méthode interne
        List<Flux> fluxs = parser.findFluxs();

        assertNotNull(fluxs);
        assertFalse(fluxs.isEmpty());
        assertEquals(2, fluxs.size());

        // On vérifie maintenant les champs source/target directement
        boolean found = fluxs.stream().anyMatch(f -> "Utilisateur".equals(f.getSender()));
        assertTrue(found, "Le flux devrait avoir un émetteur 'Utilisateur'");

    }





}
