package com.github.nayacco.restfulhelper.model

/**
 * 검색 팝업 항목 이름(`RequestMappingItem.getName`)을 정렬용으로 해석한다.
 *
 * format: METHOD PATH PARAMS(if presents)
 * example: GET /account/activate params=some
 */
class PopupPath(popupItem: String) {
    private val method = popupItem.substringBefore(' ')

    // params까지 포함해 비교해야 "/a"가 "/a params=x"보다 앞에 온다.
    private val path = popupItem.substringAfter(' ')

    // 목록에 없는 Method는 관례 순서 뒤에 오도록 목록 크기를 순위로 쓴다.
    private val methodRank = METHOD_ORDER.indexOf(method).let { if (it < 0) METHOD_ORDER.size else it }

    companion object {
        private val METHOD_ORDER = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS")

        private val comparator: Comparator<PopupPath> = compareBy<PopupPath, String>(String.CASE_INSENSITIVE_ORDER) { it.path }
            .thenBy { it.path }
            .thenBy { it.methodRank }
            .thenBy { it.method }

        /**
         * 검색 결과 이름을 URL 경로명 → HTTP Method 순으로 정렬한다.
         */
        fun sortNames(names: List<String>, pattern: String): List<String> =
            names.map { it to PopupPath(it) }
                .sortedWith(compareBy(comparator) { it.second })
                .map { it.first }
    }
}
