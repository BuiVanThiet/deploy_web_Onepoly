package com.example.shopgiayonepoly.controller.attribute;

import com.example.shopgiayonepoly.entites.Sole;
import com.example.shopgiayonepoly.entites.Sole;
import com.example.shopgiayonepoly.entites.Staff;
import com.example.shopgiayonepoly.service.CashierInventoryService;
import com.example.shopgiayonepoly.service.TimekeepingService;
import com.example.shopgiayonepoly.service.attribute.SoleService;
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
public class SoleController {

    @Autowired
    SoleService soleService;

    @Autowired
    TimekeepingService timekeepingService;
    @Autowired
    CashierInventoryService cashierInventoryService;

    @RequestMapping("/sole")
    public String list(Model model, HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return "redirect:/login";
        }
        if(staffLogin.getStatus() != 1) {
            return "redirect:/home_manage";
        }
        model.addAttribute("soleList", soleService.getSoleNotStatus0());
        model.addAttribute("soleAdd", new Sole());
        return "Attribute/sole";
    }

    @GetMapping("/sole/delete")
    public ResponseEntity<List<Sole>> listSoleDelete(HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return null;
        }
        if(staffLogin.getStatus() != 1) {
            return null;
        }
        List<Sole> deletedSoles = soleService.getSoleDelete();
        return new ResponseEntity<>(deletedSoles, HttpStatus.OK);
    }

    @GetMapping("/sole/active")
    public ResponseEntity<List<Sole>> listActive(HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return null;
        }
        if(staffLogin.getStatus() != 1) {
            return null;
        }
        List<Sole> listSoleActive = soleService.getSoleNotStatus0();
        return new ResponseEntity<>(listSoleActive, HttpStatus.OK);
    }

    @GetMapping("/sole/get-code")
    public ResponseEntity<List<String>> findAllCodeSole(HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return null;
        }
        if(staffLogin.getStatus() != 1) {
            return null;
        }
        List<String> codeSole = new ArrayList<>();
        for (Sole listSole : soleService.findAll()) {
            codeSole.add(listSole.getCodeSole());
        }
        return new ResponseEntity<>(codeSole, HttpStatus.OK);
    }

    @GetMapping("/sole/get-name")
    public ResponseEntity<List<String>> findAllNameSole(HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return null;
        }
        if(staffLogin.getStatus() != 1) {
            return null;
        }
        List<String> nameSole = new ArrayList<>();
        for (Sole listSole : soleService.findAll()) {
            nameSole.add(listSole.getNameSole());
        }
        return new ResponseEntity<>(nameSole, HttpStatus.OK);
    }

    @ResponseBody
    @PostMapping("/sole/add")
    public ResponseEntity<Map<String, String>> add(@ModelAttribute Sole sole, HttpSession session) {
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
        for (Sole listSole : soleService.findAll()) {
            if (sole.getCodeSole().trim().equalsIgnoreCase(listSole.getCodeSole().trim())) {
                checkCode = false;
                break;
            }
        }
        for (Sole listSole : soleService.findAll()) {
            if (sole.getNameSole().trim().equalsIgnoreCase(listSole.getNameSole().trim())) {
                checkName = false;
                break;
            }
        }
        if (!checkCode) {
            thongBao.put("message", "Mã đế giày đã tồn tại");
            thongBao.put("check", "2");
        } else if (!checkName) {
            thongBao.put("message", "Tên đế giày đã tồn tại");
            thongBao.put("check", "2");
        } else if (sole.getCodeSole().isEmpty()) {
            thongBao.put("message", "Mã đế giày không được để trống");
            thongBao.put("check", "2");
        } else if (sole.getNameSole().isEmpty()) {
            thongBao.put("message", "Tên đế giày không được để trống");
            thongBao.put("check", "2");
        } else if (sole.getCodeSole().length() > 10) {
            thongBao.put("message", "Mã đế giày không được dài quá 10 ký tự");
            thongBao.put("check", "2");
        } else if (sole.getNameSole().length() > 50) {
            thongBao.put("message", "Tên đế giày không được dài quá 50 ký tự");
            thongBao.put("check", "2");
        } else {
            sole.setStatus(1);
            soleService.save(sole);
            thongBao.put("message", "Thêm đế giày thành công");
            thongBao.put("check", "1");
        }

        return ResponseEntity.ok(thongBao);
    }

    @PostMapping("/update-sole")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateSole(@RequestBody Map<String, Object> payload, HttpSession session) {
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
            String codeSole;
            String nameSole;

            // Lấy giá trị từ payload
            if (payload.get("id") instanceof Integer) {
                id = (Integer) payload.get("id");
            } else {
                id = Integer.parseInt((String) payload.get("id"));
            }

            codeSole = (String) payload.get("codeSole");
            nameSole = (String) payload.get("nameSole");
            boolean checkCode = true;
            for (Sole listSole : soleService.findAll()) {
                if (codeSole.trim().equalsIgnoreCase(listSole.getCodeSole().trim()) && id != listSole.getId()) {
                    checkCode = false;
                    break;
                }
            }
            boolean checkName = true;
            for (Sole listSole : soleService.findAll()) {
                if (nameSole.trim().equalsIgnoreCase(listSole.getNameSole().trim()) && id != listSole.getId()) {
                    checkName = false;
                    break;
                }
            }
            // Gọi service để cập nhật dữ liệu đế giày trong cơ sở dữ liệu
            if (!checkCode) {
                thongBao.put("message", "Mã đế giày đã tồn tại");
                thongBao.put("check", "2");
            } else if (!checkName) {
                thongBao.put("message", "Tên đế giày đã tồn tại");
                thongBao.put("check", "2");
            } else if (codeSole.isEmpty()) {
                thongBao.put("message", "Mã đế giày không được để trống");
                thongBao.put("check", "2");
            } else if (nameSole.isEmpty()) {
                thongBao.put("message", "Tên đế giày không được để trống");
                thongBao.put("check", "2");
            } else if (codeSole.length() > 10) {
                thongBao.put("message", "Mã đế giày không được dài quá 10 ký tự");
                thongBao.put("check", "2");
            } else if (nameSole.length() > 50) {
                thongBao.put("message", "Tên đế giày không được dài quá 50 ký tự");
                thongBao.put("check", "2");
            } else {
                soleService.updateSole(id, codeSole, nameSole);
                thongBao.put("message", "Sửa đế giày thành công");
                thongBao.put("check", "1");
            }
            return ResponseEntity.ok(thongBao);
        } catch (Exception e) {
            thongBao.put("message", "Sửa đế giày thất bại");
            thongBao.put("check", "2");
            return ResponseEntity.ok(thongBao);
        }
    }
    @PostMapping("/delete-sole")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteSole(@RequestBody Map<String, Object> payload, HttpSession session) {
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
                thongBao.put("message", "Xóa đế giày thành công");
                thongBao.put("check", "1");
            } else {
                thongBao.put("message", "Khôi phục đế giày thành công");
                thongBao.put("check", "1");
            }
            // Gọi service để cập nhật trạng thái trong cơ sở dữ liệu
            soleService.updateStatus(id, status);
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
