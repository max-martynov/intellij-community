// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.evaluable

interface StrategyBuilder<T : EvaluationStrategy> {
  fun build(map: Map<String, Any>): T

  companion object {
    inline fun <reified T> Map<String, *>.getOrThrow(key: String): T {
      if (key !in this.keys) throw IllegalArgumentException("No key <$key> found in config")
      val value = this.getValue(key)
      check(value is T) { "Wrong type of key <$key> in config" }
      return value
    }
  }
}

