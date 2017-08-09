[4]: https://github.com/earthquakesan/fox-py
[5]: ./requests.md
[6]: ./requirements.md
[7]: ../docker
[8]: https://github.com/AKSW/FOX/releases/tag/v2.3.0
[9]: https://github.com/AKSW/FOX
[10]: ../evaluation
[11]: ../input


# Version ([2.4.0][9])

## [Requirements][6]

## [API and request examples][5]

## [Docker][7]

## Without Docker:
### setup
Copy `fox.properties-dist` to `fox.properties`.

### build
Run `ScriptBuild.sh`  to build fox.

Run `downloadAgdistis.sh` and  `downloadSpotlight.sh`.

### run

Run `ScriptRunSpotlight.sh` to start spotlight services.

Run `ScriptRun.sh` to start FOX.

Run `stop.sh` to stop FX.


## Bindings

[Python][4]

## [FOX datasets][11]

## [FOX evaluation data][10]


<!--
## Old Version ([2.3.0][8])

### Build:

Copy `fox.properties-dist` to `fox.properties` and run `./build.sh`.

Now, the release is ready in the `release` folder, `cd release`.

### Run:

Copy `fox.properties-dist` to `fox.properties` and run `run.sh`  to start the server.

To close the server, run `close.sh`.
-->
