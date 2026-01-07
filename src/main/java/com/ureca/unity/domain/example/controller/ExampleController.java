package com.ureca.unity.domain.example.controller;

import com.ureca.unity.domain.example.dto.response.ExampleResponse;
import com.ureca.unity.domain.example.service.ExampleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExampleController {

    private final ExampleService exampleService;

    @GetMapping("/examples")
    public List<ExampleResponse> getExamples() {
        return exampleService.getExamples();
    }
}
