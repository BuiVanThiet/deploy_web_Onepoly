package com.example.shopgiayonepoly.entites;

import com.example.shopgiayonepoly.entites.baseEntity.Base;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bill")
@ToString
public class Bill extends Base {
    @Column(name = "code_bill")
    private String codeBill;
    @ManyToOne
    @JoinColumn(name = "id_customer")
    private Customer customer;
    @ManyToOne
    @JoinColumn(name = "id_staff")
    private Staff staff;
    @Column(name = "address")
    private String addRess;
    @ManyToOne
    @JoinColumn(name = "id_voucher")
    private Voucher voucher;
    @Column(name = "shipping_price")
    private BigDecimal shippingPrice;
    @Column(name = "cash")
    private BigDecimal cash;
    @Column(name = "acount_money")
    private BigDecimal acountMoney;
    @Column(name = "note")
    private String note;
    @Column(name = "total_amount")
    private BigDecimal totalAmount;
    @Column(name = "payment_method")
    private Integer paymentMethod;
    @Column(name = "bill_type")
    private Integer billType;
    @Column(name = "payment_status")
    private Integer paymentStatus;
    @Column(name = "surplus_money")
    private BigDecimal surplusMoney;
    @Column(name = "price_discount")
    private BigDecimal priceDiscount;
    @Column(name = "transaction_no")
    private String transactionNo;
    @Column(name = "bank_tran_no")
    private String bankTranNo;
}
