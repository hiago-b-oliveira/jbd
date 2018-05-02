package io.jbd.weblogin.controller;

import io.jbd.weblogin.dao.UserAccountDAO;
import io.jbd.weblogin.domain.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.Map;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private UserAccountDAO userAccountDAO;

    @GetMapping("/home")
    public String home(Principal principal, HttpSession httpSession, Map<String, Object> model) {
        logger.trace("Executing home with principal: {}", principal.getName());

        UserAccount userAccount = userAccountDAO.findByLogin(principal.getName());

        model.put("user", String.valueOf(userAccount));
        model.put("token", httpSession.getId());

        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/access-denied")
    public String accesDenied() {
        return "access-denied";
    }
}
