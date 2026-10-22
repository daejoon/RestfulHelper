package com.github.nayacco.restfulhelper.model

import com.github.nayacco.restfulhelper.utils.collapseRepeatedCurlyBrackets
import com.intellij.psi.codeStyle.MinusculeMatcher
import com.intellij.psi.codeStyle.NameUtil

/**
 * 검색 팝업 항목 이름(`RequestMappingItem.getName`)을 정렬용으로 해석한다.
 *
 * format: METHOD PATH PARAMS(if presents)
 * example: GET /account/activate params=some
 * 다중 Method는 METHOD 자리에 `{GET, POST}`처럼 공백이 섞인 토큰이 온다.
 */
class PopupPath(popupItem: String) {
    private val methodToken = if (popupItem.startsWith('{')) popupItem.substringBefore('}') + "}" else popupItem.substringBefore(' ')

    // 다중 Method는 첫 Method를 기준으로 정렬한다.
    private val method = methodToken.removePrefix("{").removeSuffix("}").substringBefore(',').trim()

    // params까지 포함해 비교해야 "/a"가 "/a params=x"보다 앞에 온다.
    private val path = popupItem.removePrefix(methodToken).trimStart()

    // 목록에 없는 Method는 관례 순서 뒤에 오도록 목록 크기를 순위로 쓴다.
    private val methodRank = METHOD_ORDER.indexOf(method).let { if (it < 0) METHOD_ORDER.size else it }

    /** 검색어에 대한 매칭 단계. 선언 순서가 곧 정렬 순서다. */
    private enum class MatchTier { EXACT, PREFIX, SUBSTRING, FUZZY }

    private fun matchTier(normalizedPattern: String): MatchTier {
        val normalizedPath = normalize(path)
        return when {
            normalizedPath == normalizedPattern -> MatchTier.EXACT
            normalizedPath.startsWith(normalizedPattern) -> MatchTier.PREFIX
            normalizedPath.contains(normalizedPattern) -> MatchTier.SUBSTRING
            else -> MatchTier.FUZZY
        }
    }

    // 점수 계산 실패가 탐색을 멈추지 않도록 최저 점수로 대신한다.
    // 경로는 항상 '/'로 시작하므로 이를 떼어야 경로 맨 앞부터의 일치를 start match로 인정받는다.
    private fun matchingDegree(matcher: MinusculeMatcher): Int = runCatching { matcher.matchingDegree(path.removePrefix("/")) }.getOrDefault(0)

    private class SortKey(val name: String, val popupPath: PopupPath, val tier: MatchTier, val degree: Int)

    companion object {
        private val METHOD_ORDER = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS")

        private val comparator: Comparator<PopupPath> = compareBy<PopupPath, String>(String.CASE_INSENSITIVE_ORDER) { it.path }
            .thenBy { it.path }
            .thenBy { it.methodRank }
            .thenBy { it.method }
            .thenBy { it.methodToken.startsWith('{') }

        private val sortKeyComparator: Comparator<SortKey> = compareBy<SortKey> { it.tier }
            .thenByDescending { it.degree }
            .thenBy(comparator) { it.popupPath }

        // "users"와 "/Users"를 같은 입력으로 보기 위해 대소문자와 선행 '/'를 무시한다.
        private fun normalize(value: String): String = value.lowercase().removePrefix("/")

        /**
         * 검색 결과 이름을 매칭 정확도 → URL 경로명 → HTTP Method 순으로 정렬한다.
         */
        fun sortNames(names: List<String>, pattern: String, checkCanceled: () -> Unit = {}): List<String> {
            // matches()와 같이 {{var}} 입력을 {var}로 맞춘 뒤 판정한다.
            val collapsedPattern = pattern.collapseRepeatedCurlyBrackets()
            val normalizedPattern = normalize(collapsedPattern)
            // 빈 검색어나 "/"는 모든 경로와 일치하므로 정확도 없이 경로명 → Method 순으로만 정렬한다.
            val ignoreAccuracy = normalizedPattern.isEmpty()
            // IntelliJ Go To File처럼 경로 맨 앞부터 일치하면 큰 가산점(start match)을 준다. 경로와 맞춰 검색어의 선행 '/'도 뗀다.
            val matcher = if (ignoreAccuracy) null else NameUtil.buildMatcher("*" + collapsedPattern.removePrefix("/"))
                .withCaseSensitivity(NameUtil.MatchingCaseSensitivity.NONE)
                .preferringStartMatches()
                .build()
            return names.map { name ->
                checkCanceled()
                val popupPath = PopupPath(name)
                if (matcher == null) SortKey(name, popupPath, MatchTier.EXACT, 0)
                else SortKey(name, popupPath, popupPath.matchTier(normalizedPattern), popupPath.matchingDegree(matcher))
            }
                .sortedWith(sortKeyComparator)
                .map { it.name }
        }
    }
}
