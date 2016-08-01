package com.cultofbits.es.weightedmean;

import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregation;

public interface WeightedMean extends NumericMetricsAggregation.SingleValue  {
    /**
     * The weighted mean value.
     */
    double getValue();

}
