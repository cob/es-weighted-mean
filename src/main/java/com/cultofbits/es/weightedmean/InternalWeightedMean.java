package com.cultofbits.es.weightedmean;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.DocValueFormat;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.metrics.InternalNumericMetricsAggregation;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class InternalWeightedMean extends InternalNumericMetricsAggregation.SingleValue implements WeightedMean {

    private double sumOfProducts;
    private double sumOfWeights;

    public InternalWeightedMean(String name, double sumOfProducts, double sumOfWeights, DocValueFormat format,
                                List<PipelineAggregator> pipelineAggregators, Map<String, Object> metaData) {
        super(name, pipelineAggregators, metaData);
        this.sumOfProducts = sumOfProducts;
        this.sumOfWeights = sumOfWeights;
        this.format = format;
    }

    public InternalWeightedMean(StreamInput in) throws IOException {
        super(in);
        format = in.readNamedWriteable(DocValueFormat.class);
        sumOfProducts = in.readDouble();
        sumOfWeights = in.readDouble();
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeNamedWriteable(format);
        out.writeDouble(sumOfProducts);
        out.writeDouble(sumOfWeights);
    }

    @Override
    public double value() {
        return getValue();
    }

    @Override
    public double getValue() {
        return sumOfProducts / sumOfWeights;
    }

    @Override
    public String getWriteableName() {
        return WeightedMeanAggregationBuilder.NAME;
    }

    @Override
    public InternalAggregation doReduce(List<InternalAggregation> aggregations, ReduceContext reduceContext) {
        double sumOfProducts = 0;
        double sumOfWeights = 0;
        for (InternalAggregation aggregation : aggregations) {
            sumOfProducts += ((InternalWeightedMean) aggregation).sumOfProducts;
            sumOfWeights += ((InternalWeightedMean) aggregation).sumOfWeights;
        }
        return new InternalWeightedMean(getName(), sumOfProducts, sumOfWeights, format, pipelineAggregators(), getMetaData());
    }

    @Override
    public XContentBuilder doXContentBody(XContentBuilder builder, Params params) throws IOException {
        builder.field(CommonFields.VALUE.getPreferredName(), sumOfWeights != 0 ? value() : null);
        if (sumOfWeights != 0 && format != DocValueFormat.RAW) {
            builder.field(CommonFields.VALUE_AS_STRING.getPreferredName(), format.format(value()));
        }
        return builder;
    }
}
