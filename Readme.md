## action-scheduler
The target of this service is to be able to define generic actions and to schedule
those by using JSON.</br>
It is possible to define custom actions. It may become quite complicated according to the problem. At the moment you
can schedule *HttpRequests* with *JSON* content and chain them as deep as needed.

At the moment there is no authentication and https built in but it will be added in future.
#### Routes

| Route                   | Input                                | Effect 
|-------------------------|--------------------------------------|------------------
| POST /schedule          | Schedule with one or more actions    | Stores a single schedule in the db and schedules it
| POST /schedule/execute  | Schedule with one or more actions    | Stores a single schedule in the db, schedules it, and executes it once
| POST /schedules         | Array of schedules with one or more  | Stores an array of schedules in the db and schedules all
| POST /schedules/execute | Array of schedules with one or more  | Stores an array of schedules in the db, schedules all and executes all
| POST /test/action       | One or a chain of actions            | Executes a given action or chain of actions (Just for testing purposes).

### A simple HTTP-Request-Action
First a use-case will be shown to implement it with the action-scheduler.
A user-service which manages user-data in a profile like manner, needs to be tested regularly.
Therefore some test data is needed. Step 1 is getting some data and then post it to the service.

To *GET* user-data the service of [Fake-User-Data-Api](https://randomuser.me/api/) is used. A simple *GET* request looks like:

```json
{
    "type" : "HttpRequestAction",
    "name" : "Get some Random Person",
    "method" : "GET",
    "url" : "https://randomuser.me/api/",
    "body": ""
  }
```

The type defines which *Action* will be used at runtime, the **name** can be freely chosen the **method** and **url** are defined as stated in RFC2616 with a
small limitation regarding to the methods. Only a subset is available GET, POST, PUT, DELETE and HEAD.

The aquired data will now be posted to the service (assumed at localhost/person). Therefore:
```json
{
    "type" : "HttpRequestAction",
    "name" : "Get some Random Person",
    "method" : "GET",
    "url" : "https://randomuser.me/api/",
    "body": "",
    "next":
    [
        {
            "type" : "HttpRequestAction",
            "name" : "Post the random data",
            "method" : "POST",
            "url" : "https://localhost/person",
            "body": "",
            "mappings": 
            [
                {
                    "from": "response.body",
                    "to": {"field": "body" }
                }
            ]
        }
    ]
}
```

The *POST* request is put after the *GET* request from the previous json. To acquire this it needs to be put
into the **next** array. It is quite simple to understand despite the **mappings** part. </br>
Basically a *mapping* will take some *data* from the previous request/response and apply that *data* to the
actual request. In this case it will take the *response body* from the *GET* request and put it into the *body* of
the *POST* request. It is also possible to use more complex functions here -- but everything at its time.
The **from** field can be filled with **request.body**, **request.param**, **request.header**, **response.body** and **response.header**.
And you can set the **to** field to **body**, **header** and **param**.

\\TODO example with mapping-functions; example with params/headers; example with scheduling;

### Basic Model
The whole model centers around the AbstractAction class. It defines a generic Action. And contains
the type, a name, an array of next actions (that are also AbstractActions) and the mappings.

\\TODO
