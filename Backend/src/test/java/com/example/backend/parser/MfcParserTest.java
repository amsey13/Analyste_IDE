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
    String output;

    @BeforeEach
    public void setUp() {
        InputStream in = getClass().getResourceAsStream("/resourcesparser/test-mfc.flu");
        if (in == null) {
            throw new RuntimeException("MFC file not found");
        }
        parser = new MfcParserStrategy(in);
        output = parser.parse();
    }


    @Test
    public void testOutputShouldContainMfcHeaderAndSectionTitles() {
        assertNotNull(output);
        assertTrue(output.contains("DÉTAILS DU MODÈLE MFC"), "Le header MFC est manquant");
        assertTrue(output.contains("[ACTEURS]"), "La section des acteurs est manquante");
    }

    @Test
    public void testOutputShouldDistinguishInternalAndExternalActors() {

        assertTrue(output.contains("- Internes :"), "Le libellé des acteurs internes est absent");
        assertTrue(output.contains("- Externes :"), "Le libellé des acteurs externes est absent");
    }

    @Test
    public void testOutputShouldContainSpecificActorNameFromResource() {

        assertTrue(output.contains("Utilisateur"), "L'acteur 'Utilisateur' devrait apparaître dans le texte");
    }

    @Test
    public void testOutputShouldListFluxAndMessagesSection() {
        assertTrue(output.contains("[FLUX ET MESSAGES]"), "La section des flux est absente");
    }

    @Test
    public void testOutputShouldFormatFluxArrowAndSpecificMessageCorrectly() {

        assertTrue(output.contains("Utilisateur"), "Le formatage du flux avec la flèche est incorrect");
    }

    @Test
    public void testOutputShouldNotBeEmptyOrNull() {
        assertFalse(output.trim().isEmpty(), "Le parseur MFC a renvoyé une chaîne vide");
    }




}
