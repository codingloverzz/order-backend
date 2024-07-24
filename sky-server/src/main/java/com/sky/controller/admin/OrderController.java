package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.entity.OrderDetail;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/conditionSearch")
    public Result<PageResult> searchCondition(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageResult pageResult = orderService.searchCondition(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
    @PutMapping("confirm")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }
    @PutMapping("/rejection")
    public  Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }
    @PutMapping("/cancel")
    public  Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO){
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }
    @GetMapping("/details/{id}")
    public Result<OrderVO> detail(@PathVariable Long id){
        OrderVO detail = orderService.detail(id);
        return Result.success(detail);
    }
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics(){
        OrderStatisticsVO orderStatisticsVO =  orderService.statistics();
        return Result.success(orderStatisticsVO);
    }
}
