package com.intellij.cce.actions

import com.intellij.cce.core.CodeFragment
import com.intellij.cce.processor.DeleteScopesProcessor
import com.intellij.cce.processor.GenerateActionsProcessor
import com.intellij.cce.util.FileTextUtil.computeChecksum

class ActionsGenerator(private val generateActionsProcessor: GenerateActionsProcessor) {

  fun generate(code: CodeFragment): FileActions {
    val deletionVisitor = DeleteScopesProcessor()
//    if (strategy.context == CompletionContext.PREVIOUS) deletionVisitor.process(code)

    generateActionsProcessor.process(code)

    val actions: MutableList<Action> = mutableListOf()
    val completionActions = generateActionsProcessor.getActions()
    if (completionActions.isNotEmpty()) {
      actions.addAll(deletionVisitor.getActions().reversed())
      actions.addAll(completionActions)
    }
    return FileActions(code.path, computeChecksum(code.text),
                       actions.count { it is FinishSession || it is EmulateUserSession || it is CodeGolfSession }, actions)
  }
}
