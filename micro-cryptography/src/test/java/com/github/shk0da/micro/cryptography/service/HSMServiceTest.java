package com.github.shk0da.micro.cryptography.service;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest({"spring.profiles.active=test"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class HSMServiceTest {

    private final static Logger log = LoggerFactory.getLogger(HSMServiceTest.class);

    private static String mac;

    @Autowired
    private HSMService hsmService;

    private static final byte[] MESSAGE = ("Lorem ipsum dolor sit amet, adhuc dicunt prodesset cum no, eos lorem albucius " +
            "comprehensam no. Te nec ignota fuisset, omnium rationibus an qui, graeco equidem vivendo eam no. Ei duo " +
            "delenit verterem vituperatoribus, et eos prompta detraxit. Eam suas viris denique cu. Cu esse inani repudiare " +
            "vix, veniam platonem est id, simul timeam explicari ad vix.").getBytes();

    @Before
    public void setUp() {
        log.info("HSMServiceTest setUp");
    }

    @After
    public void tearDown() {
        log.info("HSMServiceTest tearDown");
    }

    @Test
    public void test1GenerateMAC() {
        mac = hsmService.generateMAC(MESSAGE);
        assertNotNull("MAC was not generated", mac);
    }

    @Test
    public void test2VerifyMAC() {
        assertTrue("MAC not passed the test", hsmService.verifyMAC(mac, MESSAGE));
    }
}