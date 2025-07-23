# Meet

## Videos
https://github.com/vladislav-iliev/meet/blob/main/media/demo.mp4

## Inside

### Scopes

Two fundamental elements - a user Session scope, and an Event scope. Each delimit the lifetime of storage components.

___

The Session scope starts just before the Login screen is shown. It encompasses the following user-related components:
* LoginRepository - contains the access tokens
* LoginRepositoryTimer - refreshes the access tokens before expiry
* UserRepository - contains the user profile
* FeedRepository - contains a flow of paging data

The Session scope is available to be recreated anytime, so as to provide a new login experience with a clean slate

___

The Event scope is created when opening an event. It contains:
* EventRepository - contains the data necessary to display an event

Whenever a new event is opened, the Event scope is recreated, so as to provide clean Event containers.

### Connectivity

An HTTP client lives during the entire application lifetime. Also with the same lifetime is a LoginRepositoryProvider - a component that serves the current Session-scoped LoginRepository to the HTTP client. Every new LoginRepository attaches itself to the Provider. When the client needs to sign outgoing requests, it fetches tokens through the Provider.