package com.example.shopgiayonepoly.controller;

import com.example.shopgiayonepoly.baseMethod.BaseEmail;
import com.example.shopgiayonepoly.dto.request.StaffRequest;
import com.example.shopgiayonepoly.dto.response.StaffResponse;
import com.example.shopgiayonepoly.entites.Customer;
import com.example.shopgiayonepoly.entites.Role;
import com.example.shopgiayonepoly.entites.Staff;
import com.example.shopgiayonepoly.repositores.StaffRepository;
import com.example.shopgiayonepoly.repositores.RoleReponsitory;
import com.example.shopgiayonepoly.service.CashierInventoryService;
import com.example.shopgiayonepoly.service.CustomerService;
import com.example.shopgiayonepoly.service.StaffService;
import com.example.shopgiayonepoly.service.TimekeepingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/staff-manage")
public class StaffController extends BaseEmail {
    @Autowired
    StaffService staffService;

    @Autowired
    StaffRepository staffRepository;

    @Autowired
    CustomerService customerService;

    @Autowired
    RoleReponsitory roleReponsitory;

    String mess = "";
    String check = "";

    private final int pageSize = 5;

    @Autowired
    TimekeepingService timekeepingService;

    @Autowired
    CashierInventoryService cashierInventoryService;

//    @GetMapping("/list")
//    public String list(Model model) {
//        List<StaffResponse> listStaff = staffService.getAllStaff();
//        model.addAttribute("staffList", staffService.getAllStaff());
//        return "Staff/list";
//    }

    @GetMapping("/list")
    public String getListStaffByPage(@RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber, Model model,HttpSession session) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Staff staff = (Staff) session.getAttribute("staffLogin");
        if(staff == null || staff.getId() == null) {
            return "redirect:/login";
        }
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return "redirect:/login";
        }
        if(staffLogin.getStatus() != 1) {
            return "redirect:/home_manage";
        }
        Page<StaffResponse> pageStaff = staffService.getAllStaffByPage(pageable, staff.getId());
        model.addAttribute("pageStaff", pageStaff);
        model.addAttribute("staff", new StaffRequest());
        model.addAttribute("message",mess);
        model.addAttribute("check",check);
        mess = "";
        check = "";
        return "Staff/list";
    }

//    @GetMapping("/search")
//    public String searchStaffByKey(@RequestParam(name = "key") String key, @RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber, Model model) {
//        String trimmedKey = key != null ? key.trim() : null;
//        Pageable pageableSearch = PageRequest.of(pageNumber, pageSize);
//        Page<StaffResponse> pageStaff = staffService.searchStaffByKeywordPage(trimmedKey, pageableSearch);
////        model.addAttribute("staffList", searchStaff);
//        model.addAttribute("pageStaff", pageStaff);
//        model.addAttribute("staff", new StaffRequest());
//        return "Staff/list";
//    }

    @GetMapping("/create")
    public String createStaff(ModelMap modelMap) {
        modelMap.addAttribute("staff", new StaffRequest());
        return "Staff/create";
    }

    @PostMapping("/add")
    public String addStaff(Model model, @Valid @ModelAttribute(name = "staff") StaffRequest staffRequest, BindingResult result, HttpSession session) throws IOException {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return "redirect:/login";
        }
        if(staffLogin.getStatus() != 1) {
            return "redirect:/home_manage";
        }
        Map<String,String> checkLoginAndLogout = checkLoginAndLogOutByStaff(staffLogin.getId());
        String messMap = checkLoginAndLogout.get("message");
        if(!messMap.trim().equals("")) {
            mess = messMap;
            check = "3";
            return "redirect:/staff-manage/list";
        }
        // Kiểm tra tên
        if (staffRequest.getFullName() == null || staffRequest.getFullName().trim().isEmpty()) {
            result.rejectValue("fullName", "error.staff", "Tên không được để trống!"); // Thông báo nếu tên rỗng hoặc chỉ chứa khoảng trắng
        } else if (staffRequest.getFullName().length() > 255) {
            result.rejectValue("fullName", "error.staff", "Tên không được vượt quá 255 ký tự!");
        } else if (!staffRequest.getFullName().matches("^[\\p{L} ]+$")) {
            result.rejectValue("fullName", "error.staff", "Tên chỉ được chứa ký tự chữ cái và dấu cách!");
        }
