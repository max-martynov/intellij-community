// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.uast.test.common.kotlin

import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.evaluation.UEvaluationContext
import org.jetbrains.uast.visitor.UastVisitor

class ValueLogger(val evaluationContext: UEvaluationContext) : UastVisitor {

    val builder = StringBuilder()

    var level = 0

    override fun visitElement(node: UElement): Boolean {
        val initialLine = node.asLogString() + " [" + run {
            val renderString = node.asRenderString().lines()
            if (renderString.size == 1) {
                renderString.single()
            } else {
                renderString.first() + "..." + renderString.last()
            }
        } + "]"

        (1..level).forEach { builder.append("    ") }
        builder.append(initialLine)
        if (node is UExpression) {
            val value = evaluationContext.valueOf(node)
            builder.append(" = ").append(value)
        }
        builder.appendLine()
        level++
        return false
    }

    override fun afterVisitElement(node: UElement) {
        level--
    }

    override fun toString() = builder.toString()
}
