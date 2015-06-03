package com.cultofbits.es.weightedmean;

import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.support.AggregationContext;
import org.elasticsearch.search.aggregations.support.ValuesSource;
import org.elasticsearch.search.aggregations.support.ValuesSourceConfig;


public class WeightedMeanAggregatorFactory extends AggregatorFactory {

    private ValuesSourceConfig<ValuesSource.Numeric> valueConfig;
    private ValuesSourceConfig<ValuesSource.Numeric> weightConfig;

    public WeightedMeanAggregatorFactory(String name, ValuesSourceConfig valueConfig, ValuesSourceConfig weightConfig) {
        super(name, InternalWeightedMean.TYPE.name());
        this.valueConfig = valueConfig;
        this.weightConfig = weightConfig;
    }

    @Override
    public Aggregator create(AggregationContext context, Aggregator parent, long expectedBucketsCount) {
        if(valueConfig.unmapped() || weightConfig.unmapped())
            return new WeightedMeanAggregator(name, parent == null ? 1 : parent.estimatedBucketCount(),
                                              null, null,
                                              context, parent);

        ValuesSource.Numeric values = context.valuesSource(valueConfig, parent == null ? 0 : 1 + parent.depth());
        ValuesSource.Numeric weights = context.valuesSource(weightConfig, parent == null ? 0 : 1 + parent.depth());

        return new WeightedMeanAggregator(name, parent == null ? 1 : parent.estimatedBucketCount(),
                                          values, weights, context, parent);
    }
}
