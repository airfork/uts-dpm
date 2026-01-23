package com.tunjicus.utsdpm.models

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AutogenDpmTest {

  @Test
  fun `should extract location as OTR when description starts with bracket`() {
    val (location, _) = AutogenDpm.parseDescription("[1] Some description")
    assertThat(location).isEqualTo("OTR")
  }

  @Test
  fun `should extract first word as location when not starting with bracket`() {
    val (location, _) = AutogenDpm.parseDescription("LOCATION Some other text")
    assertThat(location).isEqualTo("LOCATION")
  }

  @Test
  fun `should extract notes from complete braces`() {
    val (_, notes) = AutogenDpm.parseDescription("LOCATION {These are notes}")
    assertThat(notes).isEqualTo("These are notes")
  }

  @Test
  fun `should extract notes from incomplete braces`() {
    val (_, notes) = AutogenDpm.parseDescription("LOCATION {Incomplete notes without closing")
    assertThat(notes).isEqualTo("Incomplete notes without closing")
  }

  @Test
  fun `should return empty notes when no braces present`() {
    val (_, notes) = AutogenDpm.parseDescription("LOCATION No braces here")
    assertThat(notes).isEmpty()
  }

  @Test
  fun `should handle empty description`() {
    val (location, notes) = AutogenDpm.parseDescription("")
    assertThat(location).isEmpty()
    assertThat(notes).isEmpty()
  }

  @Test
  fun `should handle description with only bracket`() {
    val (location, notes) = AutogenDpm.parseDescription("[")
    assertThat(location).isEqualTo("OTR")
    assertThat(notes).isEmpty()
  }
}
