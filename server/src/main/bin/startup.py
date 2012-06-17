#!/usr/bin/python

import os
import sys

conf_path = ''

if len(sys.argv)>= 2:
	global conf_path 
	conf_path = sys.argv[1]
	print 'Use the config path %s' % conf_path


JAVA_OPTS='''-Dcom.sun.management.jmxremote.port=3343 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false\
	-server -Xms1g -Xmx1g -Xmn512m -Xss128K  -XX:PermSize=256m\
	-XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:ParallelGCThreads=4\
	-XX:+PrintGCDetails -XX:+PrintGCTimeStamps\
	-Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n'''

CLASSPATH='../conf:../lib/*'

exec_str = 'java %s -cp %s com.hopper.server.Main -start %s' % (JAVA_OPTS,CLASSPATH,conf_path)


print exec_str

os.system(exec_str)