// Kiểm tra mã
        if (staffRequest.getCodeStaff() == null || staffRequest.getCodeStaff().trim().isEmpty()) {
            result.rejectValue("codeStaff", "error.staff", "Mã không được để trống!"); // Thông báo nếu mã rỗng hoặc chỉ chứa khoảng trắng
        } else if (staffRequest.getCodeStaff().length() < 3 || staffRequest.getCodeStaff().length() > 10) {
            result.rejectValue("codeStaff", "error.staff", "Mã phải có độ dài từ 3 đến 10 ký tự!");
        } else if (!staffRequest.getCodeStaff().matches("^[a-zA-Z0-9]*$")) {
            result.rejectValue("codeStaff", "error.staff", "Mã không được chứa ký tự đặc biệt!"); // Thông báo nếu chứa ký tự đặc biệt
        } else if (staffService.existsByCodeStaff(staffRequest.getCodeStaff())) {
            result.rejectValue("codeStaff", "error.staff", "Mã đã tồn tại!"); // Thông báo nếu mã đã tồn tại
        }
// Kiểm tra số điện thoại
        if (staffRequest.getNumberPhone() == null || staffRequest.getNumberPhone().isEmpty()) {
            result.rejectValue("numberPhone", "error.staff", "Số điện thoại không được để trống!");
        } else if (!staffRequest.getNumberPhone().matches("^(0[3|5|7|8|9])+([0-9]{8})$")) {
            result.rejectValue("numberPhone", "error.staff", "Số điện thoại không hợp lệ!");
        }

        //Kiem tra role
        if (staffRequest.getRole() == null) {
            result.rejectValue("role", "error.staff", "Chức vụ không được để trống!");
        }
        if (staffRequest.getRole().getId() == null) {
            result.rejectValue("role", "error.staff", "Chức vụ không được để trống!");
        }

//        // Kiểm tra email
//        if (staffRequest.getEmail() == null || staffRequest.getEmail().isEmpty()) {
//            result.rejectValue("email", "error.customer", "Email không được để trống!");
//        }
        // Kiểm tra ngày sinh
        if (staffRequest.getBirthDay() == null) {
            result.rejectValue("birthDay", "error.customer", "Ngày sinh không được để trống!");
        } else if (staffRequest.getBirthDay().isAfter(LocalDate.now())) {
            result.rejectValue("birthDay", "error.customer", "Ngày sinh không được lớn hơn ngày hiện tại!");
        } else if (Period.between(staffRequest.getBirthDay(), LocalDate.now()).getYears() < 18) {
            result.rejectValue("birthDay", "error.customer", "Nhân viên phải đủ 18 tuổi trở lên!");
        }
        // Kiểm tra email
