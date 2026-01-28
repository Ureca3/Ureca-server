package com.ureca.unity.domain.category.mapper;

import com.ureca.unity.domain.category.dto.Category;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper {
  List<Category> selectAll();
}
