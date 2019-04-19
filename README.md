# kafka-demo

### Creating initial data

    ./gradlew :data-generator:run
    
### Running a stream

    ./gradlew :stream-word-count:run


// TODO 
Stream to table join
Table to table join
(Copartitioning, state, aggregate  scheme per intermediate)

Stream to stream windows

idea is to go through/demo the options for how to do that, and the tradeoffs:
- windowed kstream-to-kstream join (works for some cases, but you lose state after window expires)
- ktable-to-ktable joins (works, but clumsy when you have many topics)
- rekey data from each topic into a single topic with different schemas (and use table.groupByKey().aggregate) ... IMO the cleanest implementation
let me know what you think