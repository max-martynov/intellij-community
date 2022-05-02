// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.generalization

import com.intellij.cce.interpreter.ActionsInvoker


class RenameCore : FeatureCore {
  override val name = "rename"
  override val actionsGenerationStrategy = RenameStrategy()
  override val actionsInvoker: ActionsInvoker
    get() = featureInvoker

  lateinit var featureInvoker: FeatureInvoker

}