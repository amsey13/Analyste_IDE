package com.example.backend.modules.analysis.parser;

import IhmMCD2.IhmEntite2;
import IhmMCD2.IhmLien2;
import IhmMCD2.IhmRelation2;
import com.example.backend.modules.analysis.model.Actor;
import com.example.backend.modules.analysis.model.Flux;
import com.example.backend.modules.analysis.model.McdLink;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class McdParserStrategy implements ModelParserStrategy {

    private final InputStream in;

    public McdParserStrategy(InputStream in) {
        this.in = in;
    }



    /**
     * This Java function reads objects from an ObjectInputStream, handling different types of objects
     * and returning them in a List.
     * 
     * @return The method `loadObjects()` returns a List of Objects containing the objects read from
     * the ObjectInputStream.
     */
    public List<Object> loadObjects() {

        List<Object> allObjects = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(in);) {

            while (true) {
                try {
                    Object obj = ois.readObject();

                    if (obj instanceof ArrayList<?>) {
                        allObjects.addAll((ArrayList<?>) obj);
                    } else {
                        allObjects.add(obj);
                    }

                } catch (EOFException e) {
                    break;
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Erreur lors du parsing du fichier MCD", e);
        }

        return allObjects;
    }




    @Override
    public String parse() {

        List<Object> allObjects = loadObjects();
        StringBuilder sb = new StringBuilder();

        sb.append("--- DÉTAILS DU MODÈLE MCD (Données) ---\n");


        sb.append("[ENTITÉS]\n");
        List<IhmEntite2> entities = findEntities(allObjects);
        if (entities.isEmpty()) {
            sb.append("- Aucune entité détectée.\n");
        } else {
            for (IhmEntite2 ent : entities) {
                sb.append("- ").append(ent.getEntite().getNom());
                sb.append("\n");
            }
        }


        sb.append("\n[RELATIONS ET CARDINALITÉS]\n");
        List<McdLink> links = findLinks(allObjects);
        if (links.isEmpty()) {
            sb.append("- Aucune relation détectée.\n");
        } else {
            for (McdLink link : links) {
                sb.append(String.format("- L'entité [%s] est liée à la relation [%s] avec la cardinalité (%s)\n",
                        link.getEntity(),
                        link.getRelationship(),
                        link.getCardinality()));
            }
        }

        return sb.toString();
    }

    //specific methods to mcd objects


    /**
     * The function `findEntities` takes a list of objects and returns a list of `IhmEntite2` entities
     * from the input list.
     * 
     * @param allObjects A list of objects of type Object.
     * @return The method `findEntities` returns a list of `IhmEntite2` entities that are found within
     * the input list `allObjects`.
     */
    public List<IhmEntite2> findEntities(List<Object> allObjects) {
        List<IhmEntite2> entities = new ArrayList<>();
        for (Object o : allObjects) {
            if (o instanceof IhmEntite2 entity) {
                entities.add(entity);
            }
        }
        return entities;

    }


    /**
     * The function `findRelations` filters a list of objects to return only instances of
     * `IhmRelation2`.
     * 
     * @param allObjects The `allObjects` parameter is a list of objects of type `Object`. The method
     * `findRelations` iterates through this list and adds objects of type `IhmRelation2` to a new list
     * called `relationships`, which is then returned.
     * @return The method `findRelations` returns a list of `IhmRelation2` objects that are found
     * within the input list `allObjects`.
     */
    public List<IhmRelation2> findRelations(List<Object> allObjects) {
        List<IhmRelation2> relationships = new ArrayList<>();
        for(Object o : allObjects) {
            if (o instanceof IhmRelation2 relation) {
                relationships.add(relation);
            }
        }
        return relationships;
    }


    /**
     * The function `findAttributes` filters a list of objects to only include those whose class name
     * contains "Attribut".
     * 
     * @param allObjects A list of objects from which you want to find attributes.
     * @return The `findAttributes` method returns a List of Objects that are considered attributes
     * based on the class name containing the substring "Attribut".
     */
    public List<Object> findAttributes(List<Object> allObjects) {
        List<Object> attributes = new ArrayList<>();
        for(Object o : allObjects) {
            String className = o.getClass().getSimpleName();

            if (className.contains("Attribut")) {
                attributes.add(o);
            }
        }
        return attributes;
    }

    /**
     * The function `findLinks` takes a list of objects, filters out objects of type `IhmLien2`,
     * extracts specific information from them, and creates `McdLink` objects with that information.
     * 
     * @param allObjects A list of objects that may contain instances of the `IhmLien2` class.
     * @return The method `findLinks` returns a list of `McdLink` objects that are extracted from the
     * input list of `Object` objects. The method iterates through each object in the input list,
     * checks if it is an instance of `IhmLien2`, extracts relevant information (entity, relationship,
     * cardinality) from the object, and creates a new `McdLink` object
     */
    public List<McdLink> findLinks(List<Object> allObjects){
        List<McdLink> links = new ArrayList<>();
        for(Object o : allObjects) {
            if(o instanceof IhmLien2 link) {
                String entity = link.getEntite().getEntite().getNom();
                String relationship = link.getRelation().getRelation().getNom();
                String cardinality = String.valueOf(link.getCardinalite());

                links.add(new  McdLink(entity, relationship, cardinality));


            }
        }
        return links;
    }


}






