package com.example.backend.parser;

import IhmMCD2.IhmRelation2;
import com.example.backend.modules.analysis.model.Actor;
import com.example.backend.modules.analysis.model.McdLink;
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
                "Le header spécifique au MCD est absent");
    }

    @Test
    public void testshouldlistEntitiesSection() {
        assertTrue(output.contains("[ENTITÉS]"),
                "La section listant les entités est absente");
    }

    @Test
    public void testShouldContainSpecificEntityname() {

        assertTrue(output.contains("Connexion"),
                "L'entité 'Connexion' devrait être présente dans la sortie textuelle");
    }

    @Test
    public void testShouldlistRelationsandCardinalitiesSection() {
        assertTrue(output.contains("[RELATIONS ET CARDINALITÉS]"),
                "La section des relations est indispensable pour l'audit de cohérence");
    }

    @Test
    public void testShouldformatlinksWithReadablesentence() {

        assertTrue(output.contains("est liée à la relation"),
                "Les liens devraient être décrits par une phrase compréhensible pour l'IA");
    }

    @Test
    public void testShouldContainSpecificRelationname() {

        assertTrue(output.contains("concerne"),
                "La relation 'concerne' devrait être extraite dans le rapport textuel");
    }
}
