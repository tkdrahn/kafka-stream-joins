package com.objectpartners.kafka.streamjoin.generator.api

import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class CreateEmailRequest {

    @NotNull
    @Valid
    CreateEmailRequestKey key

    @NotNull
    @Valid
    CreateEmailRequestValue value

    static class CreateEmailRequestKey {
        @NotBlank
        String personId

        @NotNull
        ApiEmailType type
    }

    static class CreateEmailRequestValue {
        @NotBlank
        String address
    }
}
