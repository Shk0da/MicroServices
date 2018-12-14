package com.github.shk0da.micro.visa2.util;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class Visa2UtilTest {

    private final static Logger log = LoggerFactory.getLogger(Visa2UtilTest.class);

    static {
        Arrays.stream(Visa2Util.Code.values()).forEach(code -> log.debug("{}[{}, {}]", code.name(), code.getCode(), code.getDescription()));
    }

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void prepareResponse() {
    }

    @Test
    public void parseRequest() {
    }

    @Test
    public void calculateMac() {
    }

    @Test
    public void transliterateMessage() {
    }

    @Test
    public void getCurrentDateMMDDYY() {
    }

    @Test
    public void generateAuthorizationCode() {
    }
}