//        if (staffRequest.getEmail() == null || staffRequest.getEmail().isEmpty()) {
//            result.rejectValue("email", "error.staff", "Email không được để trống!");
//        } else if (customerService.existsByEmail(staffRequest.getEmail()) != null || staffService.existsByEmail(staffRequest.getEmail()) != null) {
//            result.rejectValue("email", "error.staff", "Email đã được sử dụng!");
//        }
        if (staffRequest.getEmail() == null || staffRequest.getEmail().isEmpty()) {
            result.rejectValue("email", "error.staff", "Email không được để trống!");
        } else if (staffRequest.getEmail().length() > 100) {
            result.rejectValue("email", "error.staff", "Email không được vượt quá 100 ký tự!");
        } else if (!staffRequest.getEmail().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            result.rejectValue("email", "error.staff", "Email không hợp lệ!");
        } else if (customerService.existsByEmail(staffRequest.getEmail()) != null || staffService.existsByEmail(staffRequest.getEmail()) != null) {
            result.rejectValue("email", "error.staff", "Email đã được sử dụng!");
        }
        // Kiểm tra địa chỉ
        if (staffRequest.getAddRessDetail() == null || staffRequest.getAddRessDetail().trim().isEmpty()) {
            result.rejectValue("addRessDetail", "error.staff", "Địa chỉ cụ thể không được để trống!");
        } else if (staffRequest.getAddRessDetail().length() > 260) {
            result.rejectValue("addRessDetail", "error.staff", "Địa chỉ cụ thể không được vượt quá 260 ký tự!");
        }
        if (result.hasErrors()) {
            model.addAttribute("mes", "Thêm thất bại");
            // Nếu có lỗi, trả về trang form để người dùng sửa lại
            return "Staff/create"; // Bạn có thể trả về tên view của form nhập liệu
        }
        System.out.println("Du lieu khi them cua staff: " + staffRequest.toString());
        Staff staff = new Staff();
        staff.setCodeStaff(staffRequest.getCodeStaff());
        staff.setFullName(staffRequest.getFullName());
        staff.setAddress(staffRequest.getWard() + "," + staffRequest.getDistrict() + "," + staffRequest.getProvince() + "," + staffRequest.getAddRessDetail());
        staff.setNumberPhone(staffRequest.getNumberPhone());
        staff.setBirthDay(staffRequest.getBirthDay());
        staff.setEmail(staffRequest.getEmail());
        staff.setAcount("");
        staff.setPassword("");
        staff.setGender(staffRequest.getGender());
        staff.setRole(staffRequest.getRole());
        staff.setStatus(staffRequest.getStatus());
        Staff staffSave = this.staffService.save(staff);
        staffSave.setAcount(staffSave.getCodeStaff() + staffSave.getId());
        staff.setPassword("@shoponepoly");
        // Kiểm tra ảnh
        if (staffRequest.getNameImage() != null && !staffRequest.getNameImage().isEmpty()) {
            staff.setImage("fileName");
            staffService.uploadFile(staffRequest.getNameImage(), staffSave.getId());
        } else {
            // Đặt ảnh mặc định nếu không có ảnh được tải lên
            staff.setImage("Ảnh nhân viên");
        }
//        staff.setImage("fileName");
        System.out.println(staff.toString());
        System.out.println("Hello");
        mess = "Thêm nhân viên thành công";
        check = "1";
//        staffService.uploadFile(staffRequest.getNameImage(),staffSave.getId());
        return "redirect:/staff-manage/list";
    }

    @ModelAttribute("staffInfo")
    public Staff staff(HttpSession session) {
        Staff staff = (Staff) session.getAttribute("staffLogin");
        return staff;
    }

    @GetMapping("/edit/{id}")
    public String editStaff(Model model, @PathVariable("id") Integer id, HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return "redirect:/login";
        }
        if(staffLogin.getStatus() != 1) {
            return "redirect:/home_manage";
        }
        Staff staff = staffService.getOne(id);
        StaffRequest staffRequest = new StaffRequest();
        staffRequest.setId(staff.getId());
        staffRequest.setCodeStaff(staff.getCodeStaff());
        staffRequest.setFullName(staff.getFullName());
        String[] part = staff.getAddress().split(",\\s*");
        staffRequest.setProvince(part[2]);
        staffRequest.setDistrict(part[1]);
        staffRequest.setWard(part[0]);
        staffRequest.setAddRessDetail(String.join(", ", java.util.Arrays.copyOfRange(part, 3, part.length)));
        staffRequest.setGender(staff.getGender());
        staffRequest.setBirthDay(staff.getBirthDay());
        staffRequest.setNumberPhone(staff.getNumberPhone());
        staffRequest.setEmail(staff.getEmail());
        staffRequest.setRole(staff.getRole());
        staffRequest.setStatus(staff.getStatus());
        staffRequest.setImageString(staff.getImage());
        System.out.println(staffRequest.toString());
        model.addAttribute("staff", staffRequest);
        return "Staff/update";
    }

    @PostMapping("/update")
    public String updateStaff(Model model, @Valid @ModelAttribute(name = "staff") StaffRequest staffRequest, BindingResult result, HttpSession session) throws IOException {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return "redirect:/login";
        }
        if(staffLogin.getStatus() != 1) {
            return "redirect:/home_manage";
        }
        Map<String,String> checkLoginAndLogout = checkLoginAndLogOutByStaff(staffLogin.getId());
        String messMap = checkLoginAndLogout.get("message");
        if(!messMap.trim().equals("")) {
            mess = messMap;
            check = "3";
            return "redirect:/staff-manage/list";
        }
