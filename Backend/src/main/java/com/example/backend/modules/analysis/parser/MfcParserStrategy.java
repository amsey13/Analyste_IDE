package com.example.backend.modules.analysis.parser;

import com.example.backend.modules.analysis.model.Actor;
import com.example.backend.modules.analysis.model.Flux;
import flux.ActeurExterne;
import flux.ActeurInterne;
import flux.Composant;
import flux.Lien;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class MfcParser implements ModelParserStrategy {

    private static final Pattern FLUX_PATTERN =
            Pattern.compile("^FLUX\\s+(.+?)\\s*->\\s*(.+?)\\s*:\\s*(.*)$");

    private final InputStream in;
    private List<String> lines;

    public MfcParser(InputStream in) {
        this.in = in;
    }


    public List<Object> loadObjects() {
        List<Object> allObjects = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(in)) {
            while (true) {
                try {
                    Object obj = ois.readObject();
                    allObjects.add(obj);
                } catch (EOFException e) {
                    break; // Fin du fichier atteinte
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Erreur lors du parsing du fichier MFC binaire", e);
        }
        return allObjects;
    }

    public List<Actor> findActors() {
        List<Object> allObjects = loadObjects();
        List<Actor> actors = new ArrayList<>();

        for (Object o : allObjects) {
            if (o instanceof ActeurInterne actor) {
                actors.add(new Actor(actor.getNom()));
            } else if (o instanceof ActeurExterne actor) {
                actors.add(new Actor(actor.getNom()));
            } else if (o instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof ActeurInterne actor) {
                        actors.add(new Actor(actor.getNom()));
                    } else if (item instanceof ActeurExterne actor) {
                        actors.add(new Actor(actor.getNom()));
                    }
                }

            }
        }
        return actors;
    }

    public List<Flux> findFluxs() {
        List<Object> allObjects = loadObjects();
        List<Flux> fluxList = new ArrayList<>();
        for (Object o : allObjects) {
            // Si c'est une liste, on explore ses enfants
            if (o instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof flux.Lien lien) {

                        String sourceName = getName(lien.getEntite());
                        String targetName = getName(lien.getEntitefils());
                        String fluxMessage = lien.getNom();

                        // Ajout au flux
                        fluxList.add(new Flux(fluxMessage, sourceName, targetName));
                    }
                }
            }
        }
        return fluxList;
    }




    private String getName(Object comp) {
        if (comp instanceof flux.ActeurInterne a) return a.getNom();
        if (comp instanceof flux.ActeurExterne a) return a.getNom();
        return "Inconnu"; // Ou une autre valeur par défaut
    }


}















