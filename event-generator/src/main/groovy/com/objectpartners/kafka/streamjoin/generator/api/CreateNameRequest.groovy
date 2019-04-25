package com.objectpartners.kafka.streamjoin.generator.api

import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

class CreateNameRequest {

    @NotNull
    @Valid
    CreateNameRequestKey key

    @NotNull
    @Valid
    CreateNameRequestValue value

    static class CreateNameRequestKey {
        @NotBlank
        String personId
    }

    static class CreateNameRequestValue {
        @NotBlank
        String firstName

        @NotBlank
        String lastName
    }
}
