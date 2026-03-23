package helloworld;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

@ExtendWith(MockitoExtension.class)
public class CreateUrlHandlerTest {
    @Mock private DynamoDbClient mockDdb;

    @Test
    public void testSuccess() {
        APIGatewayProxyRequestEvent.ProxyRequestContext rc = new APIGatewayProxyRequestEvent.ProxyRequestContext();
        rc.setDomainName("test.com");
        rc.setStage("prod");
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setBody("{\"longUrl\": \"https://google.com\"}");
        event.setRequestContext(rc);
        when(mockDdb.putItem(any(PutItemRequest.class))).thenReturn(PutItemResponse.builder().build());
        CreateUrlHandler handler = new CreateUrlHandler(mockDdb);
        APIGatewayProxyResponseEvent result = handler.handleRequest(event, null);
        assertEquals(200, result.getStatusCode());
        verify(mockDdb).putItem(any(PutItemRequest.class));
    }
}