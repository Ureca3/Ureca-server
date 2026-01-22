package com.ureca.unity.domain.summary.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SummaryMapper {

    void insertSummary(
            @Param("sttJobId") Long sttJobId,
            @Param("counselingId") Long counselingId,
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("subject") String subject,
            @Param("keywords") String keywordsJson,
            @Param("points") String pointsJson
    );

    Boolean findBookmarkStatus(@Param("summaryId")Long summaryId);

    void updateBookmark(
            @Param("summaryId") Long summaryId,
            @Param("bookmarkId") boolean isBookmarked
    );
}
