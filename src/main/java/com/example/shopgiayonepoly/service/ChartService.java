package com.example.shopgiayonepoly.service;

import com.example.shopgiayonepoly.dto.request.ProductInfoDto;
import com.example.shopgiayonepoly.dto.request.Statistics;
import com.example.shopgiayonepoly.dto.request.StatusBill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface ChartService {
    Long monthlyBill();

    Long totalMonthlyBill();

    Long totalMonthlyInvoiceProducts();

    Long billOfTheDay();

    Long totalPriceToday();

    List<Date> findLastBillDates();

    List<Statistics> findMonthlyStatistics();

    List<Statistics> findTodayStatistics();

    List<Statistics> findLast7DaysStatistics();

    List<Statistics> getAnnualStatistics();

    List<Statistics> findStatisticsByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

    List<ProductInfoDto> getProductSales();

    List<ProductInfoDto> findTopProductsByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

    List<StatusBill> findBillsWithStatusDescription();

    List<StatusBill> getStatusCountForToday();

    List<StatusBill> findStatusCountsForLast7Days();

    List<StatusBill> countStatusByYear();

    List<StatusBill> getBillStatisticsByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

    Long serviceFee(@Param("startDate") String startDate, @Param("endDate") String endDate);

    Long returnFee(@Param("startDate") String startDate, @Param("endDate") String endDate);

    Long exchangeFee(@Param("startDate") String startDate, @Param("endDate") String endDate);

    List<ProductInfoDto> findTopProductsExchangeByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

    List<ProductInfoDto> findTopProductsReturnByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);

}
