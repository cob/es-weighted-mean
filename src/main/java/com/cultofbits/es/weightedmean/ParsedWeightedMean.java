package com.cultofbits.es.weightedmean;

import org.elasticsearch.common.xcontent.ObjectParser;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.search.aggregations.metrics.ParsedSingleValueNumericMetricsAggregation;

import java.io.IOException;

public class ParsedWeightedMean extends ParsedSingleValueNumericMetricsAggregation implements WeightedMean {

    @Override
    public double getValue() {
        return value();
    }

    @Override
    public String getType() {
        return WeightedMeanAggregationBuilder.NAME;
    }

    private static final ObjectParser<ParsedWeightedMean, Void> PARSER = new ObjectParser<>(
        ParsedWeightedMean.class.getSimpleName(), true, ParsedWeightedMean::new);

    static {
        declareAggregationFields(PARSER);
        PARSER.declareDouble((agg, value) -> agg.value = value, CommonFields.VALUE);
    }

    public static ParsedWeightedMean fromXContent(XContentParser parser, final String name) {
        ParsedWeightedMean cardinality = PARSER.apply(parser, null);
        cardinality.setName(name);
        return cardinality;
    }


    @Override
    protected XContentBuilder doXContentBody(XContentBuilder builder, Params params) throws IOException {
        // InternalWeightedMean renders value only if the avg normalizer (count) is not 0. ???
        // We parse back `null` as Double.POSITIVE_INFINITY so we check for that value here to get the same xContent output
        boolean hasValue = value != Double.POSITIVE_INFINITY;
        builder.field(CommonFields.VALUE.getPreferredName(), hasValue ? value : null);
        if (hasValue && valueAsString != null) {
            builder.field(CommonFields.VALUE_AS_STRING.getPreferredName(), valueAsString);
        }
        return builder;
    }
}
