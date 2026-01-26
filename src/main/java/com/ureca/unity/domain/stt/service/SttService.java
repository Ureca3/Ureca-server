package com.ureca.unity.domain.stt.service;

import com.ureca.unity.domain.stt.model.CounselingResult;

import java.io.File;

public interface SttService {

    CounselingResult startStt(File filePath, long userId);

    CounselingResult getStt(Long counselingId);
}
