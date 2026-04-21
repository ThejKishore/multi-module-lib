## Requirements

- Add a spring filter that would validate the incoming jwt token
- If the token is invalid, then the filter should return a 401 unauthorized response.
- If the token is valid, then the filter should extract the user detail and make a call to the azure redis cache to get the user details.
- If the redis cache is empty, then the filter should make a call to the user service to get the user details and store it in the redis cache for future use with the jwt token session id.
- Once the filter is done, setting the user details in the RequestContextHolder. 
- The user details should be available in the RequestContextHolder for the controller methods.
- ensure that the filter is added to the filter chain before the SideCarRequestContextFilter. 
- ensure that the filter is enabled only for pcf profile and disabled for azure profile.

