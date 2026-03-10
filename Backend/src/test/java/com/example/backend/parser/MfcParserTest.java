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
            throw new RuntimeException("Fichier MFC introuvable");
        }
        parser = new MfcParserStrategy(in);
    }

    @Test
    public void testLoadLines() {
        List<Object> lines = parser.loadObjects();
        assertNotNull(lines);
        assertFalse(lines.isEmpty(), "Le fichier MFC ne doit pas être vide");
    }

    @Test
    public void testFindActors() {
        List<Actor> actors = parser.findActors();

        assertNotNull(actors);
        assertFalse(actors.isEmpty(), "Il doit y avoir au moins un acteur");

        // Vérification avec l'accesseur du record 'name()'
        assertTrue(actors.stream().anyMatch(a -> a.getName().equals("Utilisateur")),
                "Le fichier doit contenir l’acteur 'Utilisateur'");
    }

    @Test
    public void testFindFluxs(){
        List<Flux> fluxs = parser.findFluxs();
        assertNotNull(fluxs);
        assertFalse(fluxs.isEmpty(), "Il doit y avoir au moins un flux");

        // Vérification avec les accesseurs 'source()' et 'target()' de ton record Flux
        assertTrue(fluxs.stream().anyMatch(f -> "Utilisateur".equals(f.getSender())),
                "Le flux 'Utilisateur -> Système' doit être présent");

    }








}
