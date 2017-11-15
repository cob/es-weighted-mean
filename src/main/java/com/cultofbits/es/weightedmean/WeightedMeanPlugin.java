package com.cultofbits.es.weightedmean;

import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.SearchPlugin;
import org.elasticsearch.search.SearchModule;

import java.util.Collections;
import java.util.List;

public class WeightedMeanPlugin extends Plugin implements SearchPlugin {

    @Override
    public List<AggregationSpec> getAggregations() {
        return Collections.singletonList(new AggregationSpec(
            WeightedMeanAggregationBuilder.NAME,
            WeightedMeanAggregationBuilder::new,
            WeightedMeanAggregationBuilder::parse
            ));
    }
}
