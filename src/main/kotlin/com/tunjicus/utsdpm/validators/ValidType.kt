package com.tunjicus.utsdpm.validators

import jakarta.validation.Constraint
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@Constraint(validatedBy = [ValidTypeValidator::class])
annotation class ValidType(
  val message: String = "Passed type is not a valid DPM type",
  val groups: Array<KClass<*>> = [],
  val payload: Array<KClass<out Payload>> = []
)
