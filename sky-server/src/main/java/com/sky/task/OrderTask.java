package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ? ")
    /**
     * 处理支付超时的订单
     */
    public void processTimeoutOrder() {
        //获取当前时间减去15分钟
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        log.info("定时处理超时订单");
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, time);

        if (ordersList != null && ordersList.size() > 0) {
            ordersList.forEach(e -> {
                e.setStatus(Orders.CANCELLED);
                e.setCancelReason("订单超时，自动取消");
                e.setCancelTime(LocalDateTime.now());
                orderMapper.update(e);
            });
        }

    }

    @Scheduled(cron = "0 0 1 * * ?")

    /**
     *  每天凌晨一点,处理一直处于派送中的订单
     */
    public void processDeliveryOrder() {
        //凌晨1点-60的话刚好就是获取昨天的订单
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, time);
        if (ordersList != null && ordersList.size() > 0) {
            ordersList.forEach(e -> {
                e.setStatus(Orders.COMPLETED);
                 orderMapper.update(e);
            });
        }
    }
}
