package com.chun.myspringboot.controller;

import com.chun.myspringboot.pojo.Notice;
import com.chun.myspringboot.pojo.User;
import com.chun.myspringboot.service.Impl.NoticeServiceImpl;
import com.chun.myspringboot.service.Impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
public class LoginController {


    @Autowired
    private UserServiceImpl userService;

    /**普通用户登录
     *登录成功，添加session
     */
    @RequestMapping("/login")
    public String login(User user, Model model, HttpSession session){
        User usr = userService.loginByEmailAndPasswordAndActiveStatus(user);
        if (usr!=null){
            //设置session
            session.setAttribute("loginUser",usr);

            System.out.println("成功登录");
            return "redirect:/main.html";
        }else {
            model.addAttribute("msg","密码或账号输入错误");
            return "index";
        }
    }

    /**
     * 管理员用户登录
     * 如果当前用户已经是管理员，则直接重定向到管理员后台
     * 否则跳转到管理员登录页面
     */
    @RequestMapping("/toAdmin")
    public String toAdmin(HttpSession session){
        // 检查当前会话中是否已有登录用户
        User loginUser = (User) session.getAttribute("loginUser");
        // 如果已登录且是管理员角色(role=1)，则直接重定向到管理员后台
        if (loginUser != null && loginUser.getRole() == 1) {
            System.out.println("当前用户已是管理员，直接进入后台");
            return "redirect:/admin/main";
        }
        // 否则跳转到管理员登录页面
        return "admin/index";
    }
    @RequestMapping("/admin")
    public String AdminLogin(User user, Model model,HttpSession session){
        User usr = userService.AdminLogin(user);
        if (usr!=null){
            // 使用从数据库查询到的完整用户对象设置会话
            session.setAttribute("loginUser",usr);
            System.out.println("管理员成功登录");
            return "redirect:/admin/main";
        }else {
            model.addAttribute("msg","密码或账号输入错误");
            return "admin/index";
        }
    }
    /**
     * 注销
     */
    @RequestMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "index";
    }

}
