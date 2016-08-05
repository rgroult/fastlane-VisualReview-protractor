# fastlane-VisualReview-protractor

This [Visual Review] protrator is able to iterate throught screenhot directory provides by [fastlane/snapshot] and send them to a Visual Review server.

**NB: This protractor need to be used with a [fasltlane/snapshot ] and a [Visual Review ] compatible version.**

_** Only iOS is supported yet**_

[Visual Review]: https://github.com/xebia/VisualReview
[Visual Review ]: https://github.com/rgroult/VisualReview
[fastlane/snapshot]: https://github.com/fastlane/fastlane/tree/master/snapshot
[fasltlane/snapshot ]: https://github.com/rgroult/fastlane

## Usage 

### Build

`mvn package`

### Run


`java -jar <path to jar>/visualreviewclient-<Vxx>.jar -server <VisualReview Server URL> -screenshots_dir <snapshot directory output dir>`

Or 

`java -jar <path to jar>/visualreviewclient-<Vxx>-jar-with-dependencies.jar -server <VisualReview Server URL> -screenshots_dir <snapshot directory output dir>`

```
Example: 

ls -lR screenshots
total 40
drwxr-xr-x  26 xxxxx  staff    884 Jun  6 15:08 en-US
drwxr-xr-x  26 xxxxx  staff    884 Jun  6 15:07 fr-FR
-rw-r--r--   1 xxxxx  staff  17537 Jun  6 15:08 screenshots.html

screenshots/en-US:
total 2768
-rw-r--r--  1 xxxxx  staff     146 Jun  6 15:08 iPhone4s-01Screen.json
-rw-r--r--  1 xxxxx  staff   83768 Jun  6 15:08 iPhone4s-01Screen.png
-rw-r--r--  1 xxxxx  staff     146 Jun  6 15:08 iPhone5-01Screen.json
-rw-r--r--  1 xxxxx  staff   83768 Jun  6 15:08 iPhone5-01Screen.png
-rw-r--r--  1 xxxxx  staff     146 Jun  6 15:08 iPhone6-01Screen.json
-rw-r--r--  1 xxxxx  staff   83768 Jun  6 15:08 iPhone6-01Screen.png

screenshots/fr-FR:
total 2832
-rw-r--r--  1 xxxxx  staff     146 Jun  6 15:08 iPhone4s-01Screen.json
-rw-r--r--  1 xxxxx  staff   83768 Jun  6 15:08 iPhone4s-01Screen.png
-rw-r--r--  1 xxxxx  staff     146 Jun  6 15:08 iPhone5-01Screen.json
-rw-r--r--  1 xxxxx  staff   83768 Jun  6 15:08 iPhone5-01Screen.png
-rw-r--r--  1 xxxxx  staff     146 Jun  6 15:08 iPhone6-01Screen.json
-rw-r--r--  1 xxxxx  staff   83768 Jun  6 15:08 iPhone6-01Screen.png


java -jar visualreviewclient-1.0-SNAPSHOT  -server http://localhost:7000 -screenshots_dir ./screenshots

started on Tue Jun 07 11:08:06 CEST 2016
Listing directory: ./screenshots
found language: en-US
Sending 01Screen for suite homepage-en-US
Sending 01Screen for suite homepage-en-US
Sending 01Screen for suite homepage-en-US
response from server when uploading screenshot: HTTP/1.1 201 Created

found language: fr-FR
Sending 01Screen for suite homepage-fr-FR
Sending 01Screen for suite homepage-fr-FR
Sending 01Screen for suite homepage-fr-FR
response from server when uploading screenshot: HTTP/1.1 201 Created
ended on Tue Jun 07 11:08:14 CEST 2016

```

#### JSON additionals info

Protractor is able to parse <screenName>.json to retrieve additional info
Current managed info:

 - **suiteName** : Protractor used it to create named suite on server
 - **mask** : Protector send it as the "mask" parameter in the VisualReview upload Screen request.
 - **resolution, version** : Protractor add them to generate the **properties** parameter for the VisualReview upload Screen request. 

All others additionals keys/Values are sent as the **meta** parameter in the VisualReview upload Screen request.

More informations about Visual Review API parameters [here].

[here]: https://github.com/xebia/VisualReview/blob/master/doc/api.md

```
Example: 
cat ./screenshots/en-US/iPhone5-01Screen.json

{"mask":{"exludeZones":[{"height":40,"x":0,"width":640,"y":0}]},"resolution":"640*1136","version":"9.3","suiteName":"homePage"}


```