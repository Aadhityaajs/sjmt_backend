package com.sjmt.SJMT.Controller;


import com.sjmt.SJMT.DTO.RequestDTO.LoginRequest;
import com.sjmt.SJMT.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {


    @Autowired
    private UserService userService;


    @PostMapping("/addNewUser")
    public String addNewUser(LoginRequest loginRequest){
        return userService.addNewUser(loginRequest);
    }


}
