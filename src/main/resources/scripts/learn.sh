#nohup java -Xmx8G -cp fox-${project.version}-jar-with-dependencies.jar org.aksw.fox.FoxCLI -lde -atrain -iinput/Wikiner/aij-wikiner-de-wp3.bz2 > learn.log &
nohup java -Xmx8G -cp fox-${project.version}-jar-with-dependencies.jar org.aksw.fox.FoxCLI -len -atrain -iinput/2 > learn.log &

