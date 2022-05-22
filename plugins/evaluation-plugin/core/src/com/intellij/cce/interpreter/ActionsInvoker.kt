package com.intellij.cce.interpreter

import com.intellij.cce.core.Lookup
import com.intellij.cce.core.Session
import com.intellij.cce.core.TokenProperties

interface ActionsInvoker {
  fun moveCaret(offset: Int)
  fun callFeature(expectedText: String, prefix: String?, offset: Int): Lookup
  fun finishSession(expectedText: String, prefix: String): Boolean
  fun printText(text: String)
  fun deleteRange(begin: Int, end: Int)
  fun openFile(file: String): String
  fun closeFile(file: String)
  fun isOpen(file: String): Boolean
  fun save()
  fun getText(): String
  //fun emulateUserSession(expectedText: String,
  //                       nodeProperties: TokenProperties,
  //                       offset: Int): Session

  //fun emulateCodeGolfSession(expectedLine: String, offset: Int, nodeProperties: TokenProperties): Session
}
