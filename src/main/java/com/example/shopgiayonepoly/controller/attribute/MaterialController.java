package com.example.shopgiayonepoly.controller.attribute;

import com.example.shopgiayonepoly.entites.Material;
import com.example.shopgiayonepoly.entites.Material;
import com.example.shopgiayonepoly.entites.Staff;
import com.example.shopgiayonepoly.service.CashierInventoryService;
import com.example.shopgiayonepoly.service.TimekeepingService;
import com.example.shopgiayonepoly.service.attribute.MaterialService;
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
public class MaterialController {

    @Autowired
    MaterialService materialService;

    @Autowired
    TimekeepingService timekeepingService;
    @Autowired
    CashierInventoryService cashierInventoryService;

    @RequestMapping("/material")
    public String list(Model model, HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return "redirect:/login";
        }
        if(staffLogin.getStatus() != 1) {
            return "redirect:/home_manage";
        }
        model.addAttribute("materialList", materialService.getMaterialNotStatus0());
        model.addAttribute("materialAdd", new Material());
        return "Attribute/material";
    }

    @GetMapping("/material/delete")
    public ResponseEntity<List<Material>> listMaterialDelete(HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return null;
        }
        if(staffLogin.getStatus() != 1) {
            return null;
        }
        List<Material> deletedMaterials = materialService.getMaterialDelete();
        return new ResponseEntity<>(deletedMaterials, HttpStatus.OK);
    }

    @GetMapping("/material/active")
    public ResponseEntity<List<Material>> listActive(HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return null;
        }
        if(staffLogin.getStatus() != 1) {
            return null;
        }
        List<Material> listMaterialActive = materialService.getMaterialNotStatus0();
        return new ResponseEntity<>(listMaterialActive, HttpStatus.OK);
    }

    @GetMapping("/material/get-code")
    public ResponseEntity<List<String>> findAllCodeMaterial(HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return null;
        }
        if(staffLogin.getStatus() != 1) {
            return null;
        }
        List<String> codeMaterial = new ArrayList<>();
        for (Material listMaterial : materialService.findAll()) {
            codeMaterial.add(listMaterial.getCodeMaterial());
        }
        return new ResponseEntity<>(codeMaterial, HttpStatus.OK);
    }

    @GetMapping("/material/get-name")
    public ResponseEntity<List<String>> findAllNameMaterial(HttpSession session) {
        Staff staffLogin = (Staff) session.getAttribute("staffLogin");
        if (staffLogin == null) {
            return null;
        }
        if(staffLogin.getStatus() != 1) {
            return null;
        }
        List<String> nameMaterial = new ArrayList<>();
        for (Material listMaterial : materialService.findAll()) {
            nameMaterial.add(listMaterial.getNameMaterial());
        }
        return new ResponseEntity<>(nameMaterial, HttpStatus.OK);
    }

    @ResponseBody
    @PostMapping("/material/add")
    public ResponseEntity<Map<String, String>> add(@ModelAttribute Material material, HttpSession session) {
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
        for (Material listMaterial : materialService.findAll()) {
            if (material.getCodeMaterial().trim().equalsIgnoreCase(listMaterial.getCodeMaterial().trim())) {
                checkCode = false;
                break;
            }
        }
        for (Material listMaterial : materialService.findAll()) {
            if (material.getNameMaterial().trim().equalsIgnoreCase(listMaterial.getNameMaterial().trim())) {
                checkName = false;
                break;
            }
        }
        if (!checkCode) {
            thongBao.put("message", "Mã chất liệu đã tồn tại");
            thongBao.put("check", "2");
        } else if (!checkName) {
            thongBao.put("message", "Tên chất liệu đã tồn tại");
            thongBao.put("check", "2");
        } else if (material.getCodeMaterial().isEmpty()) {
            thongBao.put("message", "Mã chất liệu không được để trống");
            thongBao.put("check", "2");
        } else if (material.getNameMaterial().isEmpty()) {
            thongBao.put("message", "Tên chất liệu không được để trống");
            thongBao.put("check", "2");
        } else if (material.getCodeMaterial().length() > 10) {
            thongBao.put("message", "Mã chất liệu không được dài quá 10 ký tự");
            thongBao.put("check", "2");
        } else if (material.getNameMaterial().length() > 50) {
            thongBao.put("message", "Tên chất liệu không được dài quá 50 ký tự");
            thongBao.put("check", "2");
        } else {
            material.setStatus(1);
            materialService.save(material);
            thongBao.put("message", "Thêm chất liệu thành công");
            thongBao.put("check", "1");
        }

        return ResponseEntity.ok(thongBao);
    }

    @PostMapping("/update-material")
    @ResponseBody
    public ResponseEntity<Map<String, String>> updateMaterial(@RequestBody Map<String, Object> payload, HttpSession session) {
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
            String codeMaterial;
            String nameMaterial;

            // Lấy giá trị từ payload
            if (payload.get("id") instanceof Integer) {
                id = (Integer) payload.get("id");
            } else {
                id = Integer.parseInt((String) payload.get("id"));
            }

            codeMaterial = (String) payload.get("codeMaterial");
            nameMaterial = (String) payload.get("nameMaterial");
            boolean checkCode = true;
            for (Material listMaterial : materialService.findAll()) {
                if (codeMaterial.trim().equalsIgnoreCase(listMaterial.getCodeMaterial().trim()) && id != listMaterial.getId()) {
                    checkCode = false;
                    break;
                }
            }
            boolean checkName = true;
            for (Material listMaterial : materialService.findAll()) {
                if (nameMaterial.trim().equalsIgnoreCase(listMaterial.getNameMaterial().trim()) && id != listMaterial.getId()) {
                    checkName = false;
                    break;
                }
            }
            // Gọi service để cập nhật dữ liệu chất liệu trong cơ sở dữ liệu
            if (!checkCode) {
                thongBao.put("message", "Mã chất liệu đã tồn tại");
                thongBao.put("check", "2");
            } else if (!checkName) {
                thongBao.put("message", "Tên chất liệu đã tồn tại");
                thongBao.put("check", "2");
            } else if (codeMaterial.isEmpty()) {
                thongBao.put("message", "Mã chất liệu không được để trống");
                thongBao.put("check", "2");
            } else if (nameMaterial.isEmpty()) {
                thongBao.put("message", "Tên chất liệu không được để trống");
                thongBao.put("check", "2");
            } else if (codeMaterial.length() > 10) {
                thongBao.put("message", "Mã chất liệu không được dài quá 10 ký tự");
                thongBao.put("check", "2");
            } else if (nameMaterial.length() > 50) {
                thongBao.put("message", "Tên chất liệu không được dài quá 50 ký tự");
                thongBao.put("check", "2");
            } else {
                materialService.updateMaterial(id, codeMaterial, nameMaterial);
                thongBao.put("message", "Sửa chất liệu thành công");
                thongBao.put("check", "1");
            }
            return ResponseEntity.ok(thongBao);
        } catch (Exception e) {
            thongBao.put("message", "Sửa chất liệu thất bại");
            thongBao.put("check", "2");
            return ResponseEntity.ok(thongBao);
        }
    }
    @PostMapping("/delete-material")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteMaterial(@RequestBody Map<String, Object> payload, HttpSession session) {
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
                thongBao.put("message", "Xóa chất liệu thành công");
                thongBao.put("check", "1");
            } else {
                thongBao.put("message", "Khôi phục chất liệu thành công");
                thongBao.put("check", "1");
            }
            // Gọi service để cập nhật trạng thái trong cơ sở dữ liệu
            materialService.updateStatus(id, status);
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
