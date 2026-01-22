package com.ureca.unity.domain.stt.mapper;

import com.ureca.unity.domain.stt.model.SttJob;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SttMapper {

    void insert(SttJob sttJob);

    void updateResult(SttJob sttJob);

    SttJob findByCounselingId(Long counselingId);

    SttJob findById(Long sttJobId);
}
