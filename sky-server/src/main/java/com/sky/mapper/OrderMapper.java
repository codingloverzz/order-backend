package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    void insert(Orders order);

    /**
     * 根据订单号查询订单
     *
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     *
     * @param orders
     */
    void update(Orders orders);

    /**
     * 根据状态和订单时间获取
     *
     * @return
     */
    @Select("select * from orders where status = #{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTime(Integer status, LocalDateTime orderTime);

    @Select("select * from orders where id = #{id};")
    Orders getById(Long id);

    Page<OrderVO> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select sum(amount) from orders where order_time > #{begin} and order_time<#{end} and status = #{status}")
    Double getSumByConditions(Map<String, Object> map);

    Integer getOrderCountByConditions(Map<String, Object> condition);

    List<GoodsSalesDTO> getSaleTop10(LocalDateTime timeStart, LocalDateTime timeEnd);
}
