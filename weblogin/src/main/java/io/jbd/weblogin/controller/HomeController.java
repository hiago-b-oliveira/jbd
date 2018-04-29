package io.jbd.weblogin.controller;

import io.jbd.weblogin.dao.UserAccountDAO;
import io.jbd.weblogin.domain.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private UserAccountDAO userAccountDAO;

    @GetMapping("/home")
    public String home(@RequestParam("login") String login, Map<String, Object> model) {
        logger.trace("Executing home with param: {}", login);

        UserAccount userAccount = userAccountDAO.findByLogin(login);

        model.put("user", String.valueOf(userAccount));

        return "home";
    }

}
