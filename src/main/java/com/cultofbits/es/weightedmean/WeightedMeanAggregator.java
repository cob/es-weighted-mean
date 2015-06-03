package com.cultofbits.es.weightedmean;

import org.apache.lucene.index.AtomicReaderContext;
import org.elasticsearch.common.lease.Releasables;
import org.elasticsearch.common.util.DoubleArray;
import org.elasticsearch.index.fielddata.SortedNumericDoubleValues;
import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregator;
import org.elasticsearch.search.aggregations.support.AggregationContext;
import org.elasticsearch.search.aggregations.support.ValuesSource;

import java.io.IOException;

public class WeightedMeanAggregator extends NumericMetricsAggregator.SingleValue {


    private ValuesSource.Numeric valuesSource;
    private ValuesSource.Numeric weightsSource;

    private DoubleArray sumsOfProducts;
    private DoubleArray sumsOfWeights;

    private SortedNumericDoubleValues values;
    private SortedNumericDoubleValues weights;



    protected WeightedMeanAggregator(String name, long estimatedBucketsCount,
                                     ValuesSource.Numeric valuesSource, ValuesSource.Numeric weightsSource,
                                     AggregationContext context, Aggregator parent) {
        super(name, estimatedBucketsCount, context, parent);
        this.valuesSource = valuesSource;
        this.weightsSource = weightsSource;
        if(valuesSource != null && weightsSource != null){
            final long initialSize = estimatedBucketsCount < 2 ? 1 : estimatedBucketsCount;
            sumsOfProducts = bigArrays.newDoubleArray(initialSize, true);
            sumsOfWeights = bigArrays.newDoubleArray(initialSize, true);

        }
    }

    @Override
    public boolean shouldCollect() {
        return valuesSource != null && weightsSource != null;
    }

    @Override
    public void setNextReader(AtomicReaderContext reader) {
        values = valuesSource.doubleValues();
        weights = weightsSource.doubleValues();
    }

    @Override
    public void collect(int docId, long bucketOrdinal) throws IOException {
        sumsOfProducts = bigArrays.grow(sumsOfProducts, bucketOrdinal + 1);
        sumsOfWeights = bigArrays.grow(sumsOfWeights, bucketOrdinal + 1);

        weights.setDocument(docId);
        final int weightCount = weights.count();
        double weight = 0;
        for (int i = 0; i < weightCount; i++) {
            weight += weights.valueAt(i);
        }
        weight = weight / weightCount; //should only be a single value, if not we'll use the avg

        values.setDocument(docId);
        final int valueCount = values.count();
        double value = 0;
        for (int i = 0; i < valueCount; i++) {
             value += values.valueAt(i);
        }

        sumsOfProducts.increment(bucketOrdinal, weight * value);
        sumsOfWeights.increment(bucketOrdinal, weight);
    }

    @Override
    public double metric(long owningBucketOrd) {
        return valuesSource == null || weightsSource == null
               ? Double.NaN
            : sumsOfProducts.get(owningBucketOrd) / sumsOfWeights.get(owningBucketOrd);
    }

    @Override
    public InternalAggregation buildAggregation(long owningBucketOrdinal) {
        if(valuesSource == null || weightsSource == null
            || owningBucketOrdinal >= sumsOfWeights.size() || owningBucketOrdinal >= sumsOfProducts.size()){
            return buildEmptyAggregation();
        }
        return new InternalWeightedMean(name,
                                        sumsOfProducts.get(owningBucketOrdinal),
                                        sumsOfWeights.get(owningBucketOrdinal));
    }

    @Override
    public InternalAggregation buildEmptyAggregation() {
        return new InternalWeightedMean(name, 0, 0);
    }

    @Override
    protected void doClose() {
        Releasables.close(sumsOfProducts, sumsOfWeights);
    }
}
