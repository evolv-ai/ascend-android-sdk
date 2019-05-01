package ai.evolv;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import org.junit.Assert;
import org.junit.Test;

import ai.evolv.exceptions.AscendKeyError;

import java.util.HashSet;
import java.util.Set;

public class AllocationsTest {

    private static final String rawAllocation = "[{\"uid\":\"test_uid\",\"sid\":\"test_sid\",\"eid\":\"test_eid\",\"cid\":\"test_cid\",\"genome\":{\"search\":{\"weighting\":{\"distance\":2.5,\"dealer_score\":2.5}},\"pages\":{\"all_pages\":{\"header_footer\":[\"blue\",\"white\"]},\"testing_page\":{\"megatron\":\"none\",\"header\":\"white\"}},\"algorithms\":{\"feature_importance\":false}},\"excluded\":false}]";
    private static final String rawMultiAllocation = "[{\"uid\":\"test_uid\",\"sid\":\"test_sid\",\"eid\":\"test_eid\",\"cid\":\"test_cid\",\"genome\":{\"search\":{\"weighting\":{\"distance\":2.5,\"dealer_score\":2.5}},\"pages\":{\"all_pages\":{\"header_footer\":[\"blue\",\"white\"]},\"testing_page\":{\"megatron\":\"none\",\"header\":\"white\"}},\"algorithms\":{\"feature_importance\":false}},\"excluded\":false}," +
            "{\"uid\":\"test_uid\",\"sid\":\"test_sid\",\"eid\":\"test_eid_2\",\"cid\":\"test_cid_2\",\"genome\":{\"best\":{\"baked\":{\"cookie\":true,\"cake\":false}},\"utensils\":{\"knives\":{\"drawer\":[\"butcher\",\"paring\"]},\"spoons\":{\"wooden\":\"oak\",\"metal\":\"steel\"}},\"measure\":{\"cups\":2.0}},\"excluded\":false}]";
    private static final String rawMultiAllocationWithDups = "[{\"uid\":\"test_uid\",\"sid\":\"test_sid\",\"eid\":\"test_eid\",\"cid\":\"test_cid\",\"genome\":{\"search\":{\"weighting\":{\"distance\":2.5,\"dealer_score\":2.5}},\"pages\":{\"all_pages\":{\"header_footer\":[\"blue\",\"white\"]},\"testing_page\":{\"megatron\":\"none\",\"header\":\"white\"}},\"algorithms\":{\"feature_importance\":false}},\"excluded\":false}," +
            "{\"uid\":\"test_uid\",\"sid\":\"test_sid\",\"eid\":\"test_eid_2\",\"cid\":\"test_cid_2\",\"genome\":{\"best\":{\"baked\":{\"cookie\":true,\"cake\":false}},\"utensils\":{\"knives\":{\"drawer\":[\"butcher\",\"paring\"]},\"spoons\":{\"wooden\":\"oak\",\"metal\":\"steel\"}},\"algorithms\":{\"feature_importance\":true}},\"excluded\":false}]";

    JsonArray parseRawAllocations(String raw) {
        JsonParser parser = new JsonParser();
        return parser.parse(raw).getAsJsonArray();
    }

    @Test
    public void testGetGenomeFromAllocation() {
        Allocations allocations = new Allocations(parseRawAllocations(rawAllocation));
        String genome = allocations.getGenomeFromAllocations().toString();
        String expectedGenome = "{\"genome\":{\"search\":{\"weighting\":{\"distance\":2.5,\"dealer_score\":2.5}},\"pages\":{\"all_pages\":{\"header_footer\":[\"blue\",\"white\"]},\"testing_page\":{\"megatron\":\"none\",\"header\":\"white\"}},\"algorithms\":{\"feature_importance\":false}}}";
        Assert.assertEquals(expectedGenome, genome);
    }

    @Test
    public void testGetGenomeFromMultAllocation() {
        Allocations allocations = new Allocations(parseRawAllocations(rawMultiAllocation));
        String genome = allocations.getGenomeFromAllocations().toString();
        String expectedGenome = "{\"genome\":{\"search\":{\"weighting\":{\"distance\":2.5,\"dealer_score\":2.5}},\"pages\":{\"all_pages\":{\"header_footer\":[\"blue\",\"white\"]},\"testing_page\":{\"megatron\":\"none\",\"header\":\"white\"}},\"algorithms\":{\"feature_importance\":false}," +
                "\"best\":{\"baked\":{\"cookie\":true,\"cake\":false}},\"utensils\":{\"knives\":{\"drawer\":[\"butcher\",\"paring\"]},\"spoons\":{\"wooden\":\"oak\",\"metal\":\"steel\"}},\"measure\":{\"cups\":2.0}}}";
        Assert.assertEquals(expectedGenome, genome);
    }

    @Test
    public void testGetGenomeFromMultAllocationWithDuplicateKeys() {
        Allocations allocations = new Allocations(parseRawAllocations(rawMultiAllocationWithDups));
        String genome = allocations.getGenomeFromAllocations().toString();
        String expectedGenome = "{\"genome\":{\"search\":{\"weighting\":{\"distance\":2.5,\"dealer_score\":2.5}},\"pages\":{\"all_pages\":{\"header_footer\":[\"blue\",\"white\"]},\"testing_page\":{\"megatron\":\"none\",\"header\":\"white\"}},\"algorithms\":{\"feature_importance\":true}," +
                "\"best\":{\"baked\":{\"cookie\":true,\"cake\":false}},\"utensils\":{\"knives\":{\"drawer\":[\"butcher\",\"paring\"]},\"spoons\":{\"wooden\":\"oak\",\"metal\":\"steel\"}}}}";
        Assert.assertEquals(expectedGenome, genome);
    }

    @Test
    public void testGetValueFromAllocationGenome() {
        try {
            Allocations allocations = new Allocations(parseRawAllocations(rawAllocation));
            Boolean featureImportance = allocations.getValueFromGenome("algorithms.feature_importance", Boolean.class);
            Assert.assertEquals(featureImportance, false);
            double weightingDistance = allocations.getValueFromGenome("search.weighting.distance", double.class);
            Assert.assertEquals(weightingDistance, 2.5, 0);
        } catch (AscendKeyError e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testGetValueFromMultiAllocationGenome() {
        try {
            Allocations allocations = new Allocations(parseRawAllocations(rawMultiAllocation));
            Boolean featureImportance = allocations.getValueFromGenome("algorithms.feature_importance", Boolean.class);
            Assert.assertEquals(featureImportance, false);
            double weightingDistance = allocations.getValueFromGenome("search.weighting.distance", double.class);
            Assert.assertEquals(weightingDistance, 2.5, 0);
        } catch (AscendKeyError e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testGetValueFromMultiAllocationWithDupsGenome() {
        try{
            Allocations allocations = new Allocations(parseRawAllocations(rawMultiAllocationWithDups));
            Boolean featureImportance = allocations.getValueFromGenome("algorithms.feature_importance", Boolean.class);
            Assert.assertEquals(featureImportance, true);
            double weightingDistance = allocations.getValueFromGenome("search.weighting.distance", double.class);
            Assert.assertEquals(weightingDistance, 2.5, 0);
        } catch (AscendKeyError e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testGetActiveExperiments() {
        Allocations allocations = new Allocations(parseRawAllocations(rawMultiAllocation));
        Set<String> activeExperiments = allocations.getActiveExperiments();
        Set<String> expected = new HashSet<>();
        expected.add("test_eid");
        expected.add("test_eid_2");
        Assert.assertEquals(expected, activeExperiments);
    }


}
