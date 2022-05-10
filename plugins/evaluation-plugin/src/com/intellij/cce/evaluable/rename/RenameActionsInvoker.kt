// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.evaluable.rename

import com.intellij.cce.core.*
import com.intellij.cce.evaluable.common.BasicActionsInvoker
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.completion.ml.actions.MLCompletionFeaturesUtil
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.refactoring.actions.RenameElementAction
import com.intellij.refactoring.rename.inplace.InplaceRefactoring

class RenameActionsInvoker(private val project: Project,
                           private val language: Language,
                           private val strategy: RenameStrategy) : BasicActionsInvoker(project, language) {

  override fun callFeature(expectedText: String, prefix: String?): Lookup {
    LOG.info("Call rename. Expected text: $expectedText. ${positionToString(editor!!.caretModel.offset)}")
    //        assert(!dumbService.isDumb) { "Calling completion during indexing." }

    val start = System.currentTimeMillis()

    val myEditor = editor

    if (myEditor != null) {
      val dataContext = DataManager.getInstance().getDataContext(myEditor.component)
      val anActionEvent = AnActionEvent(null, dataContext, "", Presentation(), ActionManager.getInstance(), 0)
      RenameElementAction().actionPerformed(anActionEvent)
    }
    val activeLookup = LookupManager.getActiveLookup(myEditor)

    var suggestions = listOf<Suggestion>()
    var resultFeatures = Features.EMPTY

    if (activeLookup != null) {
      val lookup = activeLookup as LookupImpl
      val features = MLCompletionFeaturesUtil.getCommonFeatures(lookup)
      resultFeatures = Features(
        CommonFeatures(features.context, features.user, features.session),
        lookup.items.map { MLCompletionFeaturesUtil.getElementFeatures(lookup, it).features }
      )
      suggestions = lookup.items.map { it.asSuggestion() }
    }

    val latency = System.currentTimeMillis() - start

    return com.intellij.cce.core.Lookup.fromExpectedText(expectedText, "", suggestions, latency, resultFeatures)
  }

  override fun finishSession(expectedText: String, prefix: String): Boolean {
    LOG.info("Finish rename. Expected text: $expectedText")
    val lookup = LookupManager.getActiveLookup(editor) as? LookupImpl ?: return false
    lookup.hide()
    if (editor != null) {
      InplaceRefactoring.getActiveInplaceRenamer(editor).finish(true)
      PsiDocumentManager.getInstance(project).commitAllDocuments()
    }
    return false
  }

}