package com.sky.controller.admin;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/admin/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/page")
    public Result<PageResult> queryPage(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageResult pageResult = categoryService.queryPage(categoryPageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping
    public Result add(@RequestBody CategoryDTO categoryDTO) {
        categoryService.add(categoryDTO);
        return Result.success();
    }

    @PostMapping("status/{status}")
    public Result changeStatus(@PathVariable Integer status, Long id) {
        categoryService.changeStatus(status, id);
        return Result.success();
    }

    @PutMapping
    public Result updateCategory(@RequestBody CategoryDTO categoryDTO) {
        categoryService.updateCategory(categoryDTO);
        return Result.success();
    }

    @DeleteMapping
    public Result delete(Long id){
        categoryService.delete(id);
        return Result.success();
    }
    /**
     * 根据类型获取分类列表
     */
    @GetMapping("/list")
    public Result<List<Category>> list(Integer type){
       List<Category> list =  categoryService.list(type);
        return Result.success(list);
    }
}
