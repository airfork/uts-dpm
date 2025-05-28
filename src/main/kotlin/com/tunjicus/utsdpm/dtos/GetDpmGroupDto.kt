package com.tunjicus.utsdpm.dtos

data class GetDpmGroupDto(val groupName: String, val dpms: List<GetDpmTypeDto>)
