package com.objectpartners.kafka.streamjoin.generator.api

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class CreateEmailRequest {

    @NotBlank
    String address

    @NotBlank
    String emailId

    @NotBlank
    String personId

    @NotNull
    ApiEmailType type
}
