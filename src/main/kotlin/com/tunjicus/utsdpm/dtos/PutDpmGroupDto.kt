package com.tunjicus.utsdpm.dtos

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size

data class PutDpmGroupDto(
    @field:NotBlank("groupName cannot be blank") val groupName: String,
    @field:NotEmpty("dpms cannot be empty")
    @field:Size(max = 100, message = "dpms list is too long, max is 100")
    val dpms: List<PutDpmTypeDto>
)
