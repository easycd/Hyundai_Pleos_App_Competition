package com.easycd.nextplace.core.domain.voice

/**
 * 업종 사전 (기획서 6장 슬롯 '업종', 12장 리스크: 유사 발음·별칭을 넣는다).
 * [keywords]는 발화에서 찾는 말, [poiKeywords]는 POI 분류 이름에서 찾는 말, [kakaoGroup]은 카카오 분류 코드.
 */
data class Category(
    val name: String,
    val keywords: List<String>,
    val poiKeywords: List<String>,
    val kakaoGroup: String = FOOD,
) {
    companion object {
        const val FOOD = "FD6"
        const val CAFE = "CE7"
    }
}

object Categories {
    val all: List<Category> = listOf(
        // 삼계탕·백숙은 카카오 분류가 '육류,고기 > 닭요리 > 삼계탕'이라 고깃집·치킨보다 먼저 둔다
        Category("삼계탕", listOf("삼계탕", "백숙", "닭백숙", "닭한마리", "닭볶음탕"), listOf("삼계탕", "백숙", "닭요리")),
        Category("고깃집", listOf("고깃집", "고기집", "고기", "갈비", "삼겹살", "정육식당", "숯불", "곱창", "막창"), listOf("육류", "고기", "갈비", "삼겹살", "곱창")),
        Category("카페", listOf("카페", "커피", "까페", "커피숍", "디저트"), listOf("카페", "커피", "디저트"), Category.CAFE),
        Category("빵집", listOf("빵집", "베이커리", "빵"), listOf("제과", "베이커리", "빵")),
        Category("파스타", listOf("파스타", "이탈리안", "스파게티"), listOf("이탈리안", "양식", "파스타")),
        Category("국밥", listOf("국밥", "순대국", "순댓국", "해장국", "설렁탕", "곰탕"), listOf("국밥", "해장국", "설렁탕", "곰탕", "순대")),
        Category("치킨", listOf("치킨", "통닭", "닭강정"), listOf("치킨", "닭")),
        Category("피자", listOf("피자"), listOf("피자")),
        Category("중국집", listOf("중국집", "중식", "짜장", "짬뽕", "마라탕"), listOf("중식", "중국")),
        Category("일식", listOf("일식", "초밥", "스시", "라멘", "돈카츠", "돈까스", "우동"), listOf("일식", "초밥", "돈까스", "라멘")),
        Category("분식", listOf("분식", "떡볶이", "김밥"), listOf("분식", "떡볶이", "김밥")),
        Category("햄버거", listOf("햄버거", "버거"), listOf("햄버거", "패스트푸드")),
        Category("냉면", listOf("냉면", "막국수"), listOf("냉면", "국수")),
        Category("칼국수", listOf("칼국수", "국수", "수제비"), listOf("칼국수", "국수")),
        Category("횟집", listOf("횟집", "회", "해물", "해산물", "조개"), listOf("회", "해물", "해산물")),
        Category("술집", listOf("술집", "호프", "포차", "이자카야", "맥주"), listOf("술집", "호프", "주점", "요리주점")),
        Category("한식", listOf("한식", "한정식", "백반", "밥집", "식당"), listOf("한식")),
    )

    private val byKeyword: List<Pair<String, Category>> =
        all.flatMap { c -> c.keywords.map { it to c } }.sortedByDescending { it.first.length }

    /** 발화에서 업종을 찾는다. 긴 말부터 본다 ('고깃집'이 '고기'보다 먼저). '회'처럼 한 글자 말은 단어 단위로만. */
    fun find(text: String): Category? {
        val tokens = text.split(' ').filter { it.isNotBlank() }
        return byKeyword.firstOrNull { (kw, _) ->
            if (kw.length == 1) tokens.any { it == kw || it.startsWith(kw + "집") } else text.contains(kw)
        }?.second
    }

    fun byName(name: String?): Category? = all.firstOrNull { it.name == name }

    /**
     * 저장·확인 화면에 쓸 업종 이름. 카카오 분류("음식점 > 한식 > 육류,고기 > 닭요리 > 삼계탕")를 가장 구체적인 칸부터 보고
     * 처음 맞는 업종을 쓴다 (같은 칸에 여럿 맞으면 사용자가 말한 업종 우선). 아무것도 안 맞으면 말한 업종, 그것도 없으면 마지막 칸.
     */
    fun label(poiCategory: String?, said: String? = null): String? {
        val parts = poiCategory?.split('>')?.map { it.trim() }?.filter { it.isNotEmpty() }.orEmpty()
        for (part in parts.asReversed()) {
            val hits = all.filter { matches(it, part) }
            if (hits.isNotEmpty()) return (hits.firstOrNull { it.name == said } ?: hits.first()).name
        }
        return said ?: parts.lastOrNull()
    }

    /** POI 분류 문자열이 업종과 맞는가. */
    fun matches(category: Category, poiCategory: String?): Boolean {
        if (poiCategory.isNullOrBlank()) return false
        return category.poiKeywords.any { poiCategory.contains(it) } || poiCategory.contains(category.name)
    }
}