//        // Kiểm tra tên
//        if (staffRequest.getFullName() == null || staffRequest.getFullName().trim().isEmpty()) {
//            result.rejectValue("fullName", "error.staff", "Tên không được để trống!"); // Thông báo nếu tên rỗng hoặc chỉ chứa khoảng trắng
//        } else if (staffRequest.getFullName().length() < 2 || staffRequest.getFullName().length() > 50) {
//            result.rejectValue("fullName", "error.staff", "Tên phải có độ dài từ 2 đến 50 ký tự!");
//        } else if (!staffRequest.getFullName().matches("^[\\p{L} ]+$")) {
//            result.rejectValue("fullName", "error.staff", "Tên chỉ được chứa ký tự chữ cái và dấu cách!");
//        }
//// Kiểm tra số điện thoại
//        if (staffRequest.getNumberPhone() == null || staffRequest.getNumberPhone().isEmpty()) {
//            result.rejectValue("numberPhone", "error.staff", "Số điện thoại không được để trống!");
//        } else if (!staffRequest.getNumberPhone().matches("^(0[3|5|7|8|9])+([0-9]{8})$")) {
//            result.rejectValue("numberPhone", "error.staff", "Số điện thoại không hợp lệ!");
//        }
//        // Kiểm tra email
//        if (staffRequest.getEmail() == null || staffRequest.getEmail().isEmpty()) {
//            result.rejectValue("email", "error.customer", "Email không được để trống!");
//        } else {
//            Customer existingCustomer = customerService.existsByEmail(staffRequest.getEmail());
//            Staff existingStaff = staffService.existsByEmail(staffRequest.getEmail());
//            System.out.println(existingCustomer == null ? "Dell co(khach)" : "co(khach)");
//            System.out.println(existingStaff == null ? "Dell co(nhanvien)" : "co(nhanvien)");
//            if (existingStaff != null && !existingStaff.getId().equals(staffRequest.getId())) {
//                result.rejectValue("email", "error.customer", "Email đã được sử dụng!");
//            } else if (customerService.existsByEmail(staffRequest.getEmail()) != null) {
//                result.rejectValue("email", "error.customer", "Email đã được sử dụng trong hệ thống khach hang!");
//            }
//        }
//        // Kiểm tra ngày sinh
//        if (staffRequest.getBirthDay() == null) {
//            result.rejectValue("birthDay", "error.customer", "Ngày sinh không được để trống!");
//        } else if (staffRequest.getBirthDay().isAfter(LocalDate.now())) {
//            result.rejectValue("birthDay", "error.customer", "Ngày sinh không được lớn hơn ngày hiện tại!");
//        }
        // Kiểm tra tên
        if (staffRequest.getFullName() == null || staffRequest.getFullName().trim().isEmpty()) {
            result.rejectValue("fullName", "error.staff", "Tên không được để trống!"); // Thông báo nếu tên rỗng hoặc chỉ chứa khoảng trắng
        } else if (staffRequest.getFullName().length() > 255) {
            result.rejectValue("fullName", "error.staff", "Tên không được vượt quá 255 ký tự!");
        } else if (!staffRequest.getFullName().matches("^[\\p{L} ]+$")) {
            result.rejectValue("fullName", "error.staff", "Tên chỉ được chứa ký tự chữ cái và dấu cách!");
        }
// Kiểm tra số điện thoại
        if (staffRequest.getNumberPhone() == null || staffRequest.getNumberPhone().isEmpty()) {
            result.rejectValue("numberPhone", "error.staff", "Số điện thoại không được để trống!");
        } else if (!staffRequest.getNumberPhone().matches("^(0[3|5|7|8|9])+([0-9]{8})$")) {
            result.rejectValue("numberPhone", "error.staff", "Số điện thoại không hợp lệ!");
        }

        //Kiem tra role
        if (staffRequest.getRole() == null) {
            result.rejectValue("role", "error.staff", "Chức vụ không được để trống!");
        }
        if (staffRequest.getRole().getId() == null) {
            result.rejectValue("role", "error.staff", "Chức vụ không được để trống!");
        }

