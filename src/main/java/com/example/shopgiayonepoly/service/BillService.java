package com.example.shopgiayonepoly.service;

import com.example.shopgiayonepoly.dto.request.bill.SearchBillByStatusRequest;
import com.example.shopgiayonepoly.dto.response.bill.*;
import com.example.shopgiayonepoly.entites.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface BillService {
    List<Bill> findAll();

    <S extends Bill> S save(S entity);

    Optional<Bill> findById(Integer integer);

    long count();

    void deleteById(Integer integer);

    List<Bill> findAll(Sort sort);

    Page<Bill> findAll(Pageable pageable);

    List<Bill> getBillByStatusNew(Pageable pageable);

    List<Customer> getClientNotStatus0();

    BillTotalInfornationResponse findBillVoucherById(Integer id);
    List<ClientBillInformationResponse> getClientBillInformationResponse(Integer idClient);

    Page<Voucher> getVouCherByBill(Integer idBill,String keyword, Pageable pageable);

    List<Voucher> getVoucherByBill(Integer idBill, String keyword);

//    Page<BillResponseManage> getAllBillByStatusDiss0(String nameCheck, Integer status, Pageable pageable);
//
//    List<BillResponseManage> getAllBillByStatusDiss0(String nameCheck, Integer status);
    Page<BillResponseManage> getAllBillByStatusDiss0(String nameCheck, SearchBillByStatusRequest searchBillByStatusRequest, Date start, Date end, Pageable pageable);

    List<BillResponseManage> getAllBillByStatusDiss0(String nameCheck, SearchBillByStatusRequest searchBillByStatusRequest,Date  start,Date  end);

    InformationBillByIdBillResponse getInformationBillByIdBill(Integer idBill);

    List<Object[]> getInfoPaymentByIdBill(Integer id);
    List<Object[]> getBillByIdCreatePDF(Integer id);
    List<Object[]> getBillDetailByIdBillPDF(Integer id);

    List<Object[]> getInfomationBillReturn(Integer id);

    ProductDetail getProductDteailById(Integer id);

    String getDiscountBill(Integer idBill);

    List<Object[]> getVoucherByBillV2(Integer idBill, String keyword);

    List<Object[]> getInformationPDF_Return_Exchange_Bill(Integer idCheck);

    List<Object[]> getListProductReturn(Integer idCheck);

    List<Object[]> getListProductExchange(Integer idCheck);

    void deleteBillById(Integer idBillCheck);
}
