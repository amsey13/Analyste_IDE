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

    /**
     * The function `getName` returns the name of an internal or external actor if the input object is
     * an instance of either, otherwise it returns "Inconnu" (Unknown).
     * 
     * @param comp The parameter `comp` is an object that is being passed to the `getName` method. The
     * method checks if the object is an instance of either `flux.ActeurInterne` or
     * `flux.ActeurExterne`, and if so, it returns the name of the actor (either internal
     * @return The method `getName` returns the name of the actor (either internal or external) if the
     * object `comp` is an instance of either `flux.ActeurInterne` or `flux.ActeurExterne`. If `comp`
     * is not an instance of either of these classes, the method returns "Inconnu" which means
     * "Unknown" in French.
     */
    private String getName(Object comp) {
        if (comp instanceof flux.ActeurInterne a) return a.getNom();
        if (comp instanceof flux.ActeurExterne a) return a.getNom();
        return "Inconnu";
    }


   /**
    * This Java function reads objects from an ObjectInputStream and returns a list of all the objects
    * read.
    * 
    * @return The method `loadObjects()` returns a List of Objects containing all the objects read from
    * the ObjectInputStream.
    */
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

    /**
     * The function `findActors` iterates through a list of objects, extracts actors based on their
     * type, and returns a list of Actor objects.
     * 
     * @return The method `findActors()` returns a list of `Actor` objects.
     */
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

    /**
     * The function `findFluxs` iterates through a list of objects, extracts specific information from
     * objects that meet certain criteria, and creates a list of `Flux` objects based on that
     * information.
     * 
     * @return The method `findFluxs()` returns a List of Flux objects.
     */
    public List<Flux> findFluxs() {
        List<Object> allObjects = loadObjects();
        List<Flux> fluxList = new ArrayList<>();
        for (Object o : allObjects) {
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

    /**
     * The function processes an object to extract internal and external actors' names and adds them to
     * separate lists.
     * 
     * @param o The parameter `o` in the `processObjectForActors` method represents an object that is
     * being processed to extract actors. Depending on the type of the object, it is checked if it is
     * an internal actor (`ActeurInterne`) or an external actor (`ActeurExterne`).
     * @param internes The `internes` parameter is a list of internal actors' names. When the
     * `processObjectForActors` method encounters an object that is an instance of `ActeurInterne`, it
     * adds the actor's name to this list.
     * @param externes The `externes` parameter is a list that will store the names of external actors.
     * When the `processObjectForActors` method is called with an object that is an instance of
     * `ActeurExterne`, the name of that external actor will be added to the `externes`
     */
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

    /**
     * The function `extractFluxs` takes a list of objects, extracts Flux objects from it, and returns
     * a list of Flux objects.
     * 
     * @param allObjects A list of objects that may contain instances of `flux.Lien` or lists of
     * objects that may contain instances of `flux.Lien`.
     * @return The method `extractFluxs` returns a List of Flux objects.
     */
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


    /**
     * The function `mapToFlux` takes a `flux.Lien` object, retrieves the names of actors using the
     * `getName` method, and creates a new `Flux` object with the retrieved names and message.
     * 
     * @param lien The `lien` parameter seems to be an object of type `flux.Lien`. It contains
     * information about a link or connection between entities. The method `mapToFlux` is converting
     * this `lien` object into a `Flux` object, which likely represents some kind of data flow or
     * @return A Flux object is being returned, which is created using the information extracted from
     * the provided flux.Lien object. The Flux object is initialized with the fluxMessage, sourceName,
     * and targetName obtained from the lien object.
     */
    private Flux mapToFlux(flux.Lien lien) {
        // On utilise ta méthode getName pour récupérer proprement les noms des acteurs
        String sourceName = getName(lien.getEntite());
        String targetName = getName(lien.getEntitefils());
        String fluxMessage = lien.getNom();

        return new Flux(fluxMessage, sourceName, targetName);
    }



    /**
     * The `parse` function in Java loads objects, processes them to distinguish actors, and extracts
     * and lists fluxes and messages in a StringBuilder.
     * 
     * @return The `parse()` method is returning a formatted string containing details about the MFC
     * (Flux Conceptuels) model. The string includes information about actors (internes and externes),
     * as well as a list of fluxes and messages exchanged within the model. If no fluxes are detected,
     * a message indicating that is included in the output.
     */
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















