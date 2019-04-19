package com.objectpartners.kafka.streamjoin.generator.api

import javax.validation.constraints.NotBlank

class CreateNameRequest {

    @NotBlank
    String firstName

    @NotBlank
    String lastName

    @NotBlank
    String personId
}
