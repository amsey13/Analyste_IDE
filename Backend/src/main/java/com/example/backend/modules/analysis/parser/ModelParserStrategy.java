package com.example.backend.modules.analysis.parser;

import com.example.backend.modules.analysis.model.Actor;
import com.example.backend.modules.analysis.model.Flux;

import java.util.List;

public interface ModelParserStrategy {

    public List<Actor> findActors();

    public List<Flux> findFluxs();
}
