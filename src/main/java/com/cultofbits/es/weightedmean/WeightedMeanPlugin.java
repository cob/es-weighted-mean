package com.cultofbits.es.weightedmean;

import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.search.SearchModule;

public class WeightedMeanPlugin extends Plugin {
    @Override
    public String name() {
        return "weighted-mean-plugin";
    }

    @Override
    public String description() {
        return "Weighted arithmetic mean aggregation";
    }

    public void onModule(SearchModule module) {
        module.registerAggregatorParser(WeightedMeanParser.class);

        InternalWeightedMean.registerStreams();
    }
}
