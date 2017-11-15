package com.cultofbits.es.weightedmean;

import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import org.elasticsearch.search.aggregations.support.ValuesSource;
import org.elasticsearch.search.aggregations.support.ValuesSourceConfig;
import org.elasticsearch.search.internal.SearchContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class WeightedMeanAggregatorFactory extends AggregatorFactory<WeightedMeanAggregatorFactory> {

    private ValuesSourceConfig<ValuesSource.Numeric> valueConfig;
    private ValuesSourceConfig<ValuesSource.Numeric> weightConfig;

    public WeightedMeanAggregatorFactory(String name,
                                         ValuesSourceConfig<ValuesSource.Numeric> valueConfig,
                                         ValuesSourceConfig<ValuesSource.Numeric> weightConfig,
                                         SearchContext context, AggregatorFactory<?> parent,
                                         AggregatorFactories.Builder subFactories,
                                         Map<String, Object> metaData) throws IOException {
        super(name, context, parent, subFactories, metaData);

        this.valueConfig = valueConfig;
        this.weightConfig = weightConfig;
    }

    @Override
    protected Aggregator createInternal(Aggregator parent, boolean collectsFromSingleBucket,
                                        List<PipelineAggregator> pipelineAggregators,
                                        Map<String, Object> metaData) throws IOException {
        if(valueConfig.unmapped() || weightConfig.unmapped()){
            return new WeightedMeanAggregator(name, null, null,
                                              valueConfig.format(),  //we use the same format for values and weights
                                              context, parent, pipelineAggregators, metaData);
        }

        return new WeightedMeanAggregator(name,
                                          valueConfig.toValuesSource(context.getQueryShardContext()),
                                          weightConfig.toValuesSource(context.getQueryShardContext()),
                                          valueConfig.format(),
                                          context, parent, pipelineAggregators, metaData);
    }
}
