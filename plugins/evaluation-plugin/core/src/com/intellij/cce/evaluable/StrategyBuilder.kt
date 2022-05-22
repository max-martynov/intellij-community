// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.evaluable

import com.intellij.cce.filter.EvaluationFilter
import com.intellij.cce.filter.EvaluationFilterManager

interface StrategyBuilder<T : EvaluationStrategy> {
  fun build(map: Map<String, Any>, language: String): T

  fun readFilters(map: Map<String, Any>?, language: String): MutableMap<String, EvaluationFilter> {
    val evaluationFilters = mutableMapOf<String, EvaluationFilter>()
    if (map == null)
      return evaluationFilters
    for ((id, description) in map) {
      val configuration = EvaluationFilterManager.getConfigurationById(id)
                          ?: throw IllegalStateException("Unknown filter: $id")
      assert(configuration.isLanguageSupported(language)) { "filter $id is not supported for this language" }
      evaluationFilters[id] = configuration.buildFromJson(description)
    }
    return evaluationFilters
  }

}

