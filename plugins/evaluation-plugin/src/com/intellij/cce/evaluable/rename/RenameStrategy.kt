// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.evaluable.rename

import com.intellij.cce.evaluable.EvaluationStrategy
import com.intellij.cce.filter.EvaluationFilter


data class RenameStrategy(
  val placeholderName: String,
  override val filters: Map<String, EvaluationFilter>
) : EvaluationStrategy