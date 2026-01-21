package com.tunjicus.utsdpm.services

import com.tunjicus.utsdpm.models.Shift

/**
 * Interface for providing shift data. Allows for swapping between real When2Work API
 * and mock implementations for local development and testing.
 */
interface ShiftProvider {
  fun getAssignedShifts(): List<Shift>
}
