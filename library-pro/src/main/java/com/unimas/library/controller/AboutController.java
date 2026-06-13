package com.unimas.library.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** About page - project info, team members and technology stack. */
@Controller
public class AboutController {

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
