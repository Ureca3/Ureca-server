package com.ureca.unity.domain.stt.service;

import com.ureca.unity.domain.stt.model.SttJob;

public interface SttService {

    SttJob startStt(Long counselingId);

    SttJob getStt(Long counselingId);
}
