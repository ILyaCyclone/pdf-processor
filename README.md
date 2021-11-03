# PDF Processor

Web service for processing PDF files. Receives PDF file with HTTP POST, performs specified operation and sends result
file in reply.

Operations like marge and overlay may be using additional PDF documents. Those are referred to as "attachments".  
Attachments catalog is configured on startup (see "Attachments config source").

## Usage

**Http endpoints**

`/overlay`

Overlay given PDF document pages with another PDF document page.

HTTP method: POST  
Content-type: multipart/form-data  
Parameters:

- file - PDF file to process
- attachment - name of predefined attachment PDF document for overlay
- pages - pages to overlay. Supported values: page number, "first", "last", "all", "odd", "even" or comma-separated
  mixture of any. Default is "1".

`/merge`

Merge (append or prepend) given PDF document with another PDF document.

HTTP method: POST  
Content-type: multipart/form-data  
Parameters:

- file - PDF file to process
- attachment - name of predefined attachment PDF document for merge
- position - position of PDF attachment relative to original document. Supported values: "before", "after", "both".
  Default is "before".

`/attachments`, `/attachments?text`

Display registered PDF attachments (in json or plain text format respectively).

HTTP method: POST

## Configuration

`pdf-processor.max-file-size` - max file size to accept (default: 100MB)

`pdf-processor.content-service-url` - absolute URL to content service "get" endpoint, used to get "
content-version-alias"
type attachments (default: http://localhost:7003/content3/get/)

### Attachments config source

1. Read from database (default)

For reading attachments config from database set property `pdf-processor.config.attachments-source.type`
to `database-content-version-alias`.

`pdf-processor.config.attachments-source.datasource-jndi-name` - datasource JNDI name (default: jdbc/ds_basic)

`pdf-processor.config.attachments-source.content-version-alias` - content version alias to read attachments config from.
Actual content is read from corresponding large_text_data. (default: pdf-processor.config.attachments.properties)

2. Read from file

`pdf-processor.attachments-source.file` - file system location of yml/properties file with attachments config

Example of attachments configuration:

```
pdf-processor.attachments[0].name=example-seal
pdf-processor.attachments[0].type=file
pdf-processor.attachments[0].source=/tmp/seal.pdf

pdf-processor.attachments[1].name=example-report
pdf-processor.attachments[1].type=url
pdf-processor.attachments[1].source=https://rut-miit.ru/report/public?_id=example-report&param1=1&param2=2

pdf-processor.attachments[2].name=example-content
pdf-processor.attachments[2].type=content-version-alias
pdf-processor.attachments[2].source=example-content-version-alias
```
