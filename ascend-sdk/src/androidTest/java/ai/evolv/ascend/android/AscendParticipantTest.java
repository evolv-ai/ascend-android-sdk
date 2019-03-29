package ai.evolv.ascend.android;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AscendParticipantTest {

    @Test
    public void testBuildDefaultParticipant() {
        AscendParticipant participant = new AscendParticipant.Builder().build();
        Assert.assertNotNull(participant.getUserId());
        Assert.assertNotNull(participant.getSessionId());
    }

    @Test
    public void testSetCustomParticipantAttributes() {
        String userId = "Testy";
        String sessionId = "McTestTest";

        AscendParticipant participant = new AscendParticipant.Builder()
                .setUserId(userId)
                .setSessionId(sessionId)
                .build();

        Assert.assertEquals(userId, participant.getUserId());
        Assert.assertEquals(sessionId, participant.getSessionId());
    }

    @Test
    public void testSetUserIdAfterParticipantCreated() {
        String newUserId = "Testy";
        AscendParticipant participant = new AscendParticipant.Builder().build();
        String oldUserId = participant.getUserId();
        participant.setUserId(newUserId);
        Assert.assertNotEquals(oldUserId, participant.getUserId());
        Assert.assertEquals(newUserId, participant.getUserId());
    }

}
