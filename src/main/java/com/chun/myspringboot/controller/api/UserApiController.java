package com.chun.myspringboot.controller.api;

import com.chun.myspringboot.common.ApiResponse;
import com.chun.myspringboot.common.BusinessException;
import com.chun.myspringboot.pojo.User;
import com.chun.myspringboot.service.Impl.SendEmailImpl;
import com.chun.myspringboot.service.Impl.UserServiceImpl;
import com.chun.myspringboot.util.IDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户API控制器
 */
@RestController
@RequestMapping("/api/users")
public class UserApiController {
    
    @Autowired
    private UserServiceImpl userService;
    
    @Autowired
    private SendEmailImpl sendEmail;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody User user) {
        User loginUser = userService.loginByEmailAndPasswordAndActiveStatus(user);
        if (loginUser != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("userId", loginUser.getUserId());
            data.put("userName", loginUser.getUserName());
            data.put("email", loginUser.getEmail());
            data.put("role", loginUser.getRole());
            // 这里可以添加JWT token生成逻辑
            
            return ResponseEntity.ok(ApiResponse.success("登录成功", data));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("邮箱或密码错误"));
        }
    }
    
    /**
     * 管理员登录
     */
    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> adminLogin(@RequestBody User user) {
        User adminUser = userService.AdminLogin(user);
        if (adminUser != null) {
            Map<String, Object> data = new HashMap<>();
            data.put("userId", adminUser.getUserId());
            data.put("userName", adminUser.getUserName());
            data.put("email", adminUser.getEmail());
            data.put("role", adminUser.getRole());
            // 这里可以添加JWT token生成逻辑
            
            return ResponseEntity.ok(ApiResponse.success("管理员登录成功", data));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("邮箱或密码错误，或者您不是管理员"));
        }
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(@RequestBody User user) {
        // 检查邮箱是否已存在
        // 这里需要添加一个新的方法来检查邮箱是否已存在
        
        // 设置激活状态为0（未激活）
        user.setActiveStatus(0);
        // 生成激活码
        String activeCode = IDUtils.getUUID();
        user.setActiveCode(activeCode);
        // 设置默认角色为普通用户
        user.setRole(0);
        
        // 添加用户到数据库
        int result = userService.addUser(user);
        if (result > 0) {
            // 发送激活邮件
            String email = user.getEmail();
            String subject = "来自StudyEnglish网站的激活邮件";
            String context = "<a href=\"http://localhost:8080/api/users/activate?activeCode=" + activeCode + "\">激活请点击:" + activeCode + "</a>";
            sendEmail.SendEmail(email, subject, context);
            
            Map<String, Object> data = new HashMap<>();
            data.put("userId", user.getUserId());
            data.put("email", user.getEmail());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("注册成功，请前往邮箱激活账号", data));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("注册失败，请稍后重试"));
        }
    }
    
    /**
     * 激活账号
     */
    @GetMapping("/activate")
    public ResponseEntity<ApiResponse<String>> activate(@RequestParam String activeCode) {
        User user = userService.queryUserByActiveCode(activeCode);
        if (user != null) {
            // 激活用户
            user.setActiveStatus(1);
            // 清除激活码
            user.setActiveCode("");
            userService.updateUser(user);
            
            return ResponseEntity.ok(ApiResponse.success("账号激活成功，现在可以登录了", null));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("激活码无效或已过期"));
        }
    }
    
    /**
     * 获取用户信息
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> getUserInfo(@PathVariable Integer userId) {
        User user = userService.queryUserById(userId);
        if (user != null) {
            // 出于安全考虑，清除密码和激活码
            user.setPassword(null);
            user.setActiveCode(null);
            
            return ResponseEntity.ok(ApiResponse.success(user));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户不存在"));
        }
    }
    
    /**
     * 更新用户信息
     */
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> updateUser(@PathVariable Integer userId, @RequestBody User user) {
        // 确保userId匹配
        if (!userId.equals(user.getUserId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("用户ID不匹配"));
        }
        
        // 获取原用户信息
        User existingUser = userService.queryUserById(userId);
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户不存在"));
        }
        
        // 更新用户信息
        int result = userService.updateUser(user);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("用户信息更新成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("更新失败，请稍后重试"));
        }
    }
    
    /**
     * 管理员：获取所有用户
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        List<User> users = userService.queryAllUser();
        // 出于安全考虑，清除所有用户的密码和激活码
        for (User user : users) {
            user.setPassword(null);
            user.setActiveCode(null);
        }
        
        return ResponseEntity.ok(ApiResponse.success(users));
    }
    
    /**
     * 管理员：删除用户
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Integer userId) {
        User existingUser = userService.queryUserById(userId);
        if (existingUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("用户不存在"));
        }
        
        int result = userService.deleteUser(userId);
        if (result > 0) {
            return ResponseEntity.ok(ApiResponse.success("用户删除成功", null));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("删除失败，请稍后重试"));
        }
    }
    
    /**
     * 管理员：添加用户
     */
    @PostMapping
    public ResponseEntity<ApiResponse<User>> addUser(@RequestBody User user) {
        int result = userService.addUser(user);
        if (result > 0) {
            // 获取添加后的用户（包含生成的ID）
            User addedUser = userService.queryUserById(user.getUserId());
            // 出于安全考虑，清除密码和激活码
            if (addedUser != null) {
                addedUser.setPassword(null);
                addedUser.setActiveCode(null);
            }
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("用户添加成功", addedUser));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("添加失败，请稍后重试"));
        }
    }
}
