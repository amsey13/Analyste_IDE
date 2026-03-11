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



    public List<Object> loadObjects() {

        List<Object> allObjects = new ArrayList<>();
        try(ObjectInputStream ois = new ObjectInputStream(in);) {

            while (true) {
                try {
                    Object obj = ois.readObject();

                    if (obj instanceof ArrayList<?>) {
                        allObjects.addAll((ArrayList<?>) obj);
                    } else {
                        allObjects.add(obj);
                    }

                } catch (EOFException e) {
                    break; // fin du fichier
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Erreur lors du parsing du fichier MCD", e);
        }

        return allObjects;
    }

    //Method for the strategy implementation

    @Override
    public List<Actor> findActors() {
        List<Object> allObjects = loadObjects();
        List<Actor> actors = new ArrayList<>();
        for (Object o : allObjects) {
            if (o instanceof IhmEntite2 entity) {

                actors.add(new Actor(entity.getEntite().getNom()));
            }
        }
        return actors;
    }

    @Override
    public List<Flux> findFluxs() {
        List<Object> allObjects = loadObjects();
        List<Flux> fluxs = new ArrayList<>();
        for (Object o : allObjects) {
            if (o instanceof IhmLien2 link) {
                // Dans un MCD, un lien unit une Entité et une Relation
                String source = link.getEntite().getEntite().getNom();
                String target = link.getRelation().getRelation().getNom();
                String label = "Cardinalité: " + link.getCardinalite();

                fluxs.add(new Flux(label, source, target));
            }
        }
        return fluxs;
    }

    //specific methods to mcd objects


    public List<IhmEntite2> findEntities(List<Object> allObjects) {
        List<IhmEntite2> entities = new ArrayList<>();
        for (Object o : allObjects) {
            if (o instanceof IhmEntite2 entity) {
                entities.add(entity);
            }
        }
        return entities;

    }


    public List<IhmRelation2> findRelations(List<Object> allObjects) {
        List<IhmRelation2> relationships = new ArrayList<>();
        for(Object o : allObjects) {
            if (o instanceof IhmRelation2 relation) {
                relationships.add(relation);
            }
        }
        return relationships;
    }


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






