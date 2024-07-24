package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WebSocketServer webSocketServer;

    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        //处理地址簿为空、购物车为空的异常情况（我这里就不校验了）

        //向订单表插入一条数据
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, order);
        order.setOrderTime(LocalDateTime.now());
        order.setUserId(BaseContext.getCurrentId());
        order.setPayStatus(Orders.UN_PAID);
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPhone(addressBook.getPhone());
        order.setConsignee(addressBook.getConsignee());
        order.setNumber(String.valueOf(System.currentTimeMillis()));//订单号
        order.setAddress(addressBook.getDetail());

        orderMapper.insert(order);


        //向order_detail表插入n条数据
        ShoppingCart shoppingCart = ShoppingCart.builder().id(BaseContext.getCurrentId()).build();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);

        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart s : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(s, orderDetail);
            orderDetail.setOrderId(order.getId());
            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetailList);
        //清空购物车
        shoppingCartMapper.clean(BaseContext.getCurrentId());
        //返回VO
        OrderSubmitVO orderSubmitVO = new OrderSubmitVO();
        orderSubmitVO.setId(order.getId());
        orderSubmitVO.setOrderNumber(order.getNumber());
        orderSubmitVO.setOrderTime(order.getOrderTime());
        orderSubmitVO.setOrderAmount(order.getAmount());

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
//        User user = userMapper.getByOpenid(userId);

//        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
//
//
        JSONObject jsonObject = new JSONObject();
        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));
        paySuccess(ordersPaymentDTO.getOrderNumber());
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        //通过websocket向客户端推送信息
        HashMap map = new HashMap();
        map.put("type", 1); //1:来单提醒 ； 2:用户催单
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号:" + outTradeNo);
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    @Override
    //客户催单
    public void reminder(Long id) {

        Orders order = orderMapper.getById(id);
        HashMap map = new HashMap();
        map.put("type", 1); //1:来单提醒 ； 2:用户催单
        map.put("orderId", id);
        map.put("content", "订单号:" + order.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }


    @Override
    public OrderVO detail(Long id) {
        Orders order = orderMapper.getById(id);
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    @Override
    public PageResult searchCondition(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<OrderVO> page = orderMapper.pageQuery(ordersPageQueryDTO);
        getOrderVOList(page);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public PageResult getOrder4User(OrdersPageQueryDTO ordersPageQueryDTO) {
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());

        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<OrderVO> page = orderMapper.pageQuery(ordersPageQueryDTO);
        List<OrderVO> orderVOList = page.getResult();
        for (OrderVO order : orderVOList) {

            order.setOrderDetailList(orderDetailMapper.getByOrderId(order.getId()));
        }

        return new PageResult(page.getTotal(), orderVOList);
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders order = Orders.builder().id(ordersConfirmDTO.getId()).status(Orders.CONFIRMED).build();
        orderMapper.update(order);
    }

    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders order = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(order);
    }

    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed = orderMapper.countByStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countByStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS);

        OrderStatisticsVO orderStatisticsVO  =new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    public List<OrderVO> getOrderVOList(Page<OrderVO> page) {
        List<OrderVO> pageResult = page.getResult();

        if (pageResult.size() > 0) {
            pageResult.forEach(e -> {
                String orderDishes = getOrderDishes(e.getId());
                e.setOrderDishes(orderDishes);
            });
        }
        return pageResult;
    }

    public String getOrderDishes(Long orderId) {
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orderId);
        List<String> stringList = orderDetails.stream().map(e -> {
            return e.getName() + "*" + e.getNumber() + ";";
        }).collect(Collectors.toList());
        return String.join("", stringList);
    }
}
