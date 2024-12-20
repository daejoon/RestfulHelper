package com.github.nayacco.restfulhelper.annotations.extraction

import com.intellij.psi.PsiArrayInitializerMemberValue
import com.github.nayacco.restfulhelper.annotations.extraction.PsiExpressionExtractor.extractExpression
import com.github.nayacco.restfulhelper.utils.unquote

class PsiArrayInitializerMemberValueExtractor : PsiAnnotationValueExtractor<PsiArrayInitializerMemberValue> {

    override fun extract(value: PsiArrayInitializerMemberValue): List<String> = value.initializers.map {
        val element = extractExpression(it)
        when {
            element.isNotBlank() -> element
            it.text.isNotBlank() -> it.text.unquote()
            else -> ""
        }
    }
}
