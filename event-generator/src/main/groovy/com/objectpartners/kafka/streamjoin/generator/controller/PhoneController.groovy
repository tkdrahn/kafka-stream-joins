package com.objectpartners.kafka.streamjoin.generator.controller

import com.objectpartners.kafka.streamjoin.generator.api.CreatePhoneRequest
import com.objectpartners.kafka.streamjoin.model.input.Phone
import com.objectpartners.kafka.streamjoin.model.input.PhoneKey
import com.objectpartners.kafka.streamjoin.model.input.PhoneType
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
@RequestMapping('/phone')
class PhoneController {

    private final Producer<SpecificRecord, SpecificRecord> producer

    @Autowired
    PhoneController(Producer<SpecificRecord, SpecificRecord> producer) {
        this.producer = producer
    }

    @RequestMapping(method = RequestMethod.POST)
    String publish(@RequestBody @Valid CreatePhoneRequest request) {
        PhoneKey key = PhoneKey.newBuilder().setPersonId(request.key.personId).setType(PhoneType.valueOf(request.key.type.name())).build()
        Phone value = Phone.newBuilder().setPhoneNumber(request.value.phoneNumber).setType(PhoneType.valueOf(request.key.type.name())).build()
        RecordMetadata meta = producer.send(new ProducerRecord<SpecificRecord, SpecificRecord>('phone-topic', key, value)).get()
        return meta.toString()
    }
}
