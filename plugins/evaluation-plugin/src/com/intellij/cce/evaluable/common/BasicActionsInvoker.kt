// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.cce.evaluable.common

import com.intellij.cce.actions.CodeGolfEmulation
import com.intellij.cce.actions.UserEmulator
import com.intellij.cce.actions.selectedWithoutPrefix
import com.intellij.cce.core.*
import com.intellij.cce.interpreter.ActionsInvoker
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.editorActions.CompletionAutoPopupHandler
import com.intellij.codeInsight.lookup.Lookup
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementPresentation
import com.intellij.codeInsight.lookup.LookupManager
import com.intellij.codeInsight.lookup.impl.LookupImpl
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.command.impl.UndoManagerImpl
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.editor.impl.TrailingSpacesStripper
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.testFramework.TestModeFlags
import java.io.File

abstract class BasicActionsInvoker(private val project: Project, private val language: Language) : ActionsInvoker {
  protected companion object {
    val LOG = Logger.getInstance(BasicActionsInvoker::class.java)
    const val LOG_MAX_LENGTH = 50
  }

  init {
    TestModeFlags.set(CompletionAutoPopupHandler.ourTestingAutopopup, true)
  }

  protected var editor: Editor? = null
  private var spaceStrippingEnabled: Boolean = true
  private val dumbService = DumbService.getInstance(project)

  override fun moveCaret(offset: Int) {
    LOG.info("Move caret. ${positionToString(offset)}")
    editor!!.caretModel.moveToOffset(offset)
    editor!!.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
  }

  override fun printText(text: String) {
    LOG.info("Print text: ${StringUtil.shortenPathWithEllipsis(text, LOG_MAX_LENGTH)}. ${positionToString(editor!!.caretModel.offset)}")
    val project = editor!!.project
    val runnable = Runnable { EditorModificationUtil.insertStringAtCaret(editor!!, text) }
    WriteCommandAction.runWriteCommandAction(project) {
      val lookup = LookupManager.getActiveLookup(editor) as? LookupImpl
      if (lookup != null) {
        lookup.replacePrefix(lookup.additionalPrefix, lookup.additionalPrefix + text)
      }
      else {
        runnable.run()
      }
    }
  }

  override fun deleteRange(begin: Int, end: Int) {
    val document = editor!!.document
    val textForDelete = StringUtil.shortenPathWithEllipsis(document.text.substring(begin, end), LOG_MAX_LENGTH)
    LOG.info("Delete range. Text: $textForDelete. Begin: ${positionToString(begin)} End: ${positionToString(end)}")
    val project = editor!!.project
    val runnable = Runnable { document.deleteString(begin, end) }
    WriteCommandAction.runWriteCommandAction(project, runnable)
    if (editor!!.caretModel.offset != begin) editor!!.caretModel.moveToOffset(begin)
  }

  override fun openFile(file: String): String {
    LOG.info("Open file: $file")
    val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(File(file))
    val descriptor = OpenFileDescriptor(project, virtualFile!!)
    spaceStrippingEnabled = TrailingSpacesStripper.isEnabled(virtualFile)
    TrailingSpacesStripper.setEnabled(virtualFile, false)
    val fileEditor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true)
                     ?: throw Exception("Can't open text editor for file: $file")
    editor = fileEditor
    return fileEditor.document.text
  }

  override fun closeFile(file: String) {
    LOG.info("Close file: $file")
    val virtualFile = LocalFileSystem.getInstance().findFileByIoFile(File(file))!!
    TrailingSpacesStripper.setEnabled(virtualFile, spaceStrippingEnabled)
    FileEditorManager.getInstance(project).closeFile(virtualFile)
    editor = null
  }

  override fun isOpen(file: String): Boolean {
    val isOpen = FileEditorManager.getInstance(project).openFiles.any { it.path == file }
    return isOpen
  }

  override fun save() {
    val document = editor?.document ?: throw IllegalStateException("No open editor")
    FileDocumentManager.getInstance().saveDocumentAsIs(document)
  }

  override fun getText(): String = editor?.document?.text ?: throw IllegalStateException("No open editor")

  protected fun positionToString(offset: Int): String {
    val logicalPosition = editor!!.offsetToLogicalPosition(offset)
    return "Offset: $offset, Line: ${logicalPosition.line}, Column: ${logicalPosition.column}."
  }

  protected fun LookupImpl.finish(expectedItemIndex: Int, completionLength: Int): Boolean {
    selectedIndex = expectedItemIndex
    val document = editor.document
    val lengthBefore = document.textLength
    try {
      finishLookup(Lookup.AUTO_INSERT_SELECT_CHAR, items[expectedItemIndex])
    }
    catch (e: Throwable) {
      LOG.warn("Lookup finishing error.", e)
      return false
    }
    if (lengthBefore + completionLength != document.textLength) {
      LOG.info("Undo operation after finishing completion.")
      UndoManagerImpl.getInstance(project).undo(FileEditorManager.getInstance(project).selectedEditor)
      return false
    }
    return true
  }

  private fun hideLookup() = (LookupManager.getActiveLookup(editor) as? LookupImpl)?.hide()

  protected fun LookupElement.asSuggestion(): Suggestion {
    val presentation = LookupElementPresentation()
    renderElement(presentation)
    val presentationText = "${presentation.itemText}${presentation.tailText ?: ""}" +
                           if (presentation.typeText != null) ": " + presentation.typeText else ""

    val insertedText = if (lookupString.contains('>')) lookupString.replace(Regex("<.+>"), "")
    else lookupString
    return Suggestion(insertedText, presentationText, sourceFromPresentation(presentation))
  }

  private fun sourceFromPresentation(presentation: LookupElementPresentation): SuggestionSource {
    val icon = presentation.icon
    val typeText = presentation.typeText

    return when {
      icon is IconLoader.CachedImageIcon && icon.originalPath == "/icons/codota-color-icon.png" -> SuggestionSource.CODOTA
      typeText == "@tab-nine" -> SuggestionSource.TAB_NINE
      typeText == "full-line" -> SuggestionSource.INTELLIJ
      else -> SuggestionSource.STANDARD
    }
  }

}