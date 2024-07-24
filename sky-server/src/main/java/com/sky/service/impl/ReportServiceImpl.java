package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);

        List<Double> turnoverList = new ArrayList<>();

        for (LocalDate date : dateList) {
            //获取一天的开始00:00:00和一天的结束23:59:59
            LocalDateTime start = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end1 = LocalDateTime.of(date, LocalTime.MAX);


            Map<String, Object> map = new HashMap<String, Object>();
            map.put("begin", start);
            map.put("end", end1);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.getSumByConditions(map);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);

        }
        String dateStr = StringUtils.join(dateList, ",");
        String turnoverStr = StringUtils.join(turnoverList, ",");
        return TurnoverReportVO.builder().dateList(dateStr).turnoverList(turnoverStr).build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);

        List<Long> userCountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
            Long count = userMapper.getSumByCreateTime(dayEnd);
            userCountList.add(count);
        }

        //TODO :optimize it to only one loop
        List<Long> newUserCountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);
            Long count = userMapper.getSumByTime(dayStart, dayEnd);
            newUserCountList.add(count);
        }

        String dateStr = StringUtils.join(dateList, ",");

        String userCountStr = StringUtils.join(userCountList, ",");
        String newUserCountStr = StringUtils.join(newUserCountList, ",");
        //获取每天的用户总数
        return UserReportVO.builder().dateList(dateStr).totalUserList(userCountStr).newUserList(newUserCountStr).build();

    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = getDateList(begin, end);


        List<Integer> completeCount = new ArrayList<>();
        List<Integer> allCount = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime dayStart = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime dayEnd = LocalDateTime.of(date, LocalTime.MAX);

            Map<String, Object> condition = new HashMap<>();

            condition.put("dayStart", dayStart);
            condition.put("dayEnd", dayEnd);
            condition.put("status", Orders.COMPLETED);
            //先获取每天已经完成的订单数
            completeCount.add(orderMapper.getOrderCountByConditions(condition));
            condition.remove("status");
            //获取每天的总订单数
            allCount.add(orderMapper.getOrderCountByConditions(condition));
        }

        Integer compCount = completeCount.stream().reduce(0, (a, b) -> a + b);
        Integer all = allCount.stream().reduce(0, Integer::sum);
        double rate = 0.0;
        if (all != 0) {
            rate = compCount * 1.0 / all;
        }
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(allCount, ","))
                .validOrderCountList(StringUtils.join(completeCount, ","))
                .totalOrderCount(all)
                .validOrderCount(compCount)
                .orderCompletionRate(rate)
                .build();
    }

    @Override
    public SalesTop10ReportVO getSaleTop10(LocalDate begin, LocalDate end) {

        LocalDateTime timeStart = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime timeEnd = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> saleList = orderMapper.getSaleTop10(timeStart, timeEnd);
        List<String> nameList = new ArrayList<>();
        List<Integer> countList = new ArrayList<>();

        saleList.forEach(e -> {
            countList.add(e.getNumber());
            nameList.add(e.getName());
        });
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(countList, ","))
                .build();
    }

    @Override
    public void getExportData(HttpServletResponse response) {
        //1.查询数据近30天到昨天的
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);


        //2.通过POI将数据写入excel
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");

        try {

            XSSFWorkbook excel = new XSSFWorkbook(inputStream);

            XSSFSheet sheet = excel.getSheetAt(0);
            //设置表格里面的时间
            sheet.getRow(1).getCell(1).setCellValue("时间:" + dateBegin + "至" + dateEnd);

            //3.通过输出流把文件下载到浏览器

            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public List<LocalDate> getDateList(LocalDate begin, LocalDate end) {
        //构造dateList字符串
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            //获取从begin到end的日期
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }
}
