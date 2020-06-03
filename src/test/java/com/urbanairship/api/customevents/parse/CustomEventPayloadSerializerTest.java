package com.urbanairship.api.customevents.parse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urbanairship.api.customevents.model.CustomEventBody;
import com.urbanairship.api.customevents.model.CustomEventChannelType;
import com.urbanairship.api.customevents.model.CustomEventPayload;
import com.urbanairship.api.customevents.model.CustomEventUser;
import com.urbanairship.api.push.parse.PushObjectMapper;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class CustomEventPayloadSerializerTest {

    private static final ObjectMapper MAPPER = PushObjectMapper.getInstance();

    @Test
    public void testFullPayload() throws IOException {
        CustomEventUser customEventUser = CustomEventUser.newBuilder()
                .setCustomEventChannelType(CustomEventChannelType.ANDROID_CHANNEL)
                .setChannel("e393d28e-23b2-4a22-9ace-dc539a5b07a8")
                .build();

        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("category", "mens shoes");
        properties.put("id", "pid-11046546");
        properties.put("description", "sky high");
        properties.put("brand", "victory");

        CustomEventBody customEventBody = CustomEventBody.newBuilder()
                .setName("purchased")
                .setValue(new BigDecimal(120.49))
                .setTransaction("886f53d4-3e0f-46d7-930e-c2792dac6e0a")
                .setInteractionId("your.store/us/en_us/pd/shoe/pid-11046546/pgid-10978234")
                .setInteractionType("url")
                .addAllPropertyEntries(properties)
                .setSessionId("22404b07-3f8f-4e42-a4ff-a996c18fa9f1")
                .build();

        DateTime occurred = new DateTime(2015, 5, 2, 2, 31, 22, DateTimeZone.UTC);

        CustomEventPayload customEventPayload = CustomEventPayload.newBuilder()
                .setCustomEventBody(customEventBody)
                .setCustomEventUser(customEventUser)
                .setOccurred(occurred)
                .build();

        String json = MAPPER.writeValueAsString(customEventPayload);
        String expected = "{\"occurred\": \"2015-05-02T02:31:22\",\"user\": {\"android_channel\": \"e393d28e-23b2-4a22-9ace-dc539a5b07a8\"},\"body\": {\"name\": \"purchased\",\"value\": 120.49,\"transaction\": \"886f53d4-3e0f-46d7-930e-c2792dac6e0a\",\"interaction_id\": \"your.store/us/en_us/pd/shoe/pid-11046546/pgid-10978234\",\"interaction_type\": \"url\",\"properties\": {\"category\": \"mens shoes\",\"id\": \"pid-11046546\",\"description\": \"sky high\",\"brand\": \"victory\"},\"session_id\": \"22404b07-3f8f-4e42-a4ff-a996c18fa9f1\"}}";

        JsonNode jsonFromObject = MAPPER.readTree(json);
        JsonNode jsonFromString = MAPPER.readTree(expected);


        assertEquals(jsonFromString, jsonFromObject);
    }

	@Test
	public void testMinimalPayloadWithNestedProperties() throws IOException {
		CustomEventUser customEventUser = CustomEventUser.newBuilder()
				.setCustomEventChannelType(CustomEventChannelType.GENERIC_CHANNEL)
				.setChannel("e393d28e-23b2-4a22-9ace-dc539a5b07a8")
				.build();


		Map<String, String> extras = new HashMap<String, String>();
		extras.put("super", "cool");
		extras.put("signature", "best");

		Map<String, Object> item1Attributes = new HashMap<String, Object>();
		item1Attributes.put("text", "New Line Sneakers");
		item1Attributes.put("price", "$ 79.95");
		item1Attributes.put("extras", extras);
		Map<String, Object> item2Attributes = new HashMap<String, Object>();
		item2Attributes.put("text", "Old Line Sneakers");
		item2Attributes.put("price", "$ 79.95");

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put("description", "sky high");
		properties.put("brand", "victory");
		properties.put("colors", new String[]{"red", "blue"});
		properties.put("items", asList(item1Attributes, item2Attributes));

		CustomEventBody customEventBody = CustomEventBody.newBuilder()
				.setName("purchased")
				.addAllPropertyEntries(properties)
				.build();

		DateTime occurred = new DateTime(2015, 5, 2, 2, 31, 22, DateTimeZone.UTC);

		CustomEventPayload customEventPayload = CustomEventPayload.newBuilder()
				.setCustomEventBody(customEventBody)
				.setCustomEventUser(customEventUser)
				.setOccurred(occurred)
				.build();

		String json = MAPPER.writeValueAsString(customEventPayload);
		String expected = "{\"body\":{\"name\":\"purchased\",\"session_id\":null,\"properties\":{\"description\":\"sky high\",\"brand\":\"victory\",\"items\":[{\"price\":\"$ 79.95\",\"extras\":{\"super\":\"cool\",\"signature\":\"best\"},\"text\":\"New Line Sneakers\"},{\"price\":\"$ 79.95\",\"text\":\"Old Line Sneakers\"}],\"colors\":[\"red\",\"blue\"]}},\"occurred\":\"2015-05-02T02:31:22\",\"user\":{\"channel\":\"e393d28e-23b2-4a22-9ace-dc539a5b07a8\"}}";

		JsonNode jsonFromObject = MAPPER.readTree(json);
		JsonNode jsonFromString = MAPPER.readTree(expected);


		assertEquals(jsonFromString, jsonFromObject);
	}
}
