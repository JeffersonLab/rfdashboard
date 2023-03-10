package org.jlab.rfd.config;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AppConfigTest {
    private static class MockEnvironment extends Environment {
        private final Map<String, String> env;
        public MockEnvironment() {
            this.env = new HashMap<>();
        }
        public MockEnvironment(Map<String, String> env) {
            this.env = env;
        }
        public Map<String, String> getenv() {
            return env;
        }
    }

    @Test
    public void basicUsage(){
        AppConfig config = AppConfig.getAppConfig();
        assertEquals(config.getCEDUrl(), "test_ced_file");
        assertEquals(config.getMYAUrl(), "test_mya_file");
    }
    @Test
    public void getCEDUrl() {
        String exp = "test_ced_file";
        String result = AppConfig.getAppConfig(null, new MockEnvironment(), true).getCEDUrl();
        assertEquals(exp, result);
    }

    @Test
    public void getMYAUrl() {
        String exp = "test_mya_file";
        String result = AppConfig.getAppConfig(null, new MockEnvironment(), true).getMYAUrl();
        assertEquals(exp, result);
    }


    @Test
    public void getCEDUrl_Stream() {
        String cfg = "CED_URL = test_ced_stream";
        String exp = "test_ced_stream";
        InputStream is = new ByteArrayInputStream(cfg.getBytes());
        String result = AppConfig.getAppConfig(is, new MockEnvironment(), true).getCEDUrl();
        assertEquals(exp, result);
    }

    @Test
    public void getMYAUrl_Stream() {
        String cfg = "MYA_URL = test_mya_stream";
        String exp = "test_mya_stream";
        InputStream is = new ByteArrayInputStream(cfg.getBytes());
        String result = AppConfig.getAppConfig(is, new MockEnvironment(), true).getMYAUrl();
        assertEquals(exp, result);
    }

    @Test
    public void getCEDUrl_ENV() {
        String exp = "test_ced_env";
        Map<String, String> env = new HashMap<>();
        env.put("RFD_CED_URL", "test_ced_env");
        String result = AppConfig.getAppConfig(null, new MockEnvironment(env), true).getCEDUrl();
        assertEquals(exp, result);
    }

    @Test
    public void getMYAUrl_ENV() {
        String exp = "test_mya_env";
        Map<String, String> env = new HashMap<>();
        env.put("RFD_MYA_URL", "test_mya_env");
        String result = AppConfig.getAppConfig(null, new MockEnvironment(env), true).getMYAUrl();
        assertEquals(exp, result);
    }
}
