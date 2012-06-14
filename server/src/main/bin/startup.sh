conf_path=$1

echo "the conf path is $conf_path"

JMX_OPTS="-Dcom.sun.management.jmxremote.port=3343 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false"

JAVA_OPTS="-server -Xms1g -Xmx1g -Xmn512m -Xss128K  -XX:PermSize=256m"
#JAVA_OPTS="$JAVA_OPTS -XX:+UseParallelGC -XX:+UseParallelOldGC -XX:ParallelGCThreads=4"
JAVA_OPTS="$JAVA_OPTS -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:ParallelGCThreads=4"
JAVA_OPTS="$JAVA_OPTS -XX:+PrintGCDetails -XX:+PrintGCTimeStamps"
JAVA_OPTS="$JAVA_OPTS -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n"

CURRENT_DIR=`pwd`
mkdir logs
CLASSPATH="$conf_path:$CURRENT_DIR/lib/*:$CURRENT_DIR/bin/*"
LOG_PATH="logs/fos.log"

echo "start fos server"
java $JMX_OPTS $JAVA_OPTS -cp $CLASSPATH com.sdp.fos.server.FosLauncher $conf_path >> $LOG_PATH 2>&1 &