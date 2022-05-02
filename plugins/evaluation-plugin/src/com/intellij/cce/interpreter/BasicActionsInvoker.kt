package com.intellij.cce.interpreter

import com.intellij.cce.actions.CodeGolfEmulation
import com.intellij.cce.actions.UserEmulator
import com.intellij.cce.actions.selectedWithoutPrefix
import com.intellij.cce.core.*
import com.intellij.cce.evaluation.CodeCompletionHandlerFactory
import com.intellij.codeInsight.completion.CodeCompletionHandlerBase
import com.intellij.codeInsight.completion.CompletionProgressIndicator
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.editorActions.CompletionAutoPopupHandler
import com.intellij.codeInsight.lookup.*
import com.intellij.codeInsight.lookup.Lookup
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


abstract class BasicActionsInvoker(private val project: Project,
                                   private val language: Language,
                                   completionType: com.intellij.cce.actions.CompletionType,
                                   userEmulationSettings: UserEmulator.Settings?,
                                   private val codeGolfSettings: CodeGolfEmulation.Settings?) : ActionsInvoker {
  protected companion object {
    val LOG = Logger.getInstance(BasicActionsInvoker::class.java)
    const val LOG_MAX_LENGTH = 50
  }

  init {
    TestModeFlags.set(CompletionAutoPopupHandler.ourTestingAutopopup, true)
  }

  protected val completionType = when (completionType) {
    com.intellij.cce.actions.CompletionType.SMART -> CompletionType.SMART
    else -> CompletionType.BASIC
  }
  protected var editor: Editor? = null
  private var spaceStrippingEnabled: Boolean = true
  private val userEmulator: UserEmulator = UserEmulator.create(userEmulationSettings)
  private val dumbService = DumbService.getInstance(project)
  
  override fun moveCaret(offset: Int) {
    LOG.info("Move caret. ${positionToString(offset)}")
    editor!!.caretModel.moveToOffset(offset)
    editor!!.scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
  }

//  override fun callCompletion(expectedText: String, prefix: String?): com.intellij.cce.core.Lookup {
//    LOG.info("Call completion. Type: $completionType. ${positionToString(editor!!.caretModel.offset)}")
////        assert(!dumbService.isDumb) { "Calling completion during indexing." }
//
//    val start = System.currentTimeMillis()
//    val isNew = LookupManager.getActiveLookup(editor) == null
//    val activeLookup = invokeCompletion(expectedText, prefix)
//    val latency = System.currentTimeMillis() - start
//    if (activeLookup == null) {
//      return com.intellij.cce.core.Lookup.fromExpectedText(expectedText, prefix ?: "", emptyList(), latency, isNew = isNew)
//    }
//
//    val lookup = activeLookup as LookupImpl
//    val features = MLCompletionFeaturesUtil.getCommonFeatures(lookup)
//    val resultFeatures = Features(
//      CommonFeatures(features.context, features.user, features.session),
//      lookup.items.map { MLCompletionFeaturesUtil.getElementFeatures(lookup, it).features }
//    )
//    val suggestions = lookup.items.map { it.asSuggestion() }
//
//    return com.intellij.cce.core.Lookup.fromExpectedText(expectedText, lookup.prefix(), suggestions, latency, resultFeatures, isNew)
//  }
//
//  override fun callRename(expectedName: String, offset: Int): com.intellij.cce.core.Lookup {
//    LOG.info("Call rename. Type: $completionType. ${positionToString(editor!!.caretModel.offset)}")
//    //        assert(!dumbService.isDumb) { "Calling completion during indexing." }
//
//    val start = System.currentTimeMillis()
//
//    val myEditor = editor
//
//    if (myEditor != null) {
//      val dataContext = DataManager.getInstance().getDataContext(myEditor.component)
//      val anActionEvent = AnActionEvent(null, dataContext, "", Presentation(), ActionManager.getInstance(), 0)
//      RenameElementAction().actionPerformed(anActionEvent)
//    }
//    val activeLookup = LookupManager.getActiveLookup(myEditor)
//
//    var suggestions = listOf<Suggestion>()
//    var resultFeatures = Features.EMPTY
//
//    if (activeLookup != null) {
//      val lookup = activeLookup as LookupImpl
//      val features = MLCompletionFeaturesUtil.getCommonFeatures(lookup)
//      resultFeatures = Features(
//        CommonFeatures(features.context, features.user, features.session),
//        lookup.items.map { MLCompletionFeaturesUtil.getElementFeatures(lookup, it).features }
//      )
//      suggestions = lookup.items.map { it.asSuggestion() }
//    }
//
//    val latency = System.currentTimeMillis() - start
//
//    return com.intellij.cce.core.Lookup.fromExpectedText(expectedName, "", suggestions, latency, resultFeatures)
//  }
//
//  override fun finishCompletion(expectedText: String, prefix: String): Boolean {
//    LOG.info("Finish completion. Expected text: $expectedText")
//    if (completionType == CompletionType.SMART) return false
//    val lookup = LookupManager.getActiveLookup(editor) as? LookupImpl ?: return false
//    val expectedItemIndex = lookup.items.indexOfFirst { it.lookupString == expectedText }
//    try {
//      return if (expectedItemIndex != -1) lookup.finish(expectedItemIndex, expectedText.length - prefix.length) else false
//    }
//    finally {
//      lookup.hide()
//    }
//  }
//
//  override fun finishRename(expectedText: String) {
//    LOG.info("Finish rename. Expected text: $expectedText")
//    val lookup = LookupManager.getActiveLookup(editor) as? LookupImpl ?: return
//    lookup.hide()
//    if (editor != null) {
//      InplaceRefactoring.getActiveInplaceRenamer(editor).finish(true)
//      PsiDocumentManager.getInstance(project).commitAllDocuments()
//    }
//  }

  override fun printText(text: String) {
    LOG.info("Print text: ${StringUtil.shortenPathWithEllipsis(text, LOG_MAX_LENGTH)}. ${positionToString(editor!!.caretModel.offset)}")
    val project = editor!!.project
    val runnable = Runnable { EditorModificationUtil.insertStringAtCaret(editor!!, text) }
    WriteCommandAction.runWriteCommandAction(project) {
      val lookup = LookupManager.getActiveLookup(editor) as? LookupImpl
      if (lookup != null) {
        lookup.replacePrefix(lookup.additionalPrefix, lookup.additionalPrefix + text)
      } else {
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

  override fun emulateUserSession(expectedText: String, nodeProperties: TokenProperties, offset: Int): Session {
    val editorImpl = editor as EditorImpl
    val session = Session(offset, expectedText, null, nodeProperties)
    val firstPrefixLen = userEmulator.firstPrefixLen()
    if (firstPrefixLen >= expectedText.length) {
      printText(expectedText)
      return session
    }
    var currentPrefix = expectedText.substring(0, firstPrefixLen)
    for (ch in currentPrefix) editorImpl.type(ch.toString())
    var order = 0
    while (currentPrefix.length < expectedText.length) {
      val position = callFeature(expectedText, currentPrefix)
        .also { session.addLookup(it) }
        .selectedPosition

      if (position != -1 && userEmulator.selectElement(position, currentPrefix.length)) {
        val success = finishSession(expectedText, currentPrefix)
        if (!success) printText(expectedText.substring(currentPrefix.length))
        session.success = success
        break
      }
      editorImpl.type(expectedText[currentPrefix.length].toString())
      currentPrefix += expectedText[currentPrefix.length]
      order++
    }
    hideLookup()
    return session
  }

  override fun emulateCodeGolfSession(expectedLine: String, offset: Int, nodeProperties: TokenProperties): Session {
    val document = editor!!.document
    val emulator = CodeGolfEmulation.createFromSettings(codeGolfSettings, expectedLine)
    val session = Session(offset, expectedLine, null, nodeProperties)
    val line = document.getLineNumber(offset)
    var currentString = ""

    while (currentString != expectedLine) {
      val lookup = callFeature(expectedLine, null)

      emulator.pickBestSuggestion(currentString, lookup, session).also {
        printText(it.selectedWithoutPrefix() ?: expectedLine[currentString.length].toString())
        currentString = document.getText(TextRange(offset, document.getLineEndOffset(line)))

        if (currentString.isNotEmpty()) {
          if (it.suggestions.isEmpty() || currentString.last().let { ch -> !(ch == '_' || ch.isLetter() || ch.isDigit()) }) {
            LookupManager.hideActiveLookup(project)
          }
        }
        session.addLookup(it)
      }
    }
    return session
  }

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

