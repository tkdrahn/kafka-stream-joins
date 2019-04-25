package com.objectpartners.kafka.streamjoin.generator.controller

import com.objectpartners.kafka.streamjoin.generator.api.CreateNameRequest
import com.objectpartners.kafka.streamjoin.model.input.PersonName
import com.objectpartners.kafka.streamjoin.model.input.PersonNameKey
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

import javax.validation.Valid

@RestController
@RequestMapping('/name')
class NameController {

    private final Producer<SpecificRecord, SpecificRecord> producer

    @Autowired
    NameController(Producer<SpecificRecord, SpecificRecord> producer) {
        this.producer = producer
    }

    @RequestMapping(method = RequestMethod.POST)
    String publish(@RequestBody @Valid CreateNameRequest request) {
        PersonNameKey key = PersonNameKey.newBuilder().setPersonId(request.key.personId).build()
        PersonName value = PersonName.newBuilder().setFirstName(request.value.firstName).setLastName(request.value.lastName).build()
        RecordMetadata meta = producer.send(new ProducerRecord<SpecificRecord, SpecificRecord>('name-topic', key, value)).get()
        return meta.toString()
    }
}
