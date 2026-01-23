package com.ureca.unity.domain.stt.service;

import com.ureca.unity.domain.stt.model.SttJob;

import java.io.File;

public interface SttService {

    SttJob startStt(File filePath);

    SttJob getStt(Long counselingId);
}
