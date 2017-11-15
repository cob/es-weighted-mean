package com.cultofbits.es.weightedmean;

import org.elasticsearch.common.ParseField;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ObjectParser;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.query.QueryParseContext;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.support.*;
import org.elasticsearch.search.internal.SearchContext;

import java.io.IOException;

public class WeightedMeanAggregationBuilder extends ValuesSourceAggregationBuilder.LeafOnly<ValuesSource.Numeric, WeightedMeanAggregationBuilder> {
    public static final String NAME = "weighted-mean";

    private static final ObjectParser<WeightedMeanAggregationBuilder, QueryParseContext> PARSER;

    static {
        PARSER = new ObjectParser<>(WeightedMeanAggregationBuilder.NAME);
        PARSER.declareString(WeightedMeanAggregationBuilder::value, new ParseField("value"));
        PARSER.declareString(WeightedMeanAggregationBuilder::weight, new ParseField("weight"));
    }

    public static AggregationBuilder parse(String aggregationName, QueryParseContext context) throws IOException {
        return PARSER.parse(context.parser(), new WeightedMeanAggregationBuilder(aggregationName), context);
    }

    private String valueField;
    private String weightField;


    public WeightedMeanAggregationBuilder value(String valueField) {
        this.valueField = valueField;
        return this;
    }
    public String value() {
        return valueField;
    }

    public WeightedMeanAggregationBuilder weight(String weightField) {
        this.weightField = weightField;
        return this;
    }
    public String weight() {
        return weightField;
    }


    @Override
    protected ValuesSourceAggregatorFactory<ValuesSource.Numeric, ?> innerBuild(
        SearchContext context,
        ValuesSourceConfig<ValuesSource.Numeric> config,
        AggregatorFactory<?> parent,
        AggregatorFactories.Builder subFactoriesBuilder
    ) throws IOException {
        return null;
    }


    // Os métodos abaixo foram copiados da AvgAggregationBuilder

    public WeightedMeanAggregationBuilder(String name) {
        super(name, ValuesSourceType.NUMERIC, ValueType.NUMERIC);
    }

    protected WeightedMeanAggregationBuilder(StreamInput in) throws IOException {
        super(in, ValuesSourceType.NUMERIC, ValueType.NUMERIC);
    }

    @Override
    protected XContentBuilder doXContentBody(XContentBuilder builder, Params params) throws IOException {
        return builder;
    }

    @Override
    public String getType() {
        return NAME;
    }

    @Override
    protected void innerWriteTo(StreamOutput out) throws IOException {
        // Do nothing, no extra state to write to stream
    }

    @Override
    protected int innerHashCode() {
        return 0;
    }

    @Override
    protected boolean innerEquals(Object obj) {
        return true;
    }
}
