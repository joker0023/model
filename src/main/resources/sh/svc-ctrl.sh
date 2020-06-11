#!/bin/bash

#进入脚本所在目录
cd `dirname $0`

#变量定义
APP_NAME='model'
SERVICEAPP_NAME=${APP_NAME}'.jar'
SERVICEAPP_DEPHOME='/data/webapps/'${APP_NAME}
SERVICEAPP_LOGHOME='/data/logs/'${APP_NAME}
RUN_CMD='java'
PID_FILE=${SERVICEAPP_LOGHOME}'/tpid'
CONSOLE_FILE=${SERVICEAPP_LOGHOME}'/console.log'
CHECK_COUNT=3
 
#创建应用目录
mkdir -p ${SERVICEAPP_DEPHOME}

#创建日志目录
mkdir -p ${SERVICEAPP_LOGHOME}

#进入应用所在目录（虽然都是绝对路径，但有些应用需要进入应用目录才能启动成功）
cd ${SERVICEAPP_DEPHOME}

#JVM CONFIG
JAVA_OPTS="-Xms2048m -Xmx2048m  \
-XX:PermSize=128M \
-XX:MaxPermSize=256m \
-Xss1m \
-Xmn1024m \
-XX:+AggressiveOpts \
-XX:+UseBiasedLocking \
-XX:+CMSParallelRemarkEnabled \
-XX:+UseConcMarkSweepGC \
-XX:ParallelGCThreads=2 \
-XX:SurvivorRatio=4 \
-verbose:gc \
-XX:+PrintGCDetails \
-XX:+PrintGCDateStamps \
-XX:+PrintHeapAtGC \
-Xloggc:${SERVICEAPP_LOGHOME}/gc.log \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=${SERVICEAPP_LOGHOME}/dump.logs "

#-Djava.awt.headless=true

#进程状态标识变量，1为存在，0为不存在
PID_FLAG=0

#检查服务进程是否存在
checktpid(){
	TPID=`cat ${PID_FILE} | awk '{print $1}'`
	TPID=`ps -aef | grep ${TPID} | awk '{print $2}' |grep ${TPID}`
	if [[ ${TPID} ]]
	then
	    PID_FLAG=1
	else
	    PID_FLAG=0
	fi
}

#启动服务函数
start(){
    #检查进程状态
    checktpid

    if [[ ${PID_FLAG} -ne 0 ]]
	then
        echo "WARN:$SERVICEAPP_NAME already started! Ignoring startup request."
    else
        echo "Starting $SERVICEAPP_NAME ..."
        rm -f ${PID_FILE}
        ${RUN_CMD} -jar ${JAVA_OPTS} ${SERVICEAPP_DEPHOME}/${SERVICEAPP_NAME} > ${CONSOLE_FILE} 2>&1 &
        echo $! > ${PID_FILE}
    fi
}

#关闭服务函数
stop(){
    #检查进程状态
    checktpid

    if [[ ${PID_FLAG} -ne 0 ]]
	then
        echo "Stoping $SERVICEAPP_NAME..."

		#循环检查进程3次，每次睡眠2秒
		for((i=1;i<=${CHECK_COUNT};i++))
		do
			kill ${TPID}
			sleep 2

			#检查进程状态
			checktpid

			if [[ ${PID_FLAG} -eq 0 ]]
			then
				break
			fi
		done

        #如果以上正常关闭进程都失败，则强制关闭
		if [[ ${PID_FLAG} -ne 0 ]]
		then
            echo "Stoping use kill -9..."
            kill -9 ${TPID}
            sleep 2
        else
            echo "$SERVICEAPP_NAME Stopped!"
        fi

    else
		echo "WARN:$SERVICEAPP_NAME is not runing"
    fi
}

#检测进程状态函数
status(){
    #检查进程状态
    checktpid

    if [[ ${PID_FLAG} -eq 0 ]]
	then
        echo "$SERVICEAPP_NAME is not runing"
    else
        echo "$SERVICEAPP_NAME is runing"
    fi
}


#####脚本执行入口#####
case "$1" in
    'start')
	    start
        ;;
    'stop')
        stop
        ;;
    'restart')
        stop
        start
        ;;
    'status')
        status
        ;;
    *)
    echo "Usage: $0 {start|stop|restart|status}"
    exit 1
		;;
esac

exit 0