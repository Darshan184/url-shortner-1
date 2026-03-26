package url_shortner;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.google.gson.Gson;
import java.util.Map;

public class ApiResponse {
    private static final Gson gson = new Gson();
    public static APIGatewayProxyResponseEvent format(int statusCode, Object body) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(Map.of("Content-Type", "application/json"))
                .withBody(gson.toJson(body));
    }
}