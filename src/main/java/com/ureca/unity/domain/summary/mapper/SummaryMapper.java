package com.ureca.unity.domain.summary.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SummaryMapper {

    void insertSummary(
            @Param("counselingResultId") Long counselingResultId,
            @Param("userId") Long userId
    );

    Long findLatestSummaryId(
            @Param("userId") Long userId,
            @Param("counselingResultId") Long counselingResultId
    );

    void updateSummaryResult(
            @Param("summaryId") Long summaryId,
            @Param("title") String title,
            @Param("subject") String subject,
            @Param("keywords") String keywordJson,
            @Param("points") String pointsJson
    );

    void updateStatus(
            @Param("summaryId") Long summaryId,
            @Param("status") String status
    );

    Boolean findBookmarkStatus(@Param("summaryId") Long summaryId);

    void updateBookmark(
            @Param("summaryId") Long summaryId,
            @Param("isBookmarked") boolean isBookmarked
    );
}
