package com.example.backend.parser;

import com.example.backend.modules.analysis.model.Actor;
import com.example.backend.modules.analysis.model.Flux;
import com.example.backend.modules.analysis.parser.MfcParserStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MfcParserTest {

    private MfcParserStrategy parser;

    @BeforeEach
    public void setUp() {
        InputStream in = getClass().getResourceAsStream("/resourcesparser/test-mfc.flu");
        if (in == null) {
            throw new RuntimeException("MFC file not found");
        }
        parser = new MfcParserStrategy(in);
    }

    @Test
    public void testLoadLines() {
        List<Object> lines = parser.loadObjects();
        assertNotNull(lines);
        assertFalse(lines.isEmpty(), "MFC object cannot be empty");
    }

    @Test
    public void testFindActors() {
        List<Actor> actors = parser.findActors();

        assertNotNull(actors);
        assertFalse(actors.isEmpty(), "There's at least one actor");


        assertTrue(actors.stream().anyMatch(a -> a.getName().equals("Utilisateur")),
                "the first actor should be the actor name");
    }

    @Test
    public void testFindFluxs(){
        List<Flux> fluxs = parser.findFluxs();
        assertNotNull(fluxs);
        assertFalse(fluxs.isEmpty(), "There's at least one flux");


        assertTrue(fluxs.stream().anyMatch(f -> "Utilisateur".equals(f.getSender())),
                "The flux with the sender should be the utilisateur");

    }








}
