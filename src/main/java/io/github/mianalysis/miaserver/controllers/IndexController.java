package io.github.mianalysis.miaserver.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IndexController {

  @RequestMapping(value="/", method={RequestMethod.GET,RequestMethod.HEAD})
  public String index() {
    return "MIA Server";
  }

}
