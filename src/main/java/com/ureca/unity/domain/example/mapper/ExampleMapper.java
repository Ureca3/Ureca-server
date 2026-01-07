package com.ureca.unity.domain.example.mapper;

import com.ureca.unity.domain.example.model.Example;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ExampleMapper {

    List<Example> findAll();
}
