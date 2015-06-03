package com.cultofbits.es.weightedmean;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.aggregations.AggregationStreams;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.metrics.InternalNumericMetricsAggregation;
import org.elasticsearch.search.aggregations.support.format.ValueFormatterStreams;

import java.io.IOException;

public class InternalWeightedMean extends InternalNumericMetricsAggregation.SingleValue {

    public final static Type TYPE = new Type("weighted-mean");

    public final static AggregationStreams.Stream STREAM = new AggregationStreams.Stream() {
        @Override
        public InternalWeightedMean readResult(StreamInput in) throws IOException {
            InternalWeightedMean result = new InternalWeightedMean();
            result.readFrom(in);
            return result;
        }
    };

    public static void registerStreams() {
        AggregationStreams.registerStream(STREAM, TYPE.stream());
    }

    private double sumOfProducts;
    private double sumOfWeights;

    InternalWeightedMean() { } // for serialization

    public InternalWeightedMean(String name, double sumOfProducts, double sumOfWeights) {
        super(name);
        this.sumOfProducts = sumOfProducts;
        this.sumOfWeights = sumOfWeights;
    }

    @Override
    public double value() {
        return sumOfProducts / sumOfWeights;
    }

    @Override
    public Type type() {
        return TYPE;
    }


    @Override
    public InternalAggregation reduce(ReduceContext reduceContext) {
        double sumOfProducts = 0;
        double sumOfWeights = 0;
        for (InternalAggregation aggregation : reduceContext.aggregations()) {
            sumOfProducts += ((InternalWeightedMean) aggregation).sumOfProducts;
            sumOfWeights += ((InternalWeightedMean) aggregation).sumOfWeights;
        }
        return new InternalWeightedMean(getName(), sumOfProducts, sumOfWeights);
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        name = in.readString();
        valueFormatter = ValueFormatterStreams.readOptional(in);
        sumOfProducts = in.readDouble();
        sumOfWeights = in.readDouble();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(name);
        ValueFormatterStreams.writeOptional(valueFormatter, out);
        out.writeDouble(sumOfProducts);
        out.writeDouble(sumOfWeights);
    }

    @Override
    public XContentBuilder doXContentBody(XContentBuilder builder, Params params) throws IOException {
        builder.field(CommonFields.VALUE, sumOfWeights != 0 ? value() : null);
        if (sumOfWeights != 0 && valueFormatter != null) {
            builder.field(CommonFields.VALUE_AS_STRING, valueFormatter.format(value()));
        }
        return builder;
    }

}
