package com.example.backend.modules.analysis.parser;

import com.example.backend.modules.analysis.model.Actor;
import com.example.backend.modules.analysis.model.Flux;
import flux.ActeurExterne;
import flux.ActeurInterne;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class MfcParserStrategy implements ModelParserStrategy {

    private static final Pattern FLUX_PATTERN =
            Pattern.compile("^FLUX\\s+(.+?)\\s*->\\s*(.+?)\\s*:\\s*(.*)$");

    private final InputStream in;
    private List<String> lines;

    public MfcParserStrategy(InputStream in) {
        this.in = in;
    }

    private String getName(Object comp) {
        if (comp instanceof flux.ActeurInterne a) return a.getNom();
        if (comp instanceof flux.ActeurExterne a) return a.getNom();
        return "Inconnu";
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
            throw new RuntimeException("Mfc cannot be read", e);
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

                        fluxList.add(new Flux(fluxMessage, sourceName, targetName));
                    }
                }
            }
        }
        return fluxList;
    }

    private void processObjectForActors(Object o, List<String> internes, List<String> externes) {
        if (o instanceof ActeurInterne actor) {
            internes.add(actor.getNom());
        } else if (o instanceof ActeurExterne actor) {
            externes.add(actor.getNom());
        } else if (o instanceof List<?> list) {
            for (Object item : list) {
                processObjectForActors(item, internes, externes);
            }
        }
    }

    private List<Flux> extractFluxs(List<Object> allObjects) {
        List<Flux> fluxList = new ArrayList<>();

        for (Object o : allObjects) {

            if (o instanceof flux.Lien lien) {
                fluxList.add(mapToFlux(lien));
            } else if (o instanceof List<?> list) {
                for (Object item : list) {
                    if (item instanceof flux.Lien lien) {
                        fluxList.add(mapToFlux(lien));
                    }
                }
            }
        }
        return fluxList;
    }


    private Flux mapToFlux(flux.Lien lien) {
        // On utilise ta méthode getName pour récupérer proprement les noms des acteurs
        String sourceName = getName(lien.getEntite());
        String targetName = getName(lien.getEntitefils());
        String fluxMessage = lien.getNom();

        return new Flux(fluxMessage, sourceName, targetName);
    }



    @Override
    public String parse() {
        List<Object> allObjects = loadObjects();
        StringBuilder sb = new StringBuilder();

        sb.append("--- DÉTAILS DU MODÈLE MFC (Flux Conceptuels) ---\n");

        // 1. Distinction des acteurs
        sb.append("[ACTEURS]\n");
        List<String> internes = new ArrayList<>();
        List<String> externes = new ArrayList<>();

        // On réutilise la logique de tri
        for (Object o : allObjects) {
            processObjectForActors(o, internes, externes);
        }

        sb.append("- Internes : ").append(String.join(", ", internes)).append("\n");
        sb.append("- Externes : ").append(String.join(", ", externes)).append("\n\n");

        // 2. Liste des flux (échanges)
        sb.append("[FLUX ET MESSAGES]\n");
        List<Flux> fluxList = extractFluxs(allObjects);
        if (fluxList.isEmpty()) {
            sb.append("Aucun flux détecté.\n");
        } else {
            for (Flux f : fluxList) {
                sb.append(String.format("- De [%s] vers [%s] : \"%s\"\n",
                        f.getSender(), f.getRecipient(), f.getName()));
            }
        }

        return sb.toString();
    }





}















