package com.example.backend.parser;

import IhmMCD2.IhmRelation2;
import com.example.backend.modules.analysis.model.Actor;
import com.example.backend.modules.analysis.model.McdLink;
import com.example.backend.modules.analysis.parser.McdParserStrategy;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class McdParserTest {

    int nbEntity = 4; //Number of entities in the test file used

    private McdParserStrategy createParser() {
        InputStream is = getClass().getResourceAsStream("/resourcesparser/test.mcd");
        assertNotNull(is);
        return new McdParserStrategy(is);
    }


    @Test
    public void testLoadObjects() throws Exception {

        McdParserStrategy parser = createParser();
        List<Object> objects = parser.loadObjects();

        assertNotNull(objects);
        assertFalse(objects.isEmpty());
    }

    @Test
    public void testGetAllEntityOk() throws FileNotFoundException {
        McdParserStrategy parser = createParser();
        List<Actor> actors = parser.findActors();

        assertNotNull(actors);
        assertEquals(4, actors.size());
        assertTrue(actors.stream().anyMatch(a -> "Connexion".equals(a.getName())),
                "The actor 'Connexion' should be present");


    }

    @Test
    public void testFindRelations() {

        McdParserStrategy parser = createParser();
        List<Object> objects = parser.loadObjects();
        List<IhmRelation2> relations = parser.findRelations(objects);
        List<String> names = relations.stream()
                .map(r -> r.getRelation().getNom())
                .toList();

        assertNotNull(relations);
        assertFalse(relations.isEmpty());
        assertTrue(names.contains("concerne"));

    }


    @Test
    public void testFindAttributes() {

        McdParserStrategy parser = createParser();

        List<Object> objects = parser.loadObjects();
        List<Object> attributes = parser.findAttributes(objects);

        assertNotNull(attributes);
        assertTrue(attributes.size()>nbEntity); //Entity without attributes aren't allowed so it should have at least 4 attributes
    }

    @Test
    public void testFindLinks() {
        McdParserStrategy parser = createParser();
        // Test de la logique métier spécifique au MCD
        List<McdLink> links = parser.findLinks(parser.loadObjects());

        assertNotNull(links);
        assertFalse(links.isEmpty());
        assertNotNull(links.get(0).getEntity());

    }





}
