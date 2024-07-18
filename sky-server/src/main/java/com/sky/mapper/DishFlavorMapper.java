package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    /**
     * 批量插入口味数据
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

//    @Delete("delete from dish_flavor where dish_id = #{id}")
    void deleteByDishId(List<Long> dishIds);

    List<DishFlavor> getByDishId(Long dishId);
}