package com.github.shk0da.micro.smartvista.service;

import org.junit.FixMethodOrder;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest({"spring.profiles.active=test"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SmartvistaServiceTest {

    @Autowired
    private SmartvistaService smartvistaService;
}