//        // Kiểm tra email
//        if (staffRequest.getEmail() == null || staffRequest.getEmail().isEmpty()) {
//            result.rejectValue("email", "error.customer", "Email không được để trống!");
//        }
        // Kiểm tra ngày sinh
        if (staffRequest.getBirthDay() == null) {
            result.rejectValue("birthDay", "error.customer", "Ngày sinh không được để trống!");
        } else if (staffRequest.getBirthDay().isAfter(LocalDate.now())) {
            result.rejectValue("birthDay", "error.customer", "Ngày sinh không được lớn hơn ngày hiện tại!");
        } else if (Period.between(staffRequest.getBirthDay(), LocalDate.now()).getYears() < 18) {
            result.rejectValue("birthDay", "error.customer", "Nhân viên phải đủ 18 tuổi trở lên!");
        }
        // Kiểm tra email
        if (staffRequest.getEmail() == null || staffRequest.getEmail().isEmpty()) {
            result.rejectValue("email", "error.customer", "Email không được để trống!");
        } else if (staffRequest.getEmail().length() > 100) {
            result.rejectValue("email", "error.customer", "Email không được vượt quá 100 ký tự!");
        } else if (!staffRequest.getEmail().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            result.rejectValue("email", "error.customer", "Email không hợp lệ!");
        } else {
            Customer existingCustomer = customerService.existsByEmail(staffRequest.getEmail());
            Staff existingStaff = staffService.existsByEmail(staffRequest.getEmail());
            System.out.println(existingCustomer == null ? "Dell co(khach)" : "co(khach)");
            System.out.println(existingStaff == null ? "Dell co(nhanvien)" : "co(nhanvien)");
            if (existingStaff != null && !existingStaff.getId().equals(staffRequest.getId())) {
                result.rejectValue("email", "error.customer", "Email đã được sử dụng!");
            } else if (customerService.existsByEmail(staffRequest.getEmail()) != null) {
                result.rejectValue("email", "error.customer", "Email đã được sử dụng trong hệ thống khach hang!");
            }
        }
        // Kiểm tra địa chỉ
        if (staffRequest.getAddRessDetail() == null || staffRequest.getAddRessDetail().trim().isEmpty()) {
            result.rejectValue("addRessDetail", "error.staff", "Địa chỉ cụ thể không được để trống!");
        } else if (staffRequest.getAddRessDetail().length() > 260) {
            result.rejectValue("addRessDetail", "error.staff", "Địa chỉ cụ thể không được vượt quá 260 ký tự!");
        }
        if (result.hasErrors()) {
            model.addAttribute("mes", "Thêm thất bại");
            // Nếu có lỗi, trả về trang form để người dùng sửa lại
            return "Staff/update"; // Bạn có thể trả về tên view của form nhập liệu
        }
        Staff staff = staffService.getStaffByID(staffRequest.getId());
        staffRequest.setCreateDate(staff.getCreateDate());
        staff.setCodeStaff(staffRequest.getCodeStaff());
        staff.setFullName(staffRequest.getFullName());
        staff.setAddress(staffRequest.getWard() + "," + staffRequest.getDistrict() + "," + staffRequest.getProvince() + "," + staffRequest.getAddRessDetail());
        staff.setNumberPhone(staffRequest.getNumberPhone());
        staff.setBirthDay(staffRequest.getBirthDay());
        staff.setEmail(staffRequest.getEmail());
        staff.setGender(staffRequest.getGender());
        staff.setRole(staffRequest.getRole());
        staff.setStatus(staffRequest.getStatus());
        this.staffService.save(staff);
        // Kiểm tra ảnh
        if (staffRequest.getNameImage() != null && !staffRequest.getNameImage().isEmpty()) {
            staff.setImage("fileName");
            staffService.uploadFile(staffRequest.getNameImage(), staff.getId());
        } else {
            // Đặt ảnh mặc định nếu không có ảnh được tải lên
            staff.setImage("Ảnh nhân viên");
        }
        mess = "Sửa nhân viên thành công";
        check = "1";
        return "redirect:/staff-manage/list";
    }

    @GetMapping("/detail/{id}")
    public String detailStaff(Model model, @PathVariable("id") Integer id, HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return "redirect:/login";
        }
        if(staffLogin.getStatus() != 1) {
            return "redirect:/home_manage";
        }
        Staff staff = staffService.getOne(id);
        StaffRequest staffRequest = new StaffRequest();
        staffRequest.setId(staff.getId());
        staffRequest.setCodeStaff(staff.getCodeStaff());
        staffRequest.setFullName(staff.getFullName());
        String[] part = staff.getAddress().split(",\\s*");
        staffRequest.setProvince(part[2]);
        staffRequest.setDistrict(part[1]);
        staffRequest.setWard(part[0]);
        staffRequest.setAddRessDetail(String.join(", ", java.util.Arrays.copyOfRange(part, 3, part.length)));
        staffRequest.setGender(staff.getGender());
        staffRequest.setBirthDay(staff.getBirthDay());
        staffRequest.setNumberPhone(staff.getNumberPhone());
        staffRequest.setEmail(staff.getEmail());
        staffRequest.setRole(staff.getRole());
        staffRequest.setStatus(staff.getStatus());
        staffRequest.setImageString(staff.getImage());
        System.out.println(staffRequest.toString());
        model.addAttribute("staff", staffRequest);
        return "Staff/detail";
    }

    @GetMapping("/delete/{id}")
    public String deleteStaff(RedirectAttributes ra, @PathVariable("id") Integer id, HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return "redirect:/login";
        }
        if(staffLogin.getStatus() != 1) {
            return "redirect:/home_manage";
        }
        Map<String,String> checkLoginAndLogout = checkLoginAndLogOutByStaff(staffLogin.getId());
        String messMap = checkLoginAndLogout.get("message");
        if(!messMap.trim().equals("")) {
            mess = messMap;
            check = "3";
            return "redirect:/staff-manage/list";
        }
        staffService.deleteStaff(id);
