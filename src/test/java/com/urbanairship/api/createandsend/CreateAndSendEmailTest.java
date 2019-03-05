package com.urbanairship.api.createandsend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urbanairship.api.channel.parse.ChannelObjectMapper;
import com.urbanairship.api.client.UrbanAirshipClient;
import com.urbanairship.api.common.parse.DateFormats;
import com.urbanairship.api.createandsend.model.audience.email.EmailChannel;
import com.urbanairship.api.createandsend.model.audience.email.EmailChannels;
import com.urbanairship.api.createandsend.model.notification.email.CreateAndSendEmailPayload;
import com.urbanairship.api.createandsend.model.notification.email.EmailFields;
import com.urbanairship.api.createandsend.model.notification.email.EmailTemplate;
import com.urbanairship.api.push.model.Campaigns;
import com.urbanairship.api.push.model.DeviceType;
import com.urbanairship.api.createandsend.model.audience.CreateAndSendAudience;
import com.urbanairship.api.push.model.notification.Notification;
import com.urbanairship.api.createandsend.model.notification.CreateAndSendPayload;
import com.urbanairship.api.push.model.notification.email.MessageType;
import com.urbanairship.api.push.parse.PushObjectMapper;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;


/***
 * Client is already under test coverage so no need to test headers as they are set during client creation.
 * Here we test the JSON string that is passed to the client prior to sending
 */
public class CreateAndSendEmailTest {

    private static final ObjectMapper CHANNEL_OBJECT_MAPPER = ChannelObjectMapper.getInstance();
    private static final ObjectMapper PUSH_OBJECT_MAPPER = PushObjectMapper.getInstance();

    String htmlBodyString = "<h1>Seasons Greetings</h1><p>Check out our winter deals!</p><p><a data-ua-unsubscribe=\"1\" title=\"unsubscribe\" href=\"http://unsubscribe.urbanairship.com/email/success.html\">Unsubscribe</a></p>";
    String plaintextBodyString = "Greetings! Check out our latest winter deals! [[ua-unsubscribe href=\"http://unsubscribe.urbanairship.com/email/success.html\"]]";

    EmailChannel newChannel;
    EmailChannel benChannel;
    CreateAndSendAudience audience;
    CreateAndSendEmailPayload createAndSendEmailPayload;
    CreateAndSendEmailPayload templateEmailPayload;
    Notification notification;
    Campaigns campaign;
    CreateAndSendPayload payload;
    CreateAndSendRequest request;
    CreateAndSendPayload templatePayload;

    @Before
    public void setUp() {
        DateTime newDateTime = DateTime.parse("2018-11-29T10:34:22", DateFormats.DATE_FORMATTER);
        DateTime benDateTime = DateTime.parse("2018-11-29T12:45:10", DateFormats.DATE_FORMATTER);

        newChannel = EmailChannel.newBuilder()
                .setAddress("new@email.com")
                .setCommertialOptedIn(newDateTime)
                .build();

        benChannel = EmailChannel.newBuilder()
                .setAddress("ben@icetown.com")
                .setTransactionalOptedIn(benDateTime)
                .build();

        audience = new CreateAndSendAudience(EmailChannels.newBuilder()
                .addChannel(newChannel)
                .addChannel(benChannel)
                .build());

        createAndSendEmailPayload = CreateAndSendEmailPayload.newBuilder()
                .setSubject("Welcome to the Winter Sale! ")
                .setHtmlBody(htmlBodyString)
                .setPlaintextBody(plaintextBodyString)
                .setMessageType(MessageType.COMMERCIAL)
                .setSenderName("Urban Airship")
                .setSenderAddress("team@urbanairship.com")
                .setReplyTo("no-reply@urbanairship.com")
                .build();

        notification = Notification.newBuilder()
                .addDeviceTypeOverride(DeviceType.EMAIL, createAndSendEmailPayload)
                .build();

        campaign = Campaigns.newBuilder()
                .addCategory("winter sale")
                .addCategory("west coast")
                .build();

        payload = CreateAndSendPayload.newBuilder()
                .setAudience(audience)
                .setNotification(notification)
                .setCampaigns(campaign)
                .build();

        request = CreateAndSendRequest.newRequest(payload);

        EmailTemplate template = EmailTemplate.newBuilder()
                .setEmailFields(EmailFields.newBuilder()
                        .setSubject("Hi there, {{name}}")
                        .setPlainTextBody("Hope you're enjoying our store in {{location}} [[ua-unsubscribe href=\\\"http://unsubscribe.urbanairship.com/email/success.html\\\"]]")
                        .build())
                .build();

        EmailChannel templateNewChannel = EmailChannel.newBuilder()
                .setAddress("new@email.com")
                .addSubstitution("name", "New Person Esq")
                .addSubstitution("location", "City, State")
                .build();

        EmailChannel templateBenChannel = EmailChannel.newBuilder()
                .setAddress("ben@icetown.com")
                .addSubstitution("name", "Ben Wyatt")
                .addSubstitution("location","Pawnee, IN")
                .build();

        EmailChannels templateChannels = EmailChannels.newBuilder()
                .addChannel(templateNewChannel)
                .addChannel(templateBenChannel)
                .build();

        CreateAndSendAudience templateAudience = new CreateAndSendAudience(templateChannels);

        templateEmailPayload = createAndSendEmailPayload.newBuilder()
                .setMessageType(MessageType.COMMERCIAL)
                .setSenderName("Urban Airship")
                .setSenderAddress("team@urbanairship.com")
                .setReplyTo("no-reply@urbanairship.com")
                .setEmailTemplate(template)
                .build();

        Notification templateNotification = Notification.newBuilder()
                .addDeviceTypeOverride(DeviceType.EMAIL, templateEmailPayload)
                .build();

        templatePayload = CreateAndSendPayload.newBuilder()
                .setAudience(templateAudience)
                .setNotification(templateNotification)
                .build();
    }

