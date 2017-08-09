[8]: https://github.com/AKSW/FOX/releases/tag/v2.3.0
[9]: https://github.com/AKSW/FOX
[5]: ./requests.md
[6]: ./requirements.md
[7]: ../docker

# Version ([2.4.0][9])



## [requirements][6]

## [API and request examples][5]

## [docker][7]
The docker files are in the `Docker` folder.

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

<!--
## Old Version ([2.3.0][8])

### Build:

Copy `fox.properties-dist` to `fox.properties` and run `./build.sh`.

Now, the release is ready in the `release` folder, `cd release`.

### Run:

Copy `fox.properties-dist` to `fox.properties` and run `run.sh`  to start the server.

To close the server, run `close.sh`.
-->
