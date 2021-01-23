ulimit -n 24000
$JAVA_HOME/bin/java -jar -server -XX:+UseG1GC -XX:MaxGCPauseMillis=40 -Xmx3024m -Xms3024m -XX:+AlwaysPreTouch -Djava.security.egd=file:/dev/urandom -Dlog4j2.asyncLoggerRingBufferSize=10000 -Dlog4j2.asyncQueueFullPolicy=Discard -Dlog4j2.discardThreshold=FATAL -Dlog4j2.contextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector build/libs/demo-0.0.1-SNAPSHOT.jar
