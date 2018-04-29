package io.jbd.weblogin.controller.rest;

import io.jbd.weblogin.domain.UserAccount;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestRestController {

    @GetMapping("/user/{login}/evict")
    @CacheEvict(cacheNames = UserAccount.CACHE_NAME, key = "#login")
    public void evictUser(@PathVariable("login") String login) {

    }

}
