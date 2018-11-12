#!/bin/bash

usage() {
        echo -e "usage: "
        echo -e "\t$0\t-a/--address <address> "
        echo -e "\t\t\t\t-p/--port <port> "
        echo -e "\t\t\t\t[-u/--unsecure-mode] "
        echo -e "\t\t\t\t<operation_file>"
        echo -e
        echo -e "\t-a/--address :\t\ttarget ip address"
        echo -e "\t-p/--port :\t\tnameservice port number"
        echo -e "\t-u/--unsecure-mode :\tactivate unsecure mode, using 2 computes for each computation"
        echo -e "\n"
}

PORT=-1
pushd $(dirname $0) > /dev/null
basepath=$(pwd)
popd > /dev/null

UNSECURE_MODE=no
POSITIONAL=()
while [[ $# -gt 0 ]]; do
	key="$1"

	case $key in
	    -p|--port)
	    PORT="$2"
	    shift # past argument
	    shift # past value
	    ;;
	    -a|--address)
	    ADDRESS="$2"
	    shift # past argument
	    shift # past value
	    ;;
	    -u|--unsecure-mode)
	    UNSECURE_MODE=yes
	    shift # past argument
	    ;;
	    *)    # unknown option
	    POSITIONAL+=("$1") # save it in an array for later
	    shift # past argument
	    ;;
	esac
done


set -- "${POSITIONAL[@]}" # restore positional parameters
FILEPATH=$1

if [ -z "$ADDRESS" ] || [ -z "$FILEPATH" ] || [ "$PORT" -lt 0 ]; then
	usage
	exit
fi

if [ $debug ]; then
	echo ADDRESS  = "${ADDRESS}"
	echo PORT  = "${PORT}"
	echo "param 1: $1"
fi

java -cp "$basepath"/loadbalancer.jar:"$basepath"/shared.jar \
  -Djava.rmi.server.codebase=file:"$basepath"/shared.jar \
  -Djava.security.policy="$basepath"/policy \
  loadbalancer.LoadBalancer "${FILEPATH}" ${ADDRESS} ${UNSECURE_MODE} ${PORT}
