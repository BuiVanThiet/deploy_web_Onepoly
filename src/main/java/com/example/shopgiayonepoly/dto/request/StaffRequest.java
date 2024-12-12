package com.example.shopgiayonepoly.dto.request;

import com.example.shopgiayonepoly.dto.base.BaseDTO;
import com.example.shopgiayonepoly.entites.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StaffRequest extends BaseDTO {
    private MultipartFile nameImage;
    private String imageString;
    private String codeStaff;
    private String fullName;
    @NotBlank(message = "Vui lòng chọn thành phố!")
    private String province;
    @NotBlank(message = "Vui lòng chọn huyện!")
    private String district;
    @NotBlank(message = "Vui lòng chọn xã!")
    private String ward;
    private String addRessDetail;
    @NotNull(message = "Vui lòng chọn giới tính!")
    private Integer gender;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate birthDay;
    private String numberPhone;
    private String email;
    private Role role;

    public StaffRequest(Integer id, Date createDate, Date updateDate, @NotNull(message = "Mời bạn chọn trạng thái!") Integer status, @NotBlank(message = "") MultipartFile nameImage, @NotBlank(message = "Mã nhân viên không được để trống!") String codeStaff, @NotBlank(message = "Tên nhân viên không được để trống!") String fullName, @NotBlank(message = "Thanh pho không được để trống") String province, @NotBlank(message = "Huyen không được để trống") String district, @NotBlank(message = "Xa không được để trống") String ward, @NotBlank(message = "Dia chi cu the không được để trống!") String addRessDetail, @NotNull(message = "Giới tính không được để trống") Integer gender, LocalDate birthDay, @NotBlank(message = "Số điện thoại không được để trống!") String numberPhone, @NotNull(message = "Email không được để trống") String email, @NotNull(message = "Tên chức vụ không được để trống") Role role) {
        super(id, createDate, updateDate, status);
        this.nameImage = nameImage;
        this.codeStaff = codeStaff;
        this.fullName = fullName;
        this.province = province;
        this.district = district;
        this.ward = ward;
        this.addRessDetail = addRessDetail;
        this.gender = gender;
        this.birthDay = birthDay;
        this.numberPhone = numberPhone;
        this.email = email;
        this.role = role;
    }
}
