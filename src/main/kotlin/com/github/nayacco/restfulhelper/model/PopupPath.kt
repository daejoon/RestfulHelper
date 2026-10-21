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

    companion object {
        private val comparator: Comparator<PopupPath> = compareBy<PopupPath, String>(String.CASE_INSENSITIVE_ORDER) { it.path }
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
