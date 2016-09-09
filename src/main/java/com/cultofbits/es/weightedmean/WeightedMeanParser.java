package com.cultofbits.es.weightedmean;

import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.mapper.MappedFieldType;
import org.elasticsearch.search.SearchParseException;
import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.support.FieldContext;
import org.elasticsearch.search.aggregations.support.ValuesSource;
import org.elasticsearch.search.aggregations.support.ValuesSourceConfig;
import org.elasticsearch.search.internal.SearchContext;

import java.io.IOException;

public class WeightedMeanParser implements Aggregator.Parser {

    @Override
    public String type() {
        return InternalWeightedMean.TYPE.name();
    }


    public AggregatorFactory parse(String name, XContentParser parser, SearchContext context) throws IOException {

        ValuesSourceConfig<ValuesSource.Numeric> valueConfig = null;
        ValuesSourceConfig<ValuesSource.Numeric> weightConfig = null;


        XContentParser.Token token;
        String currentFieldName = null;

        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();

            } else if (token == XContentParser.Token.VALUE_STRING) {
                if("value".equals(currentFieldName)){
                    valueConfig = new ValuesSourceConfig<>(ValuesSource.Numeric.class);
                    String fieldName = parser.text();
                    MappedFieldType valueMapper = context.smartNameFieldType(fieldName);
                    if (valueMapper != null) {
                        valueConfig.fieldContext(new FieldContext(fieldName,
                                                                  context.fieldData().getForField(valueMapper),
                                                                  valueMapper));
                    } else {
                        valueConfig.unmapped(true);
                    }

                } else if("weight".equals(currentFieldName)){
                    weightConfig = new ValuesSourceConfig<>(ValuesSource.Numeric.class);
                    String fieldName = parser.text();
                    MappedFieldType weightMapper = context.smartNameFieldType(fieldName);
                    if (weightMapper != null) {
                        weightConfig.fieldContext(new FieldContext(fieldName,
                                                                   context.fieldData().getForField(weightMapper),
                                                                   weightMapper));
                    } else {
                        weightConfig.unmapped(true);
                    }
                }

            } else {
                throw new SearchParseException(context, "Unexpected token " + token + " in [" + name + "].", parser.getTokenLocation());
            }
        }



        return new WeightedMeanAggregatorFactory(name, valueConfig, weightConfig);
    }
}
