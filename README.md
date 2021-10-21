# PDF Processor

## Usage

**Http endpoints**

`/overlay`

Overlays given PDF document pages with another PDF document page a.k.a attachment.

HTTP method: POST  
Content-type: multipart/form-data  
Parameters:

- file - PDF file to process
- attachment - name of predefined attachment PDF document for overlay
- pages - pages to overlay. Supported values: page number, "first", "last", "all", "odd", "even" or comma-separated
  mixture of any. Default is "1".

`/merge`

Merges (appends or prepends) given PDF document with another PDF document a.k.a attachment.

HTTP method: POST  
Content-type: multipart/form-data  
Parameters:

- file - PDF file to process
- attachment - name of predefined attachment PDF document for merge
- position - position of PDF attachment relative to original document. Supported values: "before" or "after". Default
  is "before".

## Configuration

`pdf-processor.max-file-size` - max file size to accept

`pdf-processor.attachments-config.location` - file system location of yml/properties file configuring attachments

Example:

```
pdf-processor.attachments[0].name=example-seal
pdf-processor.attachments[0].type=file
pdf-processor.attachments[0].source=/tmp/seal.pdf

pdf-processor.attachments[1].name=example-report
pdf-processor.attachments[1].type=url
pdf-processor.attachments[1].source=https://rut-miit.ru/report/public?_id=example-report&param1=1&param2=2
```