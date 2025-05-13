package com.tunjicus.utsdpm.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class PutDpmGroupDto(
    @field:NotBlank("groupName cannot be blank") val groupName: String,
    @field:NotEmpty("dpms cannot be empty")
    val dpms: List<PutDpmTypeDto>
)
