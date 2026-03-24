package url_shortner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class RedirectUrlHandlerTest {

    @Mock
    private DynamoDbClient mockDdb;

    @Mock
    private Context mockContext;

    @Test
    public void testRedirectSuccess() {

        RedirectUrlHandler handler = new RedirectUrlHandler(mockDdb);
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setPathParameters(Map.of("shortId", "84f291a"));
        Map<String, AttributeValue> item = Map.of(
                "shortId", AttributeValue.fromS("84f291a"),
                "longUrl", AttributeValue.fromS("https://github.com/darshan")
        );
        GetItemResponse fakeResponse = GetItemResponse.builder().item(item).build();
        when(mockDdb.getItem(any(GetItemRequest.class))).thenReturn(fakeResponse);
        APIGatewayProxyResponseEvent result = handler.handleRequest(event, mockContext);
        assertEquals(301, result.getStatusCode());
        assertEquals("https://github.com/darshan", result.getHeaders().get("Location"));
        verify(mockDdb, times(1)).getItem(any(GetItemRequest.class));
    }

    @Test
    public void testRedirectNotFound() {
        RedirectUrlHandler handler = new RedirectUrlHandler(mockDdb);
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setPathParameters(Map.of("shortId", "nonexistent"));
        GetItemResponse emptyResponse = GetItemResponse.builder().item(null).build();
        when(mockDdb.getItem(any(GetItemRequest.class))).thenReturn(emptyResponse);
        APIGatewayProxyResponseEvent result = handler.handleRequest(event, mockContext);
        assertEquals(404, result.getStatusCode());
        assertTrue(result.getBody().contains("Short URL not found"));
    }
}