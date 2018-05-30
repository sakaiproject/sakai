package org.sakaiproject.rubrics.controller;

import javax.annotation.Resource;

import org.sakaiproject.rubrics.logic.RubricsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

    @Resource(name = "org.sakaiproject.rubrics.logic.RubricsService")
    RubricsService rubricsService;

    @GetMapping("/")
    public String indexRedirect() {
        return "redirect:/index";
    }

    @GetMapping("/index")
    public String index(ModelMap model) {
        String token = rubricsService.generateJsonWebToken("sakai.rubrics");
        model.addAttribute("token", token);
        return "index";
    }
}
