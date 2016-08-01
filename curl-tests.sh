#!/bin/bash

curl -s -XDELETE "http://localhost:9200/test/"

curl -s -XPOST localhost:9200/test -d '{
   "mappings" : {
      "test" : {
         "_source" : { "enabled": true },
         "properties" : {
            "value" : {"type": "double"},
            "weight" : {"type": "double"}
         }
      }
   }
}'


#curl  -v -0 -XGET localhost:9200/test/_mapping/test

for (( i = 1; i < 1001; i++ )); do
   #statements
   curl -XPOST 'http://localhost:9200/test/test/' -d "{
      \"value\" : $i,
      \"weight\" : $i
   }"
done
echo "========================="



curl -s -XGET 'http://localhost:9200/test/test/_search?search_type=count' -d '{
  "aggregations": {
    "my_agg": {
       "weighted-mean": {"value": "value", "weight": "weight" }
      }
   }
}'

echo
