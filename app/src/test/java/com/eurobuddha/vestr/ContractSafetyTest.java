package com.eurobuddha.vestr;
import org.json.*;
import org.junit.Test;
import static org.junit.Assert.*;
public class ContractSafetyTest {
    private JSONObject coin() throws Exception {
        String hex = "0x" + "ab".repeat(32);
        JSONArray state = new JSONArray();
        for (int port : new int[]{0,1,2,3,4,5,6,7,8,199})
            state.put(new JSONObject().put("port",port).put("data",port == 0 || port == 199 ? hex : "1"));
        return new JSONObject().put("coinid",hex).put("address",hex).put("tokenid","0x00").put("amount","1").put("state",state);
    }
    @Test public void originalContractStateAndBothScriptsRemainUsable() throws Exception {
        assertTrue(Contract.from(coin()).safeForCommands());
        assertNull(CommandSafety.failure("newscript script:\""+VestingContract.CLEAN_SCRIPT+"\""));
        assertNull(CommandSafety.failure("runscript script:\""+VestingContract.CHECK_MATHS+"\""));
        // representative ASSEMBLED command with JSON params, exactly as ContractDetailActivity.runMaths builds it
        assertNull(CommandSafety.failure("runscript script:\""+VestingContract.CHECK_MATHS
                +"\" prevstate:{\"1\":\"1\",\"2\":\"1\",\"3\":\"1\",\"4\":\"1\",\"5\":\"1\"} globals:{\"@AMOUNT\":\"1\",\"@BLOCK\":\"1\",\"@COINAGE\":\"1\"}"));
    }
    @Test public void malformedCoinFieldsAreRefused() throws Exception {
        assertFalse("coinid", Contract.from(coin().put("coinid","0x12")).safeForCommands());
        assertFalse("tokenid", Contract.from(coin().put("tokenid","0x1234")).safeForCommands());
        assertFalse("amount", Contract.from(coin().put("amount","0")).safeForCommands());
        JSONObject zeroTotal = coin(); zeroTotal.getJSONArray("state").getJSONObject(1).put("data","0");
        assertFalse("total", Contract.from(zeroTotal).safeForCommands());
    }
    @Test public void stateCannotInjectCommandsParametersOrJson() throws Exception {
        for (int row=0;row<10;row++) for(String bad:new String[]{"1;quit", "1 amount:99", "1\"}", "1e999999", "1\n"}) {
            JSONObject coin=coin();coin.getJSONArray("state").getJSONObject(row).put("data",bad);
            assertFalse(row+":"+bad,Contract.from(coin).safeForCommands());
        }
        for (String bad:new String[]{"keys;quit", "runscript script:\"RETURN TRUE;quit\"", "send state:{\"1\":\"1;quit\"}", "keys\n"})
            assertNotNull(CommandSafety.failure(bad));
    }
}
