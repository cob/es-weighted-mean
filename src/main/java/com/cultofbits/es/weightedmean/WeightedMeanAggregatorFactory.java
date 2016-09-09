package com.cultofbits.es.weightedmean;

import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import org.elasticsearch.search.aggregations.support.AggregationContext;
import org.elasticsearch.search.aggregations.support.ValuesSource;
import org.elasticsearch.search.aggregations.support.ValuesSourceConfig;
import org.elasticsearch.search.internal.SearchContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class WeightedMeanAggregatorFactory extends AggregatorFactory {

    private ValuesSourceConfig<ValuesSource.Numeric> valueConfig;
    private ValuesSourceConfig<ValuesSource.Numeric> weightConfig;

    public WeightedMeanAggregatorFactory(String name, ValuesSourceConfig valueConfig, ValuesSourceConfig weightConfig) {
        super(name, InternalWeightedMean.TYPE.name());
        this.valueConfig = valueConfig;
        this.weightConfig = weightConfig;
    }

    @Override
    protected Aggregator createInternal(AggregationContext context, Aggregator parent,
                                        boolean collectsFromSingleBucket,
                                        List<PipelineAggregator> pipelineAggregators, Map<String, Object> metaData)
        throws IOException {
        if (valueConfig.unmapped() || weightConfig.unmapped())
            return new WeightedMeanAggregator(name, null, null, context, parent, pipelineAggregators, metaData);

        ValuesSource.Numeric values = context.valuesSource(valueConfig, SearchContext.current());
        ValuesSource.Numeric weights = context.valuesSource(weightConfig, SearchContext.current());

        return new WeightedMeanAggregator(name, values, weights, context, parent, pipelineAggregators, metaData);
    }

}
