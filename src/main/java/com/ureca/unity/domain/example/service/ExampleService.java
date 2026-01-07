package com.ureca.unity.domain.example.service;

import com.ureca.unity.domain.example.dto.response.ExampleResponse;
import com.ureca.unity.domain.example.mapper.ExampleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExampleService {

    private final ExampleMapper exampleMapper;

    public List<ExampleResponse> getExamples() {
        return exampleMapper.findAll()
                .stream()
                .map(e -> new ExampleResponse(e.getId(), e.getName()))
                .toList();
    }
}
