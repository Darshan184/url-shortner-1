package helloworld;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import java.util.Map;

public class RedirectUrlHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DynamoDbClient ddb;
    @SuppressWarnings("unused")
    public RedirectUrlHandler() {

        this.ddb = DynamoDbClient.create();
    }
    public RedirectUrlHandler(DynamoDbClient ddb) {

        this.ddb = ddb;
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            String shortId = event.getPathParameters().get("shortId");
            if (shortId == null)
                return ApiResponse.format(400, Map.of("message", "shortId is required"));
            var res = ddb.getItem(GetItemRequest.builder().tableName(System.getenv("TABLE_NAME"))
                    .key(Map.of("shortId", AttributeValue.fromS(shortId))).build());
            if (!res.hasItem())
                return ApiResponse.format(404, Map.of("message", "Short URL not found"));
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(301)
                    .withHeaders(Map.of("Location", res.item().get("longUrl").s()))
                    .withBody("");
        } catch (Exception e) {
            return ApiResponse.format(500, Map.of("message", "Internal Server Error"));
        }
    }
}