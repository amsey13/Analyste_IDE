package com.example.backend.parser;

import com.example.backend.modules.analysis.parser.McdParserStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class McdParserTest {

    int nbEntity = 4;//Number of entities in the test file used
    String output;

    @BeforeEach
    public void setUp() throws FileNotFoundException {
        InputStream is = getClass().getResourceAsStream("/resourcesparser/test.mcd");
        assertNotNull(is);
        McdParserStrategy parser = new McdParserStrategy(is);
        output = parser.parse();
    }


    @Test
    public void testShouldContainMcdHeader() {
        assertTrue(output.contains("--- DÉTAILS DU MODÈLE MCD"),
                "The MCD-specific header is missing");
    }

    @Test
    public void testshouldlistEntitiesSection() {
        assertTrue(output.contains("[ENTITÉS]"),
                "The section listing the entities is missing");
    }

    @Test
    public void testShouldContainSpecificEntityname() {

        assertTrue(output.contains("Connexion"),
                "The ‘Connection’ entity should appear in the text output");
    }

    @Test
    public void testShouldlistRelationsandCardinalitiesSection() {
        assertTrue(output.contains("[RELATIONS ET CARDINALITÉS]"),
                "The ‘Connection’ entity should appear in the text output. The relationships section is essential for consistency auditing.");
    }

    @Test
    public void testShouldformatlinksWithReadablesentence() {

        assertTrue(output.contains("est liée à la relation"),
                "Links should be described using a sentence that is understandable to the IA");
    }

    @Test
    public void testShouldContainSpecificRelationname() {

        assertTrue(output.contains("concerne"),
                "The phrase “concerns” should be included in the text report");
    }
}
