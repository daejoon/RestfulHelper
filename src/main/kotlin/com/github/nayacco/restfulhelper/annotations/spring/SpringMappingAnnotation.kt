package com.github.nayacco.restfulhelper.annotations.spring

import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiAnnotationMemberValue
import com.intellij.psi.PsiLiteralExpression
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReferenceExpression
import com.github.nayacco.restfulhelper.RequestMappingItem
import com.github.nayacco.restfulhelper.annotations.MappingAnnotation
import com.github.nayacco.restfulhelper.annotations.PathAnnotation
import com.github.nayacco.restfulhelper.annotations.UrlFormatter
import com.github.nayacco.restfulhelper.annotations.extraction.PsiExpressionExtractor
import com.github.nayacco.restfulhelper.model.Path
import com.github.nayacco.restfulhelper.model.PathParameter
import com.github.nayacco.restfulhelper.utils.fetchAnnotatedMethod

abstract class SpringMappingAnnotation(
    val psiAnnotation: PsiAnnotation,
    private val urlFormatter: UrlFormatter = SpringUrlFormatter
) : MappingAnnotation {

    override fun values(): List<RequestMappingItem> =
        fetchRequestMappingItem(psiAnnotation, psiAnnotation.fetchAnnotatedMethod(), extractMethod())

    abstract fun extractMethod(): String

    private fun fetchRequestMappingItem(annotation: PsiAnnotation, psiMethod: PsiMethod, methodName: String): List<RequestMappingItem> {
        val classMappings = fetchMappingsFromClass(psiMethod)
        val methodMappings = fetchMappingsFromMethod(annotation, psiMethod)
        val paramsMappings = fetchMappingsParams(annotation)
        return classMappings.map { clazz ->
            methodMappings.map { method ->
                paramsMappings.map { param ->
                    RequestMappingItem(psiMethod, urlFormatter.format(clazz, method, param), methodName)
                }
            }.flatten()
        }.flatten()
    }

    private fun fetchMappingsParams(annotation: PsiAnnotation): List<String> {
        val fetchMappingsFromAnnotation = PathAnnotation(annotation).fetchMappings(PARAMS)
        return fetchMappingsFromAnnotation.ifEmpty { listOf("") }
    }

    private fun fetchMappingsFromClass(psiMethod: PsiMethod): List<String> {
        val classMapping = psiMethod
            .containingClass
            ?.modifierList
            ?.annotations
            ?.filterNotNull()
            ?.filter { it.qualifiedName == SPRING_REQUEST_MAPPING_CLASS }
            ?.flatMap { fetchMapping(it) } ?: emptyList()
        return classMapping.ifEmpty { listOf("") }
    }

    private fun fetchMapping(annotation: PsiAnnotation): List<String> {
        val pathMapping = PathAnnotation(annotation).fetchMappings(PATH)
        return pathMapping.ifEmpty {
            val valueMapping = PathAnnotation(annotation).fetchMappings(VALUE)
            valueMapping.ifEmpty { listOf("") }
        }
    }

    private fun fetchMappingsFromMethod(annotation: PsiAnnotation, method: PsiMethod): List<String> {
        val parametersNameWithType = method
            .parameterList
            .parameters
            .mapNotNull { PathParameter(it).extractParameterNameWithType(SPRING_PATH_VARIABLE_CLASS, ::extractParameterNameFromAnnotation) }
            .toMap()

        return fetchMapping(annotation)
            .map { Path(it).addPathVariablesTypes(parametersNameWithType).toFullPath() }
    }

    private fun extractParameterNameFromAnnotation(annotation: PsiAnnotation, defaultValue: String): String {
        val pathVariableValue = annotation.findAttributeValue(VALUE)
        val pathVariableName = annotation.findAttributeValue(NAME)

        val valueAttribute = extractPsiAnnotation(pathVariableValue, defaultValue)
        return if (valueAttribute != defaultValue) valueAttribute else extractPsiAnnotation(pathVariableName, defaultValue)
    }

    private fun extractPsiAnnotation(psiAnnotationMemberValue: PsiAnnotationMemberValue?, defaultValue: String): String {
        return when (psiAnnotationMemberValue) {
            is PsiLiteralExpression -> {
                val expression = PsiExpressionExtractor.extractExpression(psiAnnotationMemberValue)
                expression.ifBlank { defaultValue }
            }
            is PsiReferenceExpression -> {
                val expression = PsiExpressionExtractor.extractExpression(psiAnnotationMemberValue)
                expression.ifBlank { defaultValue }
            }
            else -> defaultValue
        }
    }

    companion object {
        private const val VALUE = "value"
        private const val PATH = "path"
        private const val NAME = "name"
        private const val PARAMS = "params"
        private const val SPRING_REQUEST_MAPPING_CLASS = "org.springframework.web.bind.annotation.RequestMapping"
        private const val SPRING_PATH_VARIABLE_CLASS = "org.springframework.web.bind.annotation.PathVariable"
    }
}
