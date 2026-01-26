package com.ureca.unity.domain.stt.mapper;

import com.ureca.unity.domain.stt.model.CounselingResult;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CounselingResultMapper {

    void insert(CounselingResult counselingResult);

    void updateResult(CounselingResult counselingResult);

    CounselingResult findByCounselingId(Long counselingResultId);

    CounselingResult findById(Long counselingResultId);
}
