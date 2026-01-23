package com.ureca.unity.domain.auth.service;

import com.ureca.unity.domain.auth.dto.MeResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthSessionService {
    MeResponse getMe(HttpServletRequest request);
}
