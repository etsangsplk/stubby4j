package by.stub.yaml;

import by.stub.builder.yaml.YamlBuilder;
import by.stub.cli.CommandLineInterpreter;
import by.stub.utils.FileUtils;
import by.stub.yaml.stubs.StubHttpLifecycle;
import by.stub.yaml.stubs.StubRequest;
import by.stub.yaml.stubs.StubResponse;
import com.google.api.client.http.HttpMethods;
import org.fest.assertions.data.MapEntry;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * @author Alexander Zagniotov
 * @since 10/6/12, 8:13 PM
 */
public class YamlParserTest {

   @Rule
   public ExpectedException expectedException = ExpectedException.none();

   private static final YamlBuilder YAML_BUILDER = new YamlBuilder();

   @BeforeClass
   public static void beforeClass() throws Exception {
      CommandLineInterpreter.parseCommandLine(new String[]{});
   }

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithNoProperties() throws Exception {

      final String yaml = YAML_BUILDER
         .newStubbedRequest()
         .withMethodPut()
         .withUrl("/invoice")
         .newStubbedResponse()
         .build();

      final List<StubHttpLifecycle> loadedHttpCycles = loadYamlToDataStore(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubResponse actualResponse = actualHttpLifecycle.getResponse();
      final List<StubResponse> actualSequence = actualResponse.getSequence();

      assertThat(actualSequence).hasSize(0);
   }

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithNoSequenceResponses() throws Exception {

      final String yaml = YAML_BUILDER
         .newStubbedRequest()
         .withMethodPut()
         .withUrl("/invoice")
         .newStubbedResponse()
         .withStatus("200")
         .build();

      final List<StubHttpLifecycle> loadedHttpCycles = loadYamlToDataStore(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubResponse actualResponse = actualHttpLifecycle.getResponse();
      final List<StubResponse> actualSequence = actualResponse.getSequence();

      assertThat(actualSequence).hasSize(0);
   }

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithOneSequenceResponse() throws Exception {

      final String sequenceResponseHeaderKey = "content-type";
      final String sequenceResponseHeaderValue = "application/json";
      final String sequenceResponseStatus = "200";
      final String sequenceResponseBody = "OK";

      final String yaml = YAML_BUILDER
         .newStubbedRequest()
         .withMethodPut()
         .withUrl("/invoice")
         .newStubbedResponse()
         .withSequenceResponse()
         .withSequenceResponseStatus(sequenceResponseStatus)
         .withSequenceResponseHeaders(sequenceResponseHeaderKey, sequenceResponseHeaderValue)
         .withSequenceResponseLiteralBody(sequenceResponseBody)
         .build();

      final List<StubHttpLifecycle> loadedHttpCycles = loadYamlToDataStore(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubResponse actualResponse = actualHttpLifecycle.getResponse();

      final MapEntry sequenceHeaderEntry = MapEntry.entry(sequenceResponseHeaderKey, sequenceResponseHeaderValue);

      assertThat(actualResponse).isInstanceOf(StubResponse.class);
      assertThat(actualResponse.getHeaders()).contains(sequenceHeaderEntry);
      assertThat(actualResponse.getStatus()).isEqualTo(sequenceResponseStatus);
      assertThat(actualResponse.getBody()).isEqualTo(sequenceResponseBody);
   }


   @Test
   public void shouldUnmarshallYamlIntoObjectTree_AndReturnResponsesInSequence_WithManySequenceResponse() throws Exception {

      final String sequenceResponseHeaderKey = "content-type";
      final String sequenceResponseHeaderValue = "application/xml";
      final String sequenceResponseStatus = "500";
      final String sequenceResponseBody = "OMFG";

      final String yaml = YAML_BUILDER
         .newStubbedRequest()
         .withMethodPut()
         .withUrl("/invoice")
         .newStubbedResponse()
         .withSequenceResponse()
         .withSequenceResponseStatus("200")
         .withSequenceResponseHeaders("content-type", "application/json")
         .withSequenceResponseLiteralBody("OK")
         .withLineBreak()
         .withSequenceResponse()
         .withSequenceResponseStatus(sequenceResponseStatus)
         .withSequenceResponseHeaders(sequenceResponseHeaderKey, sequenceResponseHeaderValue)
         .withSequenceResponseFoldedBody(sequenceResponseBody)
         .build();

      final List<StubHttpLifecycle> loadedHttpCycles = loadYamlToDataStore(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);

      final StubResponse irrelevantSequenceResponse = actualHttpLifecycle.getResponse();
      final StubResponse actualSequenceResponse = actualHttpLifecycle.getResponse();

      final MapEntry sequenceHeaderEntry = MapEntry.entry(sequenceResponseHeaderKey, sequenceResponseHeaderValue);

      assertThat(actualSequenceResponse).isInstanceOf(StubResponse.class);
      assertThat(actualSequenceResponse.getHeaders()).contains(sequenceHeaderEntry);
      assertThat(actualSequenceResponse.getStatus()).isEqualTo(sequenceResponseStatus);
      assertThat(actualSequenceResponse.getBody()).isEqualTo(sequenceResponseBody);
   }


   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithUrlAsRegex() throws Exception {

      final String url = "^/[a-z]{3}/[0-9]+/?$";
      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl(url)
         .newStubbedResponse()
         .withStatus("301").build();

      final List<StubHttpLifecycle> loadedHttpCycles = loadYamlToDataStore(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubRequest actualRequest = actualHttpLifecycle.getRequest();

      assertThat(actualRequest.getUrl()).isEqualTo(url);
   }

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithMultipleHTTPMethods() throws Exception {

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withMethodHead()
         .newStubbedResponse()
         .withStatus("301").build();

      final List<StubHttpLifecycle> loadedHttpCycles = loadYamlToDataStore(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubRequest actualRequest = actualHttpLifecycle.getRequest();

      assertThat(actualRequest.getMethod()).contains(HttpMethods.GET, HttpMethods.HEAD);
   }

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithDefaultHTTPResponseStatus() throws Exception {

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .newStubbedResponse()
         .withLiteralBody("hello").build();

      final List<StubHttpLifecycle> loadedHttpCycles = loadYamlToDataStore(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubResponse actualResponse = actualHttpLifecycle.getResponse();

      assertThat(actualResponse.getStatus()).isEqualTo(String.valueOf(200));
   }

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithFoldedPost() throws Exception {

      final String stubbedRequestPost = "{\"message\", \"Hello, this is a request post\"}";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .withFoldedPost(stubbedRequestPost)
         .newStubbedResponse()
         .withStatus("201").build();

      final List<StubHttpLifecycle> loadedHttpCycles = loadYamlToDataStore(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubRequest actualRequest = actualHttpLifecycle.getRequest();

      assertThat(actualRequest.getPost()).isEqualTo(stubbedRequestPost);
   }


   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithFileAndBodySet() throws Exception {

      expectedException.expect(IOException.class);
      expectedException.expectMessage("Could not load file from path: ../../very.big.soap.response.xml");

      final String stubbedResponseFile = "../../very.big.soap.response.xml";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .newStubbedResponse()
         .withFoldedBody("{\"message\", \"Hello, this is a request post\"}")
         .withFile(stubbedResponseFile)
         .withStatus("201").build();

      loadYamlToDataStore(yaml);
   }

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithLiteralPost() throws Exception {

      final String stubbedRequestPost = "Hello, this is a request post";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .withLiteralPost(stubbedRequestPost)
         .newStubbedResponse()
         .withStatus("201").build();

      final List<StubHttpLifecycle> loadedHttpCycles = loadYamlToDataStore(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubRequest actualRequest = actualHttpLifecycle.getRequest();

      assertThat(actualRequest.getPost()).isEqualTo(stubbedRequestPost);
   }

   @Test
   public void shouldFailUnmarshallYamlIntoObjectTree_WithJsonAsLiteralPost() throws Exception {

      expectedException.expect(IllegalArgumentException.class);
      expectedException.expectMessage("Can not set java.lang.String field by.stub.yaml.stubs.StubRequest.post to java.util.LinkedHashMap");

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .withLiteralPost("{\"message\", \"Hello, this is a request body\"}")
         .newStubbedResponse()
         .withStatus("201").build();

      loadYamlToDataStore(yaml);
   }


   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithFoldedBody() throws Exception {

      final String stubbedResponseBody = "{\"message\", \"Hello, this is a response body\"}";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .newStubbedResponse()
         .withFoldedBody(stubbedResponseBody).build();

      final List<StubHttpLifecycle> loadedHttpCycles = loadYamlToDataStore(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubResponse actualResponse = actualHttpLifecycle.getResponse();

      assertThat(actualResponse.getBody()).isEqualTo(stubbedResponseBody);
   }

   @Test
   public void shouldFailUnmarshallYamlIntoObjectTree_WithJsonAsLiteralBody() throws Exception {

      expectedException.expect(IllegalArgumentException.class);
      expectedException.expectMessage("Can not set java.lang.String field by.stub.yaml.stubs.StubResponse.body to java.util.LinkedHashMap");

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .newStubbedResponse()
         .withLiteralBody("{\"message\", \"Hello, this is a response body\"}").build();

      loadYamlToDataStore(yaml);
   }

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithLiteralBody() throws Exception {

      final String stubbedResponseBody = "This is a sentence";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .newStubbedResponse()
         .withLiteralBody(stubbedResponseBody).build();

      final List<StubHttpLifecycle> loadedHttpCycles = loadYamlToDataStore(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubResponse actualResponse = actualHttpLifecycle.getResponse();

      assertThat(actualResponse.getBody()).isEqualTo(stubbedResponseBody);
   }


   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithMultipleQueryParams() throws Exception {

      final String expectedParamOne = "paramOne";
      final String expectedParamOneValue = "one";
      final String fullQueryOne = String.format("%s=%s", expectedParamOne, expectedParamOneValue);

      final String expectedParamTwo = "paramTwo";
      final String expectedParamTwoValue = "two";
      final String fullQueryTwo = String.format("%s=%s", expectedParamTwo, expectedParamTwoValue);

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .withQuery(expectedParamOne, expectedParamOneValue)
         .withQuery(expectedParamTwo, expectedParamTwoValue)
         .newStubbedResponse()
         .withStatus("500").build();

      final List<StubHttpLifecycle> loadedHttpCycles = loadYamlToDataStore(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubRequest actualRequest = actualHttpLifecycle.getRequest();
      final MapEntry queryEntryOne = MapEntry.entry(expectedParamOne, expectedParamOneValue);
      final MapEntry queryEntryTwo = MapEntry.entry(expectedParamTwo, expectedParamTwoValue);

      assertThat(actualRequest.getUrl()).contains(fullQueryOne);
      assertThat(actualRequest.getUrl()).contains(fullQueryTwo);
      assertThat(actualRequest.getQuery()).contains(queryEntryOne, queryEntryTwo);
   }


   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WithMultipleHeaders() throws Exception {

      final String headerOneKey = "location";
      final String headerOneValue = "/invoice/123";

      final String headerTwoKey = "content-type";
      final String headerTwoValue = "application-json";

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .newStubbedResponse()
         .withHeaders(headerOneKey, headerOneValue)
         .withHeaders(headerTwoKey, headerTwoValue).build();


      final List<StubHttpLifecycle> loadedHttpCycles = loadYamlToDataStore(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubResponse actualResponse = actualHttpLifecycle.getResponse();
      final MapEntry headerOneEntry = MapEntry.entry(headerOneKey, headerOneValue);
      final MapEntry headerTwoEntry = MapEntry.entry(headerTwoKey, headerTwoValue);

      assertThat(actualResponse.getHeaders()).contains(headerOneEntry);
      assertThat(actualResponse.getHeaders()).contains(headerTwoEntry);
   }


   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WitQueryParamIsArrayHavingDoubleQuotes() throws Exception {

      final String expectedParamOne = "fruits";
      final String expectedParamOneValue = "[\"apple\",\"orange\",\"banana\"]";
      final String fullQueryOne = String.format("%s=%s", expectedParamOne, expectedParamOneValue);

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .withQuery(expectedParamOne, String.format("'%s'", expectedParamOneValue))
         .newStubbedResponse()
         .withStatus("201").build();

      final List<StubHttpLifecycle> loadedHttpCycles = loadYamlToDataStore(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubRequest actualRequest = actualHttpLifecycle.getRequest();
      final MapEntry queryEntryOne = MapEntry.entry(expectedParamOne, expectedParamOneValue);

      assertThat(actualRequest.getUrl()).contains(fullQueryOne);
      assertThat(actualRequest.getQuery()).contains(queryEntryOne);
   }

   @Test
   public void shouldUnmarshallYamlIntoObjectTree_WhenYAMLValid_WitQueryParamIsArrayHavingSingleQuotes() throws Exception {

      final String expectedParamOne = "fruits";
      final String expectedParamOneValue = "['apple','orange','banana']";
      final String fullQueryOne = String.format("%s=%s", expectedParamOne, expectedParamOneValue);

      final String yaml = YAML_BUILDER.newStubbedRequest()
         .withMethodGet()
         .withUrl("/some/uri")
         .withQuery(expectedParamOne, String.format("\"%s\"", expectedParamOneValue))
         .newStubbedResponse()
         .withStatus("201").build();

      final List<StubHttpLifecycle> loadedHttpCycles = loadYamlToDataStore(yaml);
      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(0);
      final StubRequest actualRequest = actualHttpLifecycle.getRequest();
      final MapEntry queryEntryOne = MapEntry.entry(expectedParamOne, expectedParamOneValue);

      assertThat(actualRequest.getUrl()).contains(fullQueryOne);
      assertThat(actualRequest.getQuery()).contains(queryEntryOne);
   }


   @Test
   public void loadTest_shouldUnmarshallHugeYamlIntoObjectTree_WhenYAMLValid() throws Exception {

      final String baseRequestUrl = "/some/uri";

      final String expectedHeaderKey = "location";
      final String expectedHeaderValue = "/invoice/123";

      final String expectedParamOne = "paramOne";
      final String expectedParamTwo = "paramTwo";

      final String stubbedResponseBody = "Hello, this is a response body";
      final String stubbedResponseStatus = "301";

      final int NUMBER_OF_HTTPCYCLES = 1000;
      final StringBuilder BUILDER = new StringBuilder();

      for (int idx = 1; idx <= NUMBER_OF_HTTPCYCLES; idx++) {
         String expectedUrl = String.format("%s/%s", baseRequestUrl, idx);

         String yaml = YAML_BUILDER.newStubbedRequest()
            .withMethodGet()
            .withUrl(expectedUrl)
            .withQuery(expectedParamOne, String.valueOf(idx))
            .withQuery(expectedParamTwo, String.valueOf(idx))
            .newStubbedResponse()
            .withStatus(stubbedResponseStatus)
            .withHeaders(expectedHeaderKey, expectedHeaderValue)
            .withLiteralBody(stubbedResponseBody).build();

         BUILDER.append(yaml).append("\n\n");
      }

      final List<StubHttpLifecycle> loadedHttpCycles = loadYamlToDataStore(BUILDER.toString());
      assertThat(loadedHttpCycles.size()).isEqualTo(NUMBER_OF_HTTPCYCLES);

      final StubHttpLifecycle actualHttpLifecycle = loadedHttpCycles.get(989);
      final StubRequest actualRequest = actualHttpLifecycle.getRequest();
      final StubResponse actualResponse = actualHttpLifecycle.getResponse();

      assertThat(actualRequest.getUrl()).contains(String.format("%s/%s", baseRequestUrl, 990));
      assertThat(actualRequest.getUrl()).contains(String.format("%s=%s", expectedParamOne, 990));
      assertThat(actualRequest.getUrl()).contains(String.format("%s=%s", expectedParamTwo, 990));

      final MapEntry headerEntry = MapEntry.entry(expectedHeaderKey, expectedHeaderValue);
      assertThat(actualResponse.getHeaders()).contains(headerEntry);
   }


   private List<StubHttpLifecycle> loadYamlToDataStore(final String yaml) throws Exception {

      final YamlParser yamlParser = new YamlParser();

      final Reader reader = FileUtils.constructReader(yaml);

      return yamlParser.parse(reader);
   }
}