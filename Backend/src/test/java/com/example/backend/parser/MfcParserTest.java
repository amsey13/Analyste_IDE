package com.example.backend.parser;


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
        assertTrue(output.contains("DÉTAILS DU MODÈLE MFC"), "The MFC header is missing");
        assertTrue(output.contains("[ACTEURS]"), "The “Actors” section is missing");
    }

    @Test
    public void testOutputShouldDistinguishInternalAndExternalActors() {

        assertTrue(output.contains("- Internes :"), "The description of internal actors is missing");
        assertTrue(output.contains("- Externes :"), "The names of the external parties are missing");
    }

    @Test
    public void testOutputShouldContainSpecificActorNameFromResource() {

        assertTrue(output.contains("Utilisateur"), "The ‘User’ field should appear in the text");
    }

    @Test
    public void testOutputShouldListFluxAndMessagesSection() {
        assertTrue(output.contains("[FLUX ET MESSAGES]"), "The “Flows” section is missing");
    }

    @Test
    public void testOutputShouldFormatFluxArrowAndSpecificMessageCorrectly() {

        assertTrue(output.contains("Utilisateur"), "The formatting of the feed using the arrow is incorrect");
    }

    @Test
    public void testOutputShouldNotBeEmptyOrNull() {
        assertFalse(output.trim().isEmpty(), "The MFC parser returned an empty string");
    }




}
