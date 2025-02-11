// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.codeInsight.daemon.impl

import com.intellij.codeInsight.codeVision.CodeVisionEntry
import com.intellij.codeInsight.hints.codeVision.DaemonBoundCodeVisionProvider
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile

abstract class JavaCodeVisionProviderBase : DaemonBoundCodeVisionProvider {

  final override fun computeForEditor(editor: Editor): List<Pair<TextRange, CodeVisionEntry>> {
    val project = editor.project ?: return emptyList()
    val psiDocumentManager = PsiDocumentManager.getInstance(project)
    val psiFile = psiDocumentManager.getPsiFile(editor.document) ?: return emptyList()
    if (psiFile.language != JavaLanguage.INSTANCE) return emptyList()

    return computeLenses(editor, psiFile)
  }

  abstract fun computeLenses(editor: Editor, psiFile: PsiFile): List<Pair<TextRange, CodeVisionEntry>>
}