    @Test
    public void testCommercialOptedInChannel() throws IOException {

        String expectedNewChannelString = "{\n" +
                "        \"ua_address\": \"new@email.com\",\n" +
                "        \"ua_commercial_opted_in\": \"2018-11-29T10:34:22\"}";

        String parsedJson = CHANNEL_OBJECT_MAPPER.writeValueAsString(newChannel);
        JsonNode actual = CHANNEL_OBJECT_MAPPER.readTree(parsedJson);
        JsonNode expected = CHANNEL_OBJECT_MAPPER.readTree(expectedNewChannelString);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testTransactionalOptedInChannel() throws IOException {

        String expectedNewChannelString = "{\n" +
                "        \"ua_address\": \"ben@icetown.com\",\n" +
                "        \"ua_transactional_opted_in\": \"2018-11-29T12:45:10\"}";

        String parsedJson = CHANNEL_OBJECT_MAPPER.writeValueAsString(benChannel);
        JsonNode actual = CHANNEL_OBJECT_MAPPER.readTree(parsedJson);
        JsonNode expected = CHANNEL_OBJECT_MAPPER.readTree(expectedNewChannelString);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testNewAudience() throws IOException {

        String expectedAudienceString = "{\"create_and_send\":[{\"ua_address\":\"new@email.com\",\"ua_commercial_opted_in\":\"2018-11-29T10:34:22\"},{\"ua_address\":\"ben@icetown.com\",\"ua_transactional_opted_in\":\"2018-11-29T12:45:10\"}]}";

        String parsedJson = PUSH_OBJECT_MAPPER.writeValueAsString(audience);
        JsonNode actual = PUSH_OBJECT_MAPPER.readTree(parsedJson);
        JsonNode expected = PUSH_OBJECT_MAPPER.readTree(expectedAudienceString);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testEmailPayload() throws IOException {
        String expectedEmailpayloadString = "{\"subject\":\"Welcome to the Winter Sale! \",\"html_body\":\"<h1>Seasons Greetings</h1><p>Check out our winter deals!</p><p><a data-ua-unsubscribe=\\\"1\\\" title=\\\"unsubscribe\\\" href=\\\"http://unsubscribe.urbanairship.com/email/success.html\\\">Unsubscribe</a></p>\",\"plaintext_body\":\"Greetings! Check out our latest winter deals! [[ua-unsubscribe href=\\\"http://unsubscribe.urbanairship.com/email/success.html\\\"]]\",\"message_type\":\"commercial\",\"sender_name\":\"Urban Airship\",\"sender_address\":\"team@urbanairship.com\",\"reply_to\":\"no-reply@urbanairship.com\"}";

        String parsedJson = PUSH_OBJECT_MAPPER.writeValueAsString(createAndSendEmailPayload);
        JsonNode actual = PUSH_OBJECT_MAPPER.readTree(parsedJson);
        JsonNode expected = PUSH_OBJECT_MAPPER.readTree(expectedEmailpayloadString);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testNotification() throws IOException {
        String expectedNewNotificationString = "{\"email\":{\"subject\":\"Welcome to the Winter Sale! \",\"html_body\":\"<h1>Seasons Greetings</h1><p>Check out our winter deals!</p><p><a data-ua-unsubscribe=\\\"1\\\" title=\\\"unsubscribe\\\" href=\\\"http://unsubscribe.urbanairship.com/email/success.html\\\">Unsubscribe</a></p>\",\"plaintext_body\":\"Greetings! Check out our latest winter deals! [[ua-unsubscribe href=\\\"http://unsubscribe.urbanairship.com/email/success.html\\\"]]\",\"message_type\":\"commercial\",\"sender_name\":\"Urban Airship\",\"sender_address\":\"team@urbanairship.com\",\"reply_to\":\"no-reply@urbanairship.com\"}}";

        String parsedJson = PUSH_OBJECT_MAPPER.writeValueAsString(notification);
        JsonNode actual = PUSH_OBJECT_MAPPER.readTree(parsedJson);
        JsonNode expected = PUSH_OBJECT_MAPPER.readTree(expectedNewNotificationString);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testCreateAndSendEmailPayload() throws IOException {
        String expectedCreateAndSendEmailPayloadString = "{\n" +
                "\t\"audience\": {\n" +
                "\t\t\"create_and_send\": [{\n" +
                "\t\t\t\t\"ua_address\": \"new@email.com\",\n" +
                "\t\t\t\t\"ua_commercial_opted_in\": \"2018-11-29T10:34:22\"\n" +
                "\t\t\t},\n" +
                "\t\t\t{\n" +
                "\t\t\t\t\"ua_address\": \"ben@icetown.com\",\n" +
                "\t\t\t\t\"ua_transactional_opted_in\": \"2018-11-29T12:45:10\"\n" +
                "\t\t\t}\n" +
                "\t\t]\n" +
                "\t},\n" +
                "\t\"device_types\": [\"email\"],\n" +
                "\t\"notification\": {\n" +
                "\t\t\"email\": {\n" +
                "\t\t\t\"subject\": \"Welcome to the Winter Sale! \",\n" +
                "\t\t\t\"html_body\": \"<h1>Seasons Greetings</h1><p>Check out our winter deals!</p><p><a data-ua-unsubscribe=\\\"1\\\" title=\\\"unsubscribe\\\" href=\\\"http://unsubscribe.urbanairship.com/email/success.html\\\">Unsubscribe</a></p>\",\n" +
                "\t\t\t\"plaintext_body\": \"Greetings! Check out our latest winter deals! [[ua-unsubscribe href=\\\"http://unsubscribe.urbanairship.com/email/success.html\\\"]]\",\n" +
                "\t\t\t\"message_type\": \"commercial\",\n" +
                "\t\t\t\"sender_name\": \"Urban Airship\",\n" +
                "\t\t\t\"sender_address\": \"team@urbanairship.com\",\n" +
                "\t\t\t\"reply_to\": \"no-reply@urbanairship.com\"\n" +
                "\t\t}\n" +
                "\t},\n" +
                "\t\"campaigns\": {\n" +
                "\t\t\"categories\": [\"winter sale\", \"west coast\"]\n" +
                "\t}\n" +
                "}";

        String parsedJson = PUSH_OBJECT_MAPPER.writeValueAsString(payload);
        JsonNode actual = PUSH_OBJECT_MAPPER.readTree(parsedJson);
        JsonNode expected = PUSH_OBJECT_MAPPER.readTree(expectedCreateAndSendEmailPayloadString);

        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testCreateAndSendEmailTemplate() throws IOException {
        String templateFieldsString = "{\n" +
                "    \"audience\": {\n" +
                "        \"create_and_send\": [\n" +
                "            {\n" +
                "                \"ua_address\": \"new@email.com\",\n" +
                "                \"name\": \"New Person Esq\",\n" +
                "                \"location\": \"City, State\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"ua_address\": \"ben@icetown.com\",\n" +
                "                \"name\": \"Ben Wyatt\",\n" +
                "                \"location\": \"Pawnee, IN\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    \"device_types\": [\n" +
                "        \"email\"\n" +
                "    ],\n" +
                "    \"notification\": {\n" +
                "        \"email\": {\n" +
                "            \"message_type\": \"commercial\",\n" +
                "            \"sender_name\": \"Urban Airship\",\n" +
                "            \"sender_address\": \"team@urbanairship.com\",\n" +
                "            \"reply_to\": \"no-reply@urbanairship.com\",\n" +
                "            \"template\": {\n" +
                "                \"fields\": {\n" +
                "                    \"plaintext_body\": \"Hope you're enjoying our store in {{location}} [[ua-unsubscribe href=\\\\\\\"http://unsubscribe.urbanairship.com/email/success.html\\\\\\\"]]\",\n" +
                "                    \"subject\": \"Hi there, {{name}}\"\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        JsonNode actual = null;
        JsonNode expected = null;

        String parsedJson = PUSH_OBJECT_MAPPER.writeValueAsString(templatePayload);
        actual = PUSH_OBJECT_MAPPER.readTree(parsedJson);
        expected = PUSH_OBJECT_MAPPER.readTree(templateFieldsString);

        Assert.assertEquals(expected, actual);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmailTemplateIdAndFieldsBeingSet() {
        EmailTemplate emailTemplate = EmailTemplate.newBuilder()
                .setTemplateId("templateId")
                .setEmailFields(EmailFields.newBuilder()
                        .setPlainTextBody("plainText")
                        .setSubject("subject")
                        .build())
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testEmailTemplateIdAndFieldsNullValues() {
        EmailTemplate emailTemplate = EmailTemplate.newBuilder()
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubjectAndTemplateNotNull() {
        EmailTemplate emailTemplate = EmailTemplate.newBuilder()
                .setTemplateId("template_id")
                .build();

        CreateAndSendEmailPayload payload = CreateAndSendEmailPayload.newBuilder()
                .setMessageType(MessageType.COMMERCIAL)
                .setSenderName("Urban Airship")
                .setSubject("Welcome to the Winter Sale!")
                .setPlaintextBody("Hope you're enjoying our store in {{location}} [[ua-unsubscribe href=\\\"http://unsubscribe.urbanairship.com/email/success.html\\\"]]")
                .setSenderAddress("team@urbanairship.com")
                .setReplyTo("no-reply@urbanairship.com")
                .setEmailTemplate(emailTemplate)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSubjectAndTemplateNull() {
        CreateAndSendEmailPayload payload = CreateAndSendEmailPayload.newBuilder()
                .setMessageType(MessageType.COMMERCIAL)
                .setSenderName("Urban Airship")
                .setPlaintextBody("Hope you're enjoying our store in {{location}} [[ua-unsubscribe href=\\\"http://unsubscribe.urbanairship.com/email/success.html\\\"]]")
                .setSenderAddress("team@urbanairship.com")
                .setReplyTo("no-reply@urbanairship.com")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPlainTextAndTemplateNotNull() {
        EmailTemplate emailTemplate = EmailTemplate.newBuilder()
                .setTemplateId("template_id")
                .build();

        CreateAndSendEmailPayload payload = CreateAndSendEmailPayload.newBuilder()
                .setMessageType(MessageType.COMMERCIAL)
                .setSenderName("Urban Airship")
                .setPlaintextBody("Hope you're enjoying our store in {{location}} [[ua-unsubscribe href=\\\"http://unsubscribe.urbanairship.com/email/success.html\\\"]]")
                .setSenderAddress("team@urbanairship.com")
                .setReplyTo("no-reply@urbanairship.com")
                .setEmailTemplate(emailTemplate)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPlainTextAndTemplateNull() {
        CreateAndSendEmailPayload payload = CreateAndSendEmailPayload.newBuilder()
                .setMessageType(MessageType.COMMERCIAL)
                .setSubject("subject")
                .setSenderName("Urban Airship")
                .setSenderAddress("team@urbanairship.com")
                .setReplyTo("no-reply@urbanairship.com")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHtmlBodyAndTemplateNotNull() {
        EmailTemplate emailTemplate = EmailTemplate.newBuilder()
                .setTemplateId("template_id")
                .build();

        CreateAndSendEmailPayload payload = CreateAndSendEmailPayload.newBuilder()
                .setMessageType(MessageType.COMMERCIAL)
                .setSenderName("Urban Airship")
                .setSenderAddress("team@urbanairship.com")
                .setReplyTo("no-reply@urbanairship.com")
                .setHtmlBody("body")
                .setEmailTemplate(emailTemplate)
                .build();
    }
}

