package com.traxxiontech.syncplayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testLrcParsing() {
    val lrc = """
      [00:12.50]First line of lyrics
      [01:05.00]Chorus line starts here
    """.trimIndent()
    val lrcRegex = Regex("""\[(\d{2}):(\d{2})(?:\.(\d{2,3}))?](.*)""")
    val match = lrcRegex.find(lrc.lines()[0].trim())
    assertNotNull(match)
    val (minStr, secStr, msStr, text) = match!!.destructured
    assertEquals("00", minStr)
    assertEquals("12", secStr)
    assertEquals("50", msStr)
    assertEquals("First line of lyrics", text.trim())
  }
}
