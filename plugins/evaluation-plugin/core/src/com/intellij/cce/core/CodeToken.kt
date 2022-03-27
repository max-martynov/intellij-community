package com.intellij.cce.core

import com.intellij.psi.PsiElement

class CodeToken(val text: String,
                val offset: Int,
                val length: Int,
                val properties: TokenProperties = TokenProperties.UNKNOWN
)