export function assertJsonResponse(client, response) {
  client.test("Status is 200 OK", function () {
    client.assert(response.status === 200, "Expected HTTP 200, got " + response.status);
  });

  client.test("Content-Type is JSON", function () {
    const contentType = response.headers.valueOf("Content-Type") || "";
    client.assert(
      contentType.includes("application/json"),
      "Expected application/json, got " + contentType
    );
  });
}
