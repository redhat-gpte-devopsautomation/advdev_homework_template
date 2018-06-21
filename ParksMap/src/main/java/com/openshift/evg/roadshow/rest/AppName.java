package com.openshift.evg.roadshow.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ws/appname")
public class AppName {

    @RequestMapping(method = RequestMethod.GET, value = "/")
    public String appname() {
      String applicationName = System.getenv("APPNAME");
      if (applicationName == null) applicationName = "Parks Map (App Name not set)";
      return applicationName;
    }
}
