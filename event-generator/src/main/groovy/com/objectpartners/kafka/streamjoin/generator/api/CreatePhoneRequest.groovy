package com.objectpartners.kafka.streamjoin.generator.api

import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class CreatePhoneRequest {

    @NotNull
    @Valid
    CreatePhoneRequestKey key

    @NotNull
    @Valid
    CreatePhoneRequestValue value

    static class CreatePhoneRequestKey {
        @NotBlank
        String personId

        @NotNull
        ApiPhoneType type
    }

    static class CreatePhoneRequestValue {
        @NotBlank
        String phoneNumber
    }
}
