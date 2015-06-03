package com.cultofbits.es.weightedmean;

import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.search.aggregations.AggregationModule;

public class WeightedMeanPlugin extends AbstractPlugin {
    @Override
    public String name() {
        return "weighted-mean-plugin";
    }

    @Override
    public String description() {
        return "Weighted arithmetic mean aggregation";
    }

    public void onModule(AggregationModule module) {
        module.addAggregatorParser(WeightedMeanParser.class);

        InternalWeightedMean.registerStreams();
    }
}
