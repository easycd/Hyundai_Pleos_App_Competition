package com.easycd.nextplace.core.domain.contract

import com.easycd.nextplace.core.domain.model.LocationSnapshot

/**
 * 기능끼리 같은 흐름을 쓸 때의 계약 (기술서 3장). feature는 서로 모르고, 구현은 그 feature가 하고 연결은 app이 Hilt로 한다.
 */
interface SaveRunner {
    /** 말하기 버튼 (S-02a 시작). */
    fun startVoiceSave()
    /** S-17 '위치만 저장': 1 Capture → 8 Persist(미확인)만 돈다. */
    suspend fun saveLocationOnly()
}

interface RecommendRunner {
    /** 추천 세션을 열고 S-06으로 간다. [snapshot]은 버튼을 누른 순간의 '말한 곳'. */
    fun open(category: String?, snapshot: LocationSnapshot)
}

/** 방문 메모를 홈 음성 "여기 괜찮았어"로 남긴다 (S-15b). 최근 3시간 안 방문이 없으면 false. */
interface VisitNoteRunner {
    suspend fun attachNoteToRecentVisit(memo: String): String?
}

/**
 * '저장한 가게 모두 삭제'(S-11b) 때 DB 밖에 남는 것(예약 작업 · 메모리 캐시 등)을 정리하는 고리.
 * 기능 모듈이 Hilt @IntoSet 으로 하나씩 등록하고, 설정 화면이 모두 부른다. 새 기능이 상태를 가지면 여기 하나 추가하면 된다.
 */
interface DataResetHook {
    suspend fun onAllDataDeleted()
}
