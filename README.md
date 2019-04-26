# kafka-demo

### Start the rest api

     ./gradlew :event-generator:run
     
### Bootstrap some data

    ./scripts/event-generator/bootstrap-data.sh
  
### Running KTable-KTable join stream

    ./gradlew :stream-join-ktable:run
    
### Running Aggregate-Topic join stream

    ./gradlew :stream-join-single-topic:run

### Validate Output

    ./scripts/consume-person-topic.sh
