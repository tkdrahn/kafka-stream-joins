package com.objectpartners.kafka.streamjoin.generator.controller

import com.objectpartners.kafka.streamjoin.generator.api.CreateEmailRequest
import com.objectpartners.kafka.streamjoin.model.input.Email
import com.objectpartners.kafka.streamjoin.model.input.EmailKey
import com.objectpartners.kafka.streamjoin.model.input.EmailType
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
@RequestMapping('/email')
class EmailController {

    private final Producer<SpecificRecord, SpecificRecord> producer

    @Autowired
    EmailController(Producer<SpecificRecord, SpecificRecord> producer) {
        this.producer = producer
    }

    @RequestMapping(method = RequestMethod.POST)
    String publish(@RequestBody @Valid CreateEmailRequest request) {
        EmailKey key = EmailKey.newBuilder().setEmailId(request.getEmailId()).setPersonId(request.getPersonId()).build()
        Email value = Email.newBuilder().setAddress(request.getAddress()).setType(EmailType.valueOf(request.getType().name())).build()
        RecordMetadata meta = producer.send(new ProducerRecord<SpecificRecord, SpecificRecord>('email-topic', key, value)).get()
        return meta.toString()
    }
}
