# API

## Health
`GET /api/health`

Response:
```json
{
  "status": "ok"
}
```

## Chat stream (SSE)
`POST /api/chat/stream`

Request:
```json
{
  "message": "Hello!"
}
```

Response (SSE chunks):
```
event: chunk
data: {"content":"This "}

event: chunk
data: {"content":"is "}
```

## Ingest URL
`POST /api/ingest/url`

Request:
```json
{
  "url": "https://example.com"
}
```

Response:
```json
{
  "accepted": true
}
```

## Ingest text
`POST /api/ingest/text`

Request:
```json
{
  "sourceId": "docs/manual",
  "text": "Some text to ingest"
}
```

Response:
```json
{
  "accepted": true
}
```
