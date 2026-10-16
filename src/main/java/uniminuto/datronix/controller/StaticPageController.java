package uniminuto.datronix.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaticPageController {

    @GetMapping("/")
    public String inicioSesion() {
        return "redirect:/html/Inicio_Sesion.html";
    }

    @GetMapping({"/bodega", "/bodega/"})
    public String bodega() {
        return "redirect:/bodega/index.html";
    }
}
