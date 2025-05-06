package com.chun.myspringboot.controller.api;

import com.chun.myspringboot.common.ApiResponse;
import com.chun.myspringboot.common.BusinessException;
import com.chun.myspringboot.dto.UserLoginDTO;
import com.chun.myspringboot.dto.UserRegisterDTO;
import com.chun.myspringboot.pojo.User;
import com.chun.myspringboot.service.Impl.SendEmailImpl;
import com.chun.myspringboot.service.Impl.UserServiceImpl;
import com.chun.myspringboot.util.IDUtils;
import com.chun.myspringboot.util.JwtTokenUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户API控制器
 */
@Api(tags = "用户管理", description = "用户相关接口")
@RestController
@RequestMapping("/api/users")
public class UserApiController {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private SendEmailImpl sendEmail;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 用户登录
     */
    @ApiOperation(value = "用户登录", notes = "用户登录接口，返回用户信息和JWT令牌")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(
            @ApiParam(value = "登录信息", required = true) @Valid @RequestBody UserLoginDTO loginDTO) {
        User user = new User();
        user.setEmail(loginDTO.getEmail());
        user.setPassword(loginDTO.getPassword());

        User loginUser = userService.loginByEmailAndPasswordAndActiveStatus(user);
        if (loginUser != null) {
            // 生成JWT令牌
            String token = jwtTokenUtil.generateToken(loginUser.getEmail(), loginUser.getUserId(), loginUser.getRole());

            Map<String, Object> data = new HashMap<>();
            data.put("userId", loginUser.getUserId());
            data.put("userName", loginUser.getUserName());
            data.put("email", loginUser.getEmail());
            data.put("role", loginUser.getRole());
            data.put("token", token);

            return ResponseEntity.ok(ApiResponse.success("登录成功", data));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("邮箱或密码错误"));
        }
    }

    /**
     * 管理员登录
     */
    @ApiOperation(value = "管理员登录", notes = "管理员登录接口，返回管理员信息和JWT令牌")
    @PostMapping("/admin/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> adminLogin(
            @ApiParam(value = "登录信息", required = true) @Valid @RequestBody UserLoginDTO loginDTO) {
        User user = new User();
        user.setEmail(loginDTO.getEmail());
        user.setPassword(loginDTO.getPassword());

        User adminUser = userService.AdminLogin(user);
        if (adminUser != null) {
            // 生成JWT令牌
            String token = jwtTokenUtil.generateToken(adminUser.getEmail(), adminUser.getUserId(), adminUser.getRole());

            Map<String, Object> data = new HashMap<>();
            data.put("userId", adminUser.getUserId());
            data.put("userName", adminUser.getUserName());
            data.put("email", adminUser.getEmail());
            data.put("role", adminUser.getRole());
            data.put("token", token);

            return ResponseEntity.ok(ApiResponse.success("管理员登录成功", data));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("邮箱或密码错误，或者您不是管理员"));
        }
    }

    /**
     * 用户注册
     */
    @ApiOperation(value = "用户注册", notes = "用户注册接口，注册成功后需要通过邮箱激活")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> register(
            @ApiParam(value = "注册信息", required = true) @Valid @RequestBody UserRegisterDTO registerDTO) {
        // 检查邮箱是否已存在
        // 这里需要添加一个新的方法来检查邮箱是否已存在

        // 创建用户对象
        User user = new User();
        user.setUserName(registerDTO.getUserName());
        user.setEmail(registerDTO.getEmail());
        user.setPassword(registerDTO.getPassword());

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
    @ApiOperation(value = "激活账号", notes = "通过激活码激活用户账号")
    @GetMapping("/activate")
    public ResponseEntity<ApiResponse<String>> activate(
            @ApiParam(value = "激活码", required = true) @RequestParam String activeCode) {
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
    @ApiOperation(value = "获取用户信息", notes = "根据用户ID获取用户信息")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> getUserInfo(
            @ApiParam(value = "用户ID", required = true) @PathVariable Integer userId) {
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
    @ApiOperation(value = "更新用户信息", notes = "根据用户ID更新用户信息")
    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> updateUser(
            @ApiParam(value = "用户ID", required = true) @PathVariable Integer userId,
            @ApiParam(value = "用户信息", required = true) @RequestBody User user) {
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
     * 管理员：获取所有用户（支持分页和搜索）
     */
    @ApiOperation(value = "获取所有用户", notes = "管理员接口：获取所有用户信息，支持分页和搜索")
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllUsers(
            @ApiParam(value = "页码", defaultValue = "1") @RequestParam(required = false, defaultValue = "1") Integer page,
            @ApiParam(value = "每页数量", defaultValue = "10") @RequestParam(required = false, defaultValue = "10") Integer size,
            @ApiParam(value = "搜索关键词") @RequestParam(required = false) String query,
            @ApiParam(value = "角色筛选") @RequestParam(required = false) Integer role) {

        List<User> allUsers = userService.queryAllUser();
        List<User> filteredUsers = new java.util.ArrayList<>(allUsers);

        // 根据搜索条件过滤
        if (query != null && !query.isEmpty()) {
            String queryLower = query.toLowerCase();
            filteredUsers.removeIf(user ->
                (user.getUserName() == null || !user.getUserName().toLowerCase().contains(queryLower)) &&
                (user.getEmail() == null || !user.getEmail().toLowerCase().contains(queryLower))
            );
        }

        // 根据角色筛选
        if (role != null) {
            filteredUsers.removeIf(user -> user.getRole() != role);
        }

        // 计算总数
        int total = filteredUsers.size();

        // 分页处理
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, filteredUsers.size());

        List<User> pagedUsers;
        if (startIndex < filteredUsers.size()) {
            pagedUsers = filteredUsers.subList(startIndex, endIndex);
        } else {
            pagedUsers = new java.util.ArrayList<>();
        }

        // 出于安全考虑，清除所有用户的密码和激活码
        for (User user : pagedUsers) {
            user.setPassword(null);
            user.setActiveCode(null);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("data", pagedUsers);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 管理员：删除用户
     */
    @ApiOperation(value = "删除用户", notes = "管理员接口：根据用户ID删除用户")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @ApiParam(value = "用户ID", required = true) @PathVariable Integer userId) {
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
    @ApiOperation(value = "添加用户", notes = "管理员接口：添加新用户")
    @PostMapping
    public ResponseEntity<ApiResponse<User>> addUser(
            @ApiParam(value = "用户信息", required = true) @RequestBody User user) {
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
