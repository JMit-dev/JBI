
# Java Bluesky Interface (JBI)

This prototype provides a minimal Java REST client for interacting with the [Bluesky Queue Server](https://blueskyproject.io/bluesky-queueserver/) via the [Bluesky HTTP Server](https://blueskyproject.io/bluesky-httpserver/).

## Requirements

- Java 17+
- Maven
- Running instance of Bluesky HTTP Server (default: `http://localhost:60610`)
- API key (default is `"a"`)

## Setup

   ```sh
   # set api key as environment variable
   export BLUESKY_API_KEY=a

   # run the queue server
   start-re-manager --use-ipython-kernel=ON --zmq-publish-console=ON

   # run the http server
   QSERVER_HTTP_SERVER_SINGLE_USER_API_KEY=a uvicorn --host localhost --port 60610 bluesky_httpserver.server:app 

   # test communication
   curl -H "Authorization: ApiKey a" http://localhost:60610/api/status
   ```