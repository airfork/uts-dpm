package com.tunjicus.utsdpm.dtos

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Range

data class PutDpmTypeDto(
    @field:NotBlank("dpmType cannot be blank") val dpmType: String,
    @field:Range(min = -50, max = 50, message = "points must be between -50 and 50") val points: Int
)
