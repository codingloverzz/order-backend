package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;

public interface CategoryService {

    PageResult queryPage(CategoryPageQueryDTO categoryPageQueryDTO);

    void add(CategoryDTO categoryDTO);

    void changeStatus(Integer status, Long id);

    void updateCategory(CategoryDTO categoryDTO);

    void delete(Long id);

    List<Category> list(Integer type);
}
