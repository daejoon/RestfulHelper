package com.github.nayacco.restfulhelper.contributor

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.search.GlobalSearchScope.projectScope
import org.jetbrains.kotlin.asJava.toLightAnnotation
import org.jetbrains.kotlin.idea.stubindex.KotlinAnnotationsIndex

class KotlinRequestMappingContributor : com.github.nayacco.restfulhelper.contributor.RequestMappingByNameContributor() {

    override fun getAnnotationSearchers(annotationName: String, project: Project): Sequence<PsiAnnotation> {
        return KotlinAnnotationsIndex[annotationName, project, projectScope(project)]
            .asSequence()
            .mapNotNull { it.toLightAnnotation() }
    }
}
