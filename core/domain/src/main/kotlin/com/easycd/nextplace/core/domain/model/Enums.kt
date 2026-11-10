package com.easycd.nextplace.core.domain.model

/** 기획서 7장 Place.status. 방문 여부는 Visit 개수로 따로 센다. */
enum class PlaceStatus { UNCONFIRMED, CONFIRMED }

/** 기획서 7장 Place.source. TOUCH = S-17 위치만 저장. */
enum class PlaceSource { VOICE, TOUCH, SHARED, GLEO }

/** POI = 가게 좌표 확정, ESTIMATED = 말한 시점 추정 지점, NONE = 좌표 없음. */
enum class LocationQuality { POI, ESTIMATED, NONE }

/** VisitSession 상태 기계 (기술서 7-4). */
enum class VisitState { ROUTING, ARRIVED, STAYING, VISITED, LEFT }

/** 가게가 진행 방향의 어느 쪽에 있는가. */
enum class Side { LEFT, RIGHT, UNKNOWN }
