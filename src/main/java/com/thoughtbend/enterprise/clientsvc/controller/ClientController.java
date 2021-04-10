package com.thoughtbend.enterprise.clientsvc.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thoughtbend.enterprise.clientsvc.util.Const;

@RestController
@RequestMapping(Const.ApiPath.VERSION + "/client")
@CrossOrigin
public class ClientController {

	private static final Logger LOG = LoggerFactory.getLogger(ClientController.class);
}
