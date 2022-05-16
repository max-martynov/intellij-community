package com.intellij.cce.visitor

import com.intellij.psi.*
import com.intellij.cce.core.*
import com.intellij.cce.visitor.exceptions.PsiConverterException

class JavaRenameVisitor : EvaluationVisitor, JavaRecursiveElementVisitor() {
  private var codeFragment: CodeFragment? = null

  override val language: Language = Language.JAVA
  override val feature: String = "rename"

  override fun getFile(): CodeFragment = codeFragment
                                         ?: throw PsiConverterException("Invoke 'accept' with visitor on PSI first")

  override fun visitJavaFile(file: PsiJavaFile) {
    codeFragment = CodeFragment(file.textOffset, file.textLength)
    super.visitJavaFile(file)
  }

  override fun visitVariable(variable: PsiVariable) {
    variable.name?.let { variableName ->
      val token = CodeToken(variableName, variable.textOffset, variableName.length, variableDeclarationProperties())
      codeFragment?.addChild(token)
    }
    super.visitVariable(variable)
  }

  private fun variableDeclarationProperties(): TokenProperties {
    return properties(TypeProperty.VARIABLE_DECLARATION, SymbolLocation.PROJECT) {}
  }

  private fun properties(tokenType: TypeProperty, location: SymbolLocation, init: JvmProperties.Builder.() -> Unit)
    : TokenProperties {
    return JvmProperties.create(tokenType, location) {
      init(this)
    }
  }

}