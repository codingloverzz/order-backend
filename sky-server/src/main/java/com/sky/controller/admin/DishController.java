package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DistService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {
    @Autowired
    private DistService distService;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品,{}", dishDTO);
        distService.saveWithFlavor(dishDTO);
        //清楚缓存
        cleanCache("dist_" + dishDTO.getCategoryId());
        return Result.success();
    }

    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO) {
        PageResult pageResult = distService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids) {
        distService.deleteBatch(ids);
        //这里简单处理，删除所有dish_开头的缓存
        cleanCache("dist_*");

        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id) {
        DishVO dishVO = distService.getDishWithFlavor(id);
        return Result.success(dishVO);
    }

    @PutMapping
    public Result editDish(@RequestBody DishDTO dishDTO) {
        distService.update(dishDTO);
        //这里简单处理，删除所有dish_开头的缓存
        cleanCache("dist_*");
        return Result.success();
    }

    @PostMapping("/status/{status}")
    public Result changeStatus(@PathVariable Integer status, Long id) {
        DishDTO dishDTO = new DishDTO();
        dishDTO.setId(id);
        dishDTO.setStatus(status);
        distService.update(dishDTO);
        return Result.success();
    }

    @GetMapping("/list")
    public Result getDishByCategoryId(Integer categoryId) {
        List<DishVO> dishVOList = distService.getDishByCategory(categoryId);
        return Result.success(dishVOList);
    }

    private void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
