package com.objectpartners.kafka.streamjoin.generator.controller

import com.objectpartners.kafka.streamjoin.generator.api.CreatePhoneRequest
import com.objectpartners.kafka.streamjoin.model.input.PhoneType
import com.objectpartners.kafka.streamjoin.model.input.Telephone
import com.objectpartners.kafka.streamjoin.model.input.TelephoneKey
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
        TelephoneKey key = TelephoneKey.newBuilder().setTelephoneId(request.getTelephoneId()).setPersonId(request.getPersonId()).build()
        Telephone value = Telephone.newBuilder().setPhoneNumber(request.getPhoneNumber()).setType(PhoneType.valueOf(request.getType().name())).build()
        RecordMetadata meta = producer.send(new ProducerRecord<SpecificRecord, SpecificRecord>('phone-topic', key, value)).get()
        return meta.toString()
    }
}
