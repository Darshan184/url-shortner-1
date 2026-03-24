This is an Assignment demonstrating URL shortner and SAM in JAVA.
The steps to be followed are

Send a POST request to the following URL--https://uepypd2429.execute-api.ap-south-1.amazonaws.com/Prod/get-url-shortner
to the below example
```json
"longUrl":"https://www.google.com"
```
You will get a shortUrl in the response body of the request.
Send a GET request to the shortUrl and you will be redirected to the original website.
