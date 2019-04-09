package ai.evolv;

import org.junit.Assert;
import org.junit.Test;

public class AscendConfigTest {

    private static final String ENVIRONMENT_ID = "test_12345";

    @Test
    public void testBuildDefaultConfig() {
        AscendConfig config = new AscendConfig.Builder(ENVIRONMENT_ID).build();

        Assert.assertEquals(ENVIRONMENT_ID, config.getEnvironmentId());
        Assert.assertEquals(AscendConfig.DEFAULT_HTTP_SCHEME, config.getHttpScheme());
        Assert.assertEquals(AscendConfig.DEFAULT_DOMAIN, config.getDomain());
        Assert.assertEquals(AscendConfig.DEFAULT_API_VERSION, config.getVersion());
        Assert.assertEquals(AscendConfig.DEFAULT_TIMEOUT, config.getTimeout());
        Assert.assertEquals(AscendConfig.DEFAULT_ALLOCATION_STORE, config.getAscendAllocationStore());
        Assert.assertEquals(AscendConfig.DEFAULT_ASCEND_PARTICIPANT, config.getAscendParticipant());
    }

    @Test
    public void testBuildConfig() {
        long timeout = 1;
        String domain = "test.evolv.ai";
        String version = "test";
        AscendAllocationStore allocationStore = new DefaultAscendAllocationStore();
        AscendParticipant participant = new AscendParticipant.Builder().build();

        AscendConfig config = new AscendConfig.Builder(ENVIRONMENT_ID)
                .setTimeout(timeout)
                .setDomain(domain)
                .setVersion(version)
                .setAscendAllocationStore(allocationStore)
                .setAscendParticipant(participant)
                .build();

        Assert.assertEquals(ENVIRONMENT_ID, config.getEnvironmentId());
        Assert.assertEquals(domain, config.getDomain());
        Assert.assertEquals(version, config.getVersion());
        Assert.assertEquals(timeout, config.getTimeout());
        Assert.assertEquals(allocationStore, config.getAscendAllocationStore());
        Assert.assertEquals(participant, config.getAscendParticipant());
    }

}
