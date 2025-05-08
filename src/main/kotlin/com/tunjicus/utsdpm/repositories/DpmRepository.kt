package com.tunjicus.utsdpm.repositories

import com.tunjicus.utsdpm.entities.Dpm
import org.springframework.data.jpa.repository.JpaRepository

interface DpmRepository : JpaRepository<Dpm, Int> {}