//        ra.addFlashAttribute("mes", "Xóa thành công nhan vien với ID là: " + id);
        mess = "Xoa nhan vien thanh cong";
        check = "1";
        return "redirect:/staff-manage/list";
    }

    @GetMapping("/exchange-pass-word/{id}")
    public String getExchangePassWord(@PathVariable("id") String id) {
        Staff staff = this.staffService.getStaffByID(Integer.parseInt(id));
        setUpToken(Integer.parseInt(id),"staff",staff.getEmail());
        return "redirect:/staff-manage/list";
    }

    @ModelAttribute("listRole")
    public List<Role> getListRole(){
        for (Role role: roleReponsitory.getListRole()) {
            System.out.println("RoleL " + role.getCodeRole());
        }
        return roleReponsitory.getListRole();
    }

    protected String getCheckStaffAttendanceYetBill(
//            @PathVariable("id") Integer idStaff,@PathVariable("type") Integer timekeepingTypeCheck
            Integer idStaff, Integer timekeepingTypeCheck
    ) {
        List<Object[]> checkLoginLogOut = this.timekeepingService.getCheckStaffAttendanceYet(idStaff, timekeepingTypeCheck);

        // Kiểm tra nếu danh sách không rỗng và có kết quả
        if (!checkLoginLogOut.isEmpty() && checkLoginLogOut.get(0).length > 0) {
            // Lấy giá trị đầu tiên từ kết quả
            return checkLoginLogOut.get(0)[0].toString();
        }
        // Trường hợp không có dữ liệu
        return "Không";
    }

    protected Map<String,String> checkLoginAndLogOutByStaff(Integer idStaff) {
        Map<String,String> thongBao = new HashMap<>();
        String checkLogin = getCheckStaffAttendanceYetBill(idStaff,1);
        String checkLogOut = getCheckStaffAttendanceYetBill(idStaff,2);
        System.out.println(checkLogin);
        if(!checkLogin.equals("Có")) {
            thongBao.put("message","Mời bạn điểm danh trước khi làm việc!");
            return thongBao;
        }

        if(checkLogin.equals("Có") && checkLogOut.equals("Có")) {
            thongBao.put("message","Bạn đã điểm danh vào và ra rồi, không thể làm việc được nữa!");
            return thongBao;
        }
        thongBao.put("message","");
        return thongBao;
    }

    protected String getCheckStaffCashierInventory(
            Integer idStaff
    ) {
        List<Object[]> checkCashierInventory = this.cashierInventoryService.getCheckCashierInventoryStaff(idStaff);

        // Kiểm tra nếu danh sách không rỗng và có kết quả
        if (!checkCashierInventory.isEmpty() && checkCashierInventory.get(0).length > 0) {
            // Lấy giá trị đầu tiên từ kết quả
            return checkCashierInventory.get(0)[0].toString();
        }
        // Trường hợp không có dữ liệu
        return "Không";
    }

}
