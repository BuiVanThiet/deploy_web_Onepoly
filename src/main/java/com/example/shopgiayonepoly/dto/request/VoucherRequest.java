package com.example.shopgiayonepoly.dto.request;

import com.example.shopgiayonepoly.dto.base.BaseDTO;
import jakarta.validation.constraints.*;

import lombok.*;
import org.hibernate.validator.constraints.Range;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class VoucherRequest extends BaseDTO {
//    @NotBlank(message = "Mã phiếu giảm giá không được để trống!")
    private String codeVoucher;
//    @NotBlank(message = "Tên phiếu giảm giá không được để trống!")
    private String nameVoucher;
//    @NotNull(message = "Hãy chọn loại phiếu giảm giá!")
    private Integer discountType;
    private BigDecimal priceReduced;
//    @NotNull(message = "Giảm tối thiểu không được giảm giá!")
    private BigDecimal pricesApply;
//    @NotNull(message = "Giá trị giảm tối đa không được để trống!")
    private BigDecimal pricesMax;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
//    @NotBlank(message = "Mô tả không được để trống!")
    private String describe;
//    @NotNull(message = "Số lượng không được để trống!")
//    @Range(min = 1, max = 100, message = "Số lượng phải từ 1 đến 100!")
    private Integer quantity;
}
