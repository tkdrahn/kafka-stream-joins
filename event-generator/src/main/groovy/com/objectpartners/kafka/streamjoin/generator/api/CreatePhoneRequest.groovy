package com.objectpartners.kafka.streamjoin.generator.api

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class CreatePhoneRequest {

    @NotBlank
    String phoneNumber

    @NotBlank
    String telephoneId

    @NotBlank
    String personId

    @NotNull
    ApiPhoneType type
}
