// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.evaluable.rename

import com.intellij.cce.evaluable.StrategyBuilder
import com.intellij.cce.util.getIfExists
import com.intellij.cce.util.getOrThrow


class RenameStrategyBuilder : StrategyBuilder<RenameStrategy> {
  override fun build(map: Map<String, Any>, language: String): RenameStrategy {
    val placeholderName = map.getOrThrow<String>("placeholderName")
    val filters = readFilters(map.getIfExists<Map<String, Any>>("filters"), language)
    return RenameStrategy(placeholderName, filters)
  }
}