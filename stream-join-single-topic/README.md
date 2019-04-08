# stream-favorite-color

### produce data

    kafka-console-producer \
      --broker-list localhost:19092,localhost:29092,localhost:39092 \
      --topic stream-favorite-color-input

### print topics

    kafka-console-consumer \
      --bootstrap-server localhost:19092,localhost:29092,localhost:39092 \
      --topic stream-favorite-color-input \
      --from-beginning \
      --property print.key=true
    
    kafka-console-consumer \
      --bootstrap-server localhost:19092,localhost:29092,localhost:39092 \
      --topic stream-favorite-color-by-user \
      --from-beginning \
      --property print.key=true
      
    kafka-console-consumer \
      --bootstrap-server localhost:19092,localhost:29092,localhost:39092 \
      --topic stream-favorite-color-output \
      --from-beginning \
      --property print.key=true \
      --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
      
### print internal topics

    kafka-console-consumer \
      --bootstrap-server localhost:19092,localhost:29092,localhost:39092 \
      --topic favorite-color-stream-COUNTS-BY-COLOR-repartition \
      --from-beginning \
      --property print.key=true
      
    kafka-console-consumer \
      --bootstrap-server localhost:19092,localhost:29092,localhost:39092 \
      --topic favorite-color-stream-COUNTS-BY-COLOR-changelog \
      --from-beginning \
      --property print.key=true \
      --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer
      
### reset stream

    kafka-streams-application-reset \
      --application-id favorite-color-stream \
      --bootstrap-servers localhost:19092,localhost:29092,localhost:39092 \
      --input-topics stream-favorite-color-input \
      --intermediate-topics stream-favorite-color-by-user
