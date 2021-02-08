# Authentication
## Using a remote client
There are two end points that you can use for authentication

``` 
[VIPSLogic_URL]/rest/auth/login
```

This is a POST service where you provide the credentials like this in the request body:

``` json
{
    "username": "foo",
    "password": "bar"
}

```

**For this to be secure, VIPSLogic must be contacted over SSL.**

The response upon a successful login is with a status of 201 (created) and this example payload:

``` json
{
    "success": true,
    "UUID": "0a51facb-addd-4a6a-9222-7bf0aabb1ab8"
}

```

The UUID is valid for 30 days. To check if it's still valid, use this endpoint:

``` 
[VIPSLogic_URL]/rest/auth/uuid
```

And provide the UUID in the Authorization header. If the UUID is invalid, either because it never existed or has expired, the service returns HTTP status code 404 (Not found). If the UUID is valid, you get the basic user information, for example this:

``` json
{
    "userId": 313131,
    "email": "foo@bar.com",
    "phone": "12345678",
    "phoneCountryCode": "47",
    "firstName": "Foo",
    "lastName": "Bar",
    "preferredLocale": "nb",
    "userUuid": "0a51facb-addd-4a6a-9222-7bf0aabb1ab8",
    "organization_id": 1
}
``` 