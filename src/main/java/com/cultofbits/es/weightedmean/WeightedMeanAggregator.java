package com.cultofbits.es.weightedmean;

import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.common.lease.Releasables;
import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.common.util.DoubleArray;
import org.elasticsearch.index.fielddata.SortedNumericDoubleValues;
import org.elasticsearch.search.DocValueFormat;
import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.LeafBucketCollector;
import org.elasticsearch.search.aggregations.LeafBucketCollectorBase;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregator;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;
import org.elasticsearch.search.aggregations.support.ValuesSource;
import org.elasticsearch.search.internal.SearchContext;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class WeightedMeanAggregator extends NumericMetricsAggregator.SingleValue {


    private ValuesSource.Numeric valuesSource;
    private ValuesSource.Numeric weightsSource;

    private DocValueFormat format;

    private DoubleArray sumsOfProducts;
    private DoubleArray sumsOfWeights;

    // todo jone ver se isto está a funcionar...


    protected WeightedMeanAggregator(String name,
                                     ValuesSource.Numeric valuesSource, ValuesSource.Numeric weightsSource,
                                     DocValueFormat formatter, SearchContext context, Aggregator parent,
                                     List<PipelineAggregator> pipelineAggregators, Map<String, Object> metaData)
        throws IOException {
        super(name, context, parent, pipelineAggregators, metaData);

        this.valuesSource = valuesSource;
        this.weightsSource = weightsSource;
        format = formatter;

        if (valuesSource != null && weightsSource != null) {
            final BigArrays bigArrays = context.bigArrays();

            sumsOfProducts = bigArrays.newDoubleArray(1, true);
            sumsOfWeights = bigArrays.newDoubleArray(1, true);

        }
    }

    @Override
    protected LeafBucketCollector getLeafCollector(LeafReaderContext ctx, LeafBucketCollector sub) throws IOException {

        if (valuesSource == null || weightsSource == null) {
            return LeafBucketCollector.NO_OP_COLLECTOR;
        }

        final BigArrays bigArrays = context.bigArrays();
        SortedNumericDoubleValues values = valuesSource.doubleValues(ctx);
        SortedNumericDoubleValues weights = weightsSource.doubleValues(ctx);

        return new LeafBucketCollectorBase(sub, values) {
            @Override
            public void collect(int doc, long bucket) throws IOException {
                sumsOfProducts = bigArrays.grow(sumsOfProducts, bucket + 1);
                sumsOfWeights = bigArrays.grow(sumsOfWeights, bucket + 1);

                weights.setDocument(doc);
                final int weightCount = weights.count();
                double weight = 0;
                for (int i = 0; i < weightCount; i++) {
                    weight += weights.valueAt(i);
                }
                weight = weight / weightCount; //should only be a single value, if not we'll use the avg

                values.setDocument(doc);
                final int valueCount = values.count();
                double value = 0;
                for (int i = 0; i < valueCount; i++) {
                    value += values.valueAt(i);
                }

                sumsOfProducts.increment(bucket, weight * value);
                sumsOfWeights.increment(bucket, weight);
            }
        };
    }


    @Override
    public double metric(long owningBucketOrd) {
        return valuesSource == null || weightsSource == null
               ? Double.NaN
               : sumsOfProducts.get(owningBucketOrd) / sumsOfWeights.get(owningBucketOrd);
    }

    @Override
    public InternalAggregation buildAggregation(long owningBucketOrdinal) {
        if (valuesSource == null || weightsSource == null
            || owningBucketOrdinal >= sumsOfWeights.size() || owningBucketOrdinal >= sumsOfProducts.size()) {
            return buildEmptyAggregation();
        }
        return new InternalWeightedMean(name,
                                        sumsOfProducts.get(owningBucketOrdinal),
                                        sumsOfWeights.get(owningBucketOrdinal),
                                        format,
                                        pipelineAggregators(), metaData());
    }

    @Override
    public InternalAggregation buildEmptyAggregation() {
        return new InternalWeightedMean(name, 0, 0, format, pipelineAggregators(), metaData());
    }

    @Override
    protected void doClose() {
        Releasables.close(sumsOfProducts, sumsOfWeights);
    }


}
