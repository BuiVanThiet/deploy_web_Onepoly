package com.example.shopgiayonepoly.controller;

import com.example.shopgiayonepoly.baseMethod.BaseSaleProduct;
import com.example.shopgiayonepoly.dto.request.DiscountApplyRequest;
import com.example.shopgiayonepoly.dto.request.SaleProductRequest;
import com.example.shopgiayonepoly.entites.ProductDetail;
import com.example.shopgiayonepoly.entites.SaleProduct;
import com.example.shopgiayonepoly.entites.Staff;
import com.example.shopgiayonepoly.entites.Voucher;
import com.example.shopgiayonepoly.service.ProductDetailService;
import com.example.shopgiayonepoly.service.ProductService;
import com.example.shopgiayonepoly.service.SaleProductService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/sale-product")
public class SaleProductController extends BaseSaleProduct {
    @Autowired
    private SaleProductService saleProductService;
    @Autowired
    private ProductDetailService productDetailService;

    private static final int pageSize = 5;
    String mess = "";
    String check = "";

    @GetMapping("/list")
    public String getFormListSaleProduct(@RequestParam(name = "pageNumber", defaultValue = "0") int pageNumber,
                                         @RequestParam(name = "pageNumberDelete", defaultValue = "0") int pageNumberDelete,
                                         Model model, HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return "redirect:/login";
        }
        if (staffLogin.getStatus() != 1) {
            return "redirect:/home_manage";
        }

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<SaleProduct> pageSale = saleProductService.getAllSaleProductByPage(pageable);
        Pageable pageableDelete = PageRequest.of(pageNumberDelete, pageSize);
        Page<SaleProduct> pageSaleDelete = saleProductService.getDeletedSaleProductsByPage(pageableDelete);
        List<ProductDetail> listProductDetail = saleProductService.getAllProductDetailByPage();
        List<ProductDetail> listProductDetailWithDiscount = saleProductService.getAllProductDetailWithDiscount();
        model.addAttribute("pageSale", pageSale);
        model.addAttribute("pageSaleDelete", pageSaleDelete);
        model.addAttribute("listProductDetail", listProductDetail);
        model.addAttribute("listProductDetailWithDiscount", listProductDetailWithDiscount);
        model.addAttribute("message", mess);
        model.addAttribute("check", check);
        mess = "";
        check = "";
        return "sale_product/index";
    }

    @GetMapping("/create")
    public String getFormCreateSale(Model model, HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return "redirect:/login";
        }
        if (staffLogin.getStatus() != 1) {
            return "redirect:/home_manage";
        }

        SaleProductRequest saleProduct = new SaleProductRequest();
        model.addAttribute("saleProduct", saleProduct);
        return "sale_product/create";
    }

    @PostMapping("/create")
    public String createNewSale(RedirectAttributes redirectAttributes, Model model,
                                @Valid @ModelAttribute("saleProduct") SaleProductRequest saleProductRequest,
                                BindingResult result) {
        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal niceTeen = new BigDecimal("91");
        BigDecimal tenHundred = new BigDecimal("10000");
        BigDecimal oneMillion = new BigDecimal("1000000");
        LocalDate dateNow = LocalDate.now();
        if (saleProductRequest.getDiscountValue() == null) {
            result.rejectValue("discountValue", "error.saleProduct", "Giá trị giảm không được để trống!");
        } else {
            if (saleProductRequest.getDiscountType() == 1) {
                if (saleProductRequest.getDiscountValue().compareTo(zero) <= 0 || saleProductRequest.getDiscountValue().compareTo(niceTeen) >= 0) {
                    result.rejectValue("discountValue", "error.saleProduct", "Giá trị giảm phải lớn hơn 0% và nhỏ hơn 90%!");
                }
            } else {
                if (saleProductRequest.getDiscountValue().compareTo(tenHundred) < 0 || saleProductRequest.getDiscountValue().compareTo(oneMillion) > 0) {
                    result.rejectValue("discountValue", "error.saleProduct", "Giá trị giảm phải lớn hơn 10.000₫ và nhỏ hơn 1.000.000₫!");
                }
            }
        }
        // Kiểm tra startDate
        if (saleProductRequest.getStartDate() == null) {
            result.rejectValue("startDate", "error.saleProduct", "Ngày bắt đầu không được để trống!");
        } else if (saleProductRequest.getStartDate().isBefore(dateNow)) {
            result.rejectValue("startDate", "error.saleProduct", "Ngày bắt đầu phải là ngày hiện tại hoặc sau ngày hiện tại: " + dateNow);
        }

        // Kiểm tra endDate
        if (saleProductRequest.getEndDate() == null) {
            result.rejectValue("endDate", "error.saleProduct", "Ngày kết thúc không được để trống!");
        } else {
            if (saleProductRequest.getEndDate().isBefore(dateNow)) {
                result.rejectValue("endDate", "error.voucher", "Ngày kết thúc phải lớn hơn ngày hiện tại: " + dateNow);
            } else if (saleProductRequest.getEndDate().isBefore(saleProductRequest.getStartDate())) {
                result.rejectValue("endDate", "error.saleProduct", "Ngày kết thúc phải lớn hơn ngày bắt đầu");
            }
        }
        if (result.hasErrors()) {
            model.addAttribute("mes", "Thêm thất bại");
            return "sale_product/create";
        }
        saleProductService.createNewSale(saleProductRequest);
        redirectAttributes.addFlashAttribute("mes", " Thêm mới đợt giảm giá thành công");
        return "redirect:/sale-product/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteSaleProduct(@PathVariable("id") String id, RedirectAttributes redirectAttributes, HttpSession session) {

        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return "redirect:/login";
        }
        if (staffLogin.getStatus() != 1) {
            return "redirect:/home_manage";
        }

        try {
            Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return "redirect:/404";
        }

        Map<String, String> checkLoginAndLogout = checkLoginAndLogOutByStaff(staffLogin.getId());
        String messMap = checkLoginAndLogout.get("message");
        if (!messMap.trim().equals("")) {
            this.mess = messMap;
            this.check = "3";
            return "redirect:/sale-product/list";
        }

        SaleProduct saleProduct = saleProductService.getSaleProductByID(Integer.parseInt(id));
        if (saleProduct == null) {
            mess = "Đợt giảm giá có id: " + id + " không tồn tại!";
            check = "3";
            return "redirect:/sale-product/list";
        }
        if (saleProduct.getId() == null) {
            mess = "Đợt giảm giá có id: " + id + " không tồn tại!";
            check = "3";
            return "redirect:/sale-product/list";
        }


        List<ProductDetail> listProductDetail = saleProductService.findProducDetailByIDDiscout(saleProduct.getId());
        System.out.println(listProductDetail);
        System.out.println("đến đây");
        for (ProductDetail productDetail : listProductDetail) {
            productDetail.setSaleProduct(null);
            productDetailService.save(productDetail);
        }
        saleProductService.deleteSaleProductBySetStatus(Integer.parseInt(id));
        redirectAttributes.addFlashAttribute("mes", "Xóa thành công đợt giảm giá với ID: " + id);
        mess = "Xóa thành công đợt giảm giá với ID: " + id;
        check = "1";
        return "redirect:/sale-product/list";
    }

    @GetMapping("/restore/{id}")
    public String RestoreSaleProduct(RedirectAttributes redirectAttributes, @PathVariable("id") String id, HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return "redirect:/login";
        }
        if (staffLogin.getStatus() != 1) {
            return "redirect:/home_manage";
        }

        try {
            Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return "redirect:/404";
        }

        Map<String, String> checkLoginAndLogout = checkLoginAndLogOutByStaff(staffLogin.getId());
        String messMap = checkLoginAndLogout.get("message");
        if (!messMap.trim().equals("")) {
            this.mess = messMap;
            this.check = "3";
            return "redirect:/sale-product/list";
        }

        SaleProduct saleProduct = saleProductService.getSaleProductByID(Integer.parseInt(id));
        if (saleProduct == null) {
            mess = "Đợt giảm giá có id: " + id + " không tồn tại!";
            check = "3";
            return "redirect:/sale-product/list";
        }
        if (saleProduct.getId() == null) {
            mess = "Đợt giảm giá có id: " + id + " không tồn tại!";
            check = "3";
            return "redirect:/sale-product/list";
        }

        saleProductService.restoreSaleProductStatus(Integer.parseInt(id));
        redirectAttributes.addFlashAttribute("mes", "Khôi phục đợt giảm giá thành công");
        mess = "Khôi phục thành công đợt giảm giá với ID: " + id;
        check = "1";
        return "redirect:/sale-product/list";
    }

    @GetMapping("/edit/{id}")
    public String getFormUpdateSale(Model model, @PathVariable("id") String id, HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return "redirect:/login";
        }
        if (staffLogin.getStatus() != 1) {
            return "redirect:/home_manage";
        }

        try {
            Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return "redirect:/404";
        }

        SaleProduct saleProduct = saleProductService.getSaleProductByID(Integer.parseInt(id));
        if (saleProduct == null) {
            mess = "Đợt giảm giá có id: " + id + " không tồn tại!";
            check = "3";
            return "redirect:/sale-product/list";
        }
        if (saleProduct.getId() == null) {
            mess = "Đợt giảm giá có id: " + id + " không tồn tại!";
            check = "3";
            return "redirect:/sale-product/list";
        }

        SaleProductRequest saleProductRequest = new SaleProductRequest();
        BeanUtils.copyProperties(saleProduct, saleProductRequest);
        System.out.println(saleProductRequest.toString());
        model.addAttribute("saleProductRequest", saleProductRequest);
        model.addAttribute("title", "CẬP NHẬT ĐỢT GIẢM GIÁ VỚI ID: " + id);
        return "sale_product/update";
    }

    @PostMapping("/update")
    public String UpdateSale(Model model,
                             @ModelAttribute("saleProductRequest") SaleProductRequest saleProductRequest,
                             HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return "redirect:/login";
        }
        if (staffLogin.getStatus() != 1) {
            return "redirect:/home_manage";
        }
        Map<String, String> thongBao = this.validateAddAndUpdateSaleProduct(saleProductRequest);

        Map<String, String> checkLoginAndLogout = checkLoginAndLogOutByStaff(staffLogin.getId());
        String messMap = checkLoginAndLogout.get("message");
        if (!messMap.trim().equals("")) {
            this.mess = messMap;
            this.check = "3";
            return "redirect:/sale-product/list";
        }

        if (thongBao.get("check").equals("1")) {
            SaleProduct saleProduct = saleProductService.getSaleProductByID(saleProductRequest.getId());
            if (saleProduct == null) {
                mess = "Đợt giảm giá có id: " + saleProductRequest.getId() + " không tồn tại!";
                check = "3";
                return "redirect:/sale-product/list";
            }
            if (saleProduct.getId() == null) {
                mess = "Đợt giảm giá có id: " + saleProductRequest.getId() + " không tồn tại!";
                check = "3";
                return "redirect:/sale-product/list";
            }
            List<SaleProduct> saleProducts = this.saleProductService.getAllSaleProducts().stream()
                    .filter(saleProduct1 -> saleProduct1.getId() != saleProductRequest.getId())
                    .collect(Collectors.toList());
//            List<SaleProduct> saleProducts = this.saleProductService.getAllSaleProducts();
//            saleProducts.remove(saleProduct);

            for (SaleProduct saleProductCheckSame : saleProducts) {
                if (saleProductCheckSame.getCodeSale().equals(saleProductRequest.getCodeSale())) {
                    mess = "Mã đợt giảm giá đã tồn tại!";
                    check = "3";
                    return "redirect:/sale-product/list";
                }
            }

            saleProductRequest.setCreateDate(saleProduct.getCreateDate());
            saleProductRequest.setUpdateDate(new Date());
            saleProductService.createNewSale(saleProductRequest);
            mess = "Sửa thành công đợt giảm giá với ID: " + saleProductRequest.getId();
            check = "1";
            return "redirect:/sale-product/list";
        } else {
            model.addAttribute("title", "CẬP NHẬT ĐỢT GIẢM GIÁ VỚI ID: " + saleProductRequest.getId());
            model.addAttribute("saleProductRequest", saleProductRequest);
            model.addAttribute("message", thongBao.get("message"));
            model.addAttribute("check", "3");
            return "/sale_product/update";
        }
    }

    @GetMapping("/search-type")
    public String searchSalesByType(
            Model model,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageNumberDelete", defaultValue = "0") Integer pageNumberDelete) {

        Pageable pageableSearch = PageRequest.of(pageNumber, pageSize);
        Page<SaleProduct> pageSale;

        if (type == null || type.isEmpty()) {
            pageSale = saleProductService.getAllSaleProductByPage(pageableSearch);
        } else {
            int typeInt = Integer.parseInt(type);
            pageSale = saleProductService.searchSaleProductsByType(typeInt, pageableSearch);
        }
        Pageable pageableDelete = PageRequest.of(pageNumberDelete, pageSize);
        Page<SaleProduct> pageSaleDelete = saleProductService.getDeletedSaleProductsByPage(pageableDelete);
        List<ProductDetail> listProductDetail = saleProductService.getAllProductDetailByPage();

        model.addAttribute("pageSale", pageSale);
        model.addAttribute("pageSaleDelete", pageSaleDelete);
        model.addAttribute("listProductDetail", listProductDetail);
        model.addAttribute("type", type);
        return "sale_product/index";
    }

    @GetMapping("/search-key")
    public String SearchSaleByKey(Model model,
                                  @RequestParam("key") String key,
                                  @RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
                                  @RequestParam(name = "pageNumberDelete", defaultValue = "0") Integer pageNumberDelete) {
        Pageable pageableSearch = PageRequest.of(pageNumber, pageSize);
        Page<SaleProduct> pageSale = saleProductService.searchSaleProductsByKeyword(key, pageableSearch);
        Pageable pageableDelete = PageRequest.of(pageNumberDelete, pageSize);
        Page<SaleProduct> pageSaleDelete = saleProductService.getDeletedSaleProductsByPage(pageableDelete);
        List<ProductDetail> listProductDetail = saleProductService.getAllProductDetailByPage();

        model.addAttribute("pageSale", pageSale);
        model.addAttribute("pageSaleDelete", pageSaleDelete);
        model.addAttribute("listProductDetail", listProductDetail);
        return "sale_product/index";
    }

    @PostMapping("/apply-discount")
    public ResponseEntity<?> applyDiscount(@RequestBody DiscountApplyRequest discountApplyRequest) {
        try {
            saleProductService.applyDiscountToProductDetails(
                    discountApplyRequest.getProductIds(),
                    discountApplyRequest.getSaleProductId()
            );
            return ResponseEntity.ok("Giảm giá đã được áp dụng thành công!");
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // Xử lý các ngoại lệ khác
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi khi áp dụng giảm giá.");
        }
    }

    @PostMapping("/cancel-discount")
    public ResponseEntity<?> cancelDiscount(@RequestBody List<Integer> productIds) {
        try {
            saleProductService.restoreOriginalPrice(productIds);
            return ResponseEntity.ok("Giảm giá đã được ngừng và giá gốc đã được khôi phục!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Đã xảy ra lỗi khi ngừng giảm giá.");
        }
    }

    @ModelAttribute("staffInfo")
    public Staff staff(HttpSession session) {
        Staff staff = (Staff) session.getAttribute("staffLogin");
        return staff;
    }

    @GetMapping("/extend/{id}")
    public String extendSaleproductExpired(@PathVariable("id") String id, RedirectAttributes redirectAttributes, HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return "redirect:/login";
        }
        if (staffLogin.getStatus() != 1) {
            return "redirect:/home_manage";
        }

        try {
            Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return "redirect:/404";
        }

        Map<String, String> checkLoginAndLogout = checkLoginAndLogOutByStaff(staffLogin.getId());
        String messMap = checkLoginAndLogout.get("message");
        if (!messMap.trim().equals("")) {
            this.mess = messMap;
            this.check = "3";
            return "redirect:/sale-product/list";
        }

        SaleProduct saleProduct = saleProductService.getSaleProductByID(Integer.parseInt(id));
        if (saleProduct == null) {
            mess = "Đợt giảm giá có id: " + id + " không tồn tại!";
            check = "3";
            return "redirect:/sale-product/list";
        }
        if (saleProduct.getId() == null) {
            mess = "Đợt giảm giá có id: " + id + " không tồn tại!";
            check = "3";
            return "redirect:/sale-product/list";
        }

        saleProductService.updateSaleProductExpired(Integer.parseInt(id));
        redirectAttributes.addFlashAttribute("mes", "Gia hạn phiếu giảm giá thành công");
        mess = "Gia hạn đợt giảm giá có id: " + id + " thành công!";
        check = "1";
        return "redirect:/sale-product/list";
    }

//    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Ho_Chi_Minh")
//    public void updateExpireSaleProductsStatus() {
//        System.out.println("Running scheduled task to update voucher status");
//        saleProductService.updateSaleProductStatusForExpiredAuto();
//    }
}
