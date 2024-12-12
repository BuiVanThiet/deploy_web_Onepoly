package com.example.shopgiayonepoly.controller.attribute;

import com.example.shopgiayonepoly.entites.Manufacturer;
import com.example.shopgiayonepoly.entites.Manufacturer;
import com.example.shopgiayonepoly.entites.Staff;
import com.example.shopgiayonepoly.service.CashierInventoryService;
import com.example.shopgiayonepoly.service.TimekeepingService;
import com.example.shopgiayonepoly.service.attribute.ManufacturerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/attribute")
public class ManufacturerController {

    @Autowired
    ManufacturerService manufacturerService;

    @Autowired
    TimekeepingService timekeepingService;
    @Autowired
    CashierInventoryService cashierInventoryService;

    @RequestMapping("/manufacturer")
    public String list(Model model, HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return "redirect:/login";
        }
        if(staffLogin.getStatus() != 1) {
            return "redirect:/home_manage";
        }
        model.addAttribute("manufacturerList", manufacturerService.getManufacturerNotStatus0());
        model.addAttribute("manufacturerAdd", new Manufacturer());
        return "Attribute/manufacturer";
    }

    @GetMapping("/manufacturer/delete")
    public ResponseEntity<List<Manufacturer>> listManufacturerDelete(HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return null;
        }
        if(staffLogin.getStatus() != 1) {
            return null;
        }
        List<Manufacturer> deletedManufacturers = manufacturerService.getManufacturerDelete();
        return new ResponseEntity<>(deletedManufacturers, HttpStatus.OK);
    }

    @GetMapping("/manufacturer/active")
    public ResponseEntity<List<Manufacturer>> listActive(HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return null;
        }
        if(staffLogin.getStatus() != 1) {
            return null;
        }
        List<Manufacturer> listManufacturerActive = manufacturerService.getManufacturerNotStatus0();
        return new ResponseEntity<>(listManufacturerActive, HttpStatus.OK);
    }

    @GetMapping("/manufacturer/get-code")
    public ResponseEntity<List<String>> findAllCodeManufacturer(HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return null;
        }
        if(staffLogin.getStatus() != 1) {
            return null;
        }
        List<String> codeManufacturer = new ArrayList<>();
        for (Manufacturer listManufacturer : manufacturerService.findAll()) {
            codeManufacturer.add(listManufacturer.getCodeManufacturer());
        }
        return new ResponseEntity<>(codeManufacturer, HttpStatus.OK);
    }

    @GetMapping("/manufacturer/get-name")
    public ResponseEntity<List<String>> findAllNameManufacturer(HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return null;
        }
        if(staffLogin.getStatus() != 1) {
            return null;
        }
        List<String> nameManufacturer = new ArrayList<>();
        for (Manufacturer listManufacturer : manufacturerService.findAll()) {
            nameManufacturer.add(listManufacturer.getNameManufacturer());
        }
        return new ResponseEntity<>(nameManufacturer, HttpStatus.OK);
    }

    @ResponseBody
    @PostMapping("/manufacturer/add")
    public ResponseEntity<Map<String, String>> add(@ModelAttribute Manufacturer manufacturer, HttpSession session) {
        Map<String, String> thongBao = new HashMap<>();
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            thongBao.put("message", "Nhân viên chưa đăng nhập");
            thongBao.put("check", "3");
            return ResponseEntity.ok(thongBao);
        }
        if(staffLogin.getStatus() != 1) {
            thongBao.put("message","Nhân viên đang bị ngừng hoạt động!");
            thongBao.put("check","3");
            return ResponseEntity.ok(thongBao);
        }
        Map<String,String> checkLoginAndLogout = checkLoginAndLogOutByStaff(staffLogin.getId());
        String messMap = checkLoginAndLogout.get("message");
        if(!messMap.trim().equals("")) {
            thongBao.put("message",messMap);
            thongBao.put("check","3");
            return ResponseEntity.ok(thongBao);
        }
        boolean checkCode = true;
        boolean checkName = true;
        for (Manufacturer listManufacturer : manufacturerService.findAll()) {
            if (manufacturer.getCodeManufacturer().trim().equalsIgnoreCase(listManufacturer.getCodeManufacturer().trim())) {
                checkCode = false;
                break;
            }
        }
        for (Manufacturer listManufacturer : manufacturerService.findAll()) {
            if (manufacturer.getNameManufacturer().trim().equalsIgnoreCase(listManufacturer.getNameManufacturer().trim())) {
                checkName = false;
                break;
            }
        }
        if (!checkCode) {
            thongBao.put("message", "Mã nhà sản xuất đã tồn tại");
            thongBao.put("check", "2");
        } else if (!checkName) {
            thongBao.put("message", "Tên nhà sản xuất đã tồn tại");
            thongBao.put("check", "2");
        } else if (manufacturer.getCodeManufacturer().isEmpty()) {
            thongBao.put("message", "Mã nhà sản xuất không được để trống");
            thongBao.put("check", "2");
        } else if (manufacturer.getNameManufacturer().isEmpty()) {
            thongBao.put("message", "Tên nhà sản xuất không được để trống");
            thongBao.put("check", "2");
        } else if (manufacturer.getCodeManufacturer().length() > 10) {
            thongBao.put("message", "Mã nhà sản xuất không được dài quá 10 ký tự");
            thongBao.put("check", "2");
        } else if (manufacturer.getNameManufacturer().length() > 50) {
            thongBao.put("message", "Tên nhà sản xuất không được dài quá 50 ký tự");
            thongBao.put("check", "2");
        } else {
            manufacturer.setStatus(1);
            manufacturerService.save(manufacturer);
            thongBao.put("message", "Thêm nhà sản xuất thành công");
            thongBao.put("check", "1");
        }

        return ResponseEntity.ok(thongBao);
    }

    @PostMapping("/update-manufacturer")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateManufacturer(@RequestBody Map<String, Object> payload, HttpSession session) {
        Map<String, String> thongBao = new HashMap<>();
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            thongBao.put("message", "Nhân viên chưa đăng nhập");
            thongBao.put("check", "3");
            return null;
        }
        if(staffLogin.getStatus() != 1) {
            thongBao.put("message","Nhân viên đang bị ngừng hoạt động!");
            thongBao.put("check","3");
            return ResponseEntity.ok(thongBao);
        }
        Map<String,String> checkLoginAndLogout = checkLoginAndLogOutByStaff(staffLogin.getId());
        String messMap = checkLoginAndLogout.get("message");
        if(!messMap.trim().equals("")) {
            thongBao.put("message",messMap);
            thongBao.put("check","3");
            return ResponseEntity.ok(thongBao);
        }
        try {
            int id;
            String codeManufacturer;
            String nameManufacturer;

            // Lấy giá trị từ payload
            if (payload.get("id") instanceof Integer) {
                id = (Integer) payload.get("id");
            } else {
                id = Integer.parseInt((String) payload.get("id"));
            }

            codeManufacturer = (String) payload.get("codeManufacturer");
            nameManufacturer = (String) payload.get("nameManufacturer");
            boolean checkCode = true;
            for (Manufacturer listManufacturer : manufacturerService.findAll()) {
                if (codeManufacturer.trim().equalsIgnoreCase(listManufacturer.getCodeManufacturer().trim()) && id != listManufacturer.getId()) {
                    checkCode = false;
                    break;
                }
            }
            boolean checkName = true;
            for (Manufacturer listManufacturer : manufacturerService.findAll()) {
                if (nameManufacturer.trim().equalsIgnoreCase(listManufacturer.getNameManufacturer().trim()) && id != listManufacturer.getId()) {
                    checkName = false;
                    break;
                }
            }
            // Gọi service để cập nhật dữ liệu nhà sản xuất trong cơ sở dữ liệu
            if (!checkCode) {
                thongBao.put("message", "Mã nhà sản xuất đã tồn tại");
                thongBao.put("check", "2");
            } else if (!checkName) {
                thongBao.put("message", "Tên nhà sản xuất đã tồn tại");
                thongBao.put("check", "2");
            } else if (codeManufacturer.isEmpty()) {
                thongBao.put("message", "Mã nhà sản xuất không được để trống");
                thongBao.put("check", "2");
            } else if (nameManufacturer.isEmpty()) {
                thongBao.put("message", "Tên nhà sản xuất không được để trống");
                thongBao.put("check", "2");
            } else if (codeManufacturer.length() > 10) {
                thongBao.put("message", "Mã nhà sản xuất không được dài quá 10 ký tự");
                thongBao.put("check", "2");
            } else if (nameManufacturer.length() > 50) {
                thongBao.put("message", "Tên nhà sản xuất không được dài quá 50 ký tự");
                thongBao.put("check", "2");
            } else {
                manufacturerService.updateManufacturer(id, codeManufacturer, nameManufacturer);
                thongBao.put("message", "Sửa nhà sản xuất thành công");
                thongBao.put("check", "1");
            }
            return ResponseEntity.ok(thongBao);
        } catch (Exception e) {
            thongBao.put("message", "Sửa nhà sản xuất thất bại");
            thongBao.put("check", "2");
            return ResponseEntity.ok(thongBao);
        }
    }
    @PostMapping("/delete-manufacturer")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteManufacturer(@RequestBody Map<String, Object> payload, HttpSession session) {
        Map<String, String> thongBao = new HashMap<>();
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            thongBao.put("message", "Nhân viên chưa đăng nhập");
            thongBao.put("check", "3");
            return ResponseEntity.ok(thongBao);
        }
        if(staffLogin.getStatus() != 1) {
            thongBao.put("message","Nhân viên đang bị ngừng hoạt động!");
            thongBao.put("check","3");
            return ResponseEntity.ok(thongBao);
        }
        Map<String,String> checkLoginAndLogout = checkLoginAndLogOutByStaff(staffLogin.getId());
        String messMap = checkLoginAndLogout.get("message");
        if(!messMap.trim().equals("")) {
            thongBao.put("message",messMap);
            thongBao.put("check","3");
            return ResponseEntity.ok(thongBao);
        }
        try {
            int id;
            int status;

            // Lấy id và status từ payload, kiểm tra kiểu dữ liệu
            if (payload.get("id") instanceof Integer) {
                id = (Integer) payload.get("id");
            } else {
                id = Integer.parseInt(payload.get("id").toString());
            }

            if (payload.get("status") instanceof Integer) {
                status = (Integer) payload.get("status");
            } else {
                status = Integer.parseInt(payload.get("status").toString());
            }
            if (status == 0) {
                thongBao.put("message", "Xóa nhà sản xuất thành công");
                thongBao.put("check", "1");
            } else {
                thongBao.put("message", "Khôi phục nhà sản xuất thành công");
                thongBao.put("check", "1");
            }
            // Gọi service để cập nhật trạng thái trong cơ sở dữ liệu
            manufacturerService.updateStatus(id, status);
            return ResponseEntity.ok(thongBao);

        } catch (NumberFormatException e) {
            // Xử lý lỗi parse dữ liệu
            thongBao.put("message", "Lỗi khi xử lý dữ liệu");
            thongBao.put("check", "3");
            return ResponseEntity.ok(thongBao);
        } catch (Exception e) {
            // Xử lý lỗi khác
            thongBao.put("message", "Lỗi khi xử lý dữ liệu");
            thongBao.put("check", "3");
            return ResponseEntity.ok(thongBao);
        }
    }
    @ModelAttribute("staffInfo")
    public Staff staff(HttpSession session){
        Staff staff = (Staff) session.getAttribute("staffLogin");
        return staff;
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

}
