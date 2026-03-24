package url_shortner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.net.URI;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Map;

public class CreateUrlHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final DynamoDbClient ddb;
    private final String tableName = System.getenv("TABLE_NAME");
    private final Gson gson = new Gson();
    @SuppressWarnings("unused")
    public CreateUrlHandler() {
        this.ddb = DynamoDbClient.create();
    }
    public CreateUrlHandler(DynamoDbClient ddb) { this.ddb = ddb; }
    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context context) {
        try {
            //If the body is not present
            if (event.getBody() == null)
                return ApiResponse.format(400, Map.of("message", "Request body is required"));
            JsonObject body = gson.fromJson(event.getBody(), JsonObject.class);
            String longUrl = body.has("longUrl") ? body.get("longUrl").getAsString() : null;
            if (longUrl == null || !isValid(longUrl)) {
                return ApiResponse.format(400, Map.of("message", "A valid URL is required"));
            }
            //Generating random bytes using Secure Random in hexadecimal format
            byte[] bytes = new byte[4];
            new SecureRandom().nextBytes(bytes);
            String shortId = HexFormat.of().formatHex(bytes);
            //Putting the shortId and longUrl
            ddb.putItem(PutItemRequest.builder().tableName(tableName)
                    .item(Map.of("shortId", AttributeValue.fromS(shortId),
                            "longUrl", AttributeValue.fromS(longUrl))).build());
            var ctx = event.getRequestContext();
            String shortUrl = "https://%s/%s/short/%s".formatted(ctx.getDomainName(), ctx.getStage(), shortId);
            return ApiResponse.format(200, Map.of("shortUrl", shortUrl));
        } catch (Exception e) {
            context.getLogger().log("Error: " + e.getMessage());
            return ApiResponse.format(500, Map.of("message", "Internal Server Error"));
        }
    }
    //To validate the URL
    private boolean isValid(String url) {
        try {
            URI.create(url).toURL();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}