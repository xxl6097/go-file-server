#!/bin/bash
if [[ $1 == /* ]]; then
    file="-F \"file=@$1\""
else
    absolute_path=$(realpath "$1")
    file="-F \"file=@$absolute_path\""
fi
if [ $# -eq 2 ]; then
    path=$2
else
    path=$(date "+%Y/%m/%d/%H/%M/%S")
fi
if [[ "$path" =~ ^/ ]]; then
    while [[ "${path:0:1}" == "/" ]]; do
        path="${path:1}"
    done
fi
cmd="curl -H \"Authorization: Basic YWRtaW46YWRtaW4=\" $file -F token=het002402 http://127.0.0.1:8000/$path"
echo "cmd:$cmd"
eval $cmd
