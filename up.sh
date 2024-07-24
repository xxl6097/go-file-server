#!/bin/bash
array=()
for arg in "$@"; do
  if [[ $arg == /* ]]; then
      array+=("$arg")
  else
      absolute_path=$(realpath "$arg")
      if [ -z "$absolute_path" ]; then
         array+=("$arg")
      else
         array+=("$absolute_path")
      fi
  fi
done
size=${#array[@]}
files=""
dir=""
for i in "${!array[@]}"; do
  file=${array[$i]}
  if [ -e "$file" ]; then
    files+="-F \"file=@$file\" "
  else
     if(( (i+1) == size )); then
       dir=$file
     fi
  fi
done
if [ -z "$dir" ]; then
    dir=$(date "+%Y/%m/%d/%H/%M/%S")
fi

if [[ "$dir" =~ ^/ ]]; then
    while [[ "${dir:0:1}" == "/" ]]; do
        dir="${dir:1}"
    done
fi
cmd="curl -H $files http://127.0.0.1:8000/$dir"
echo "$cmd"
eval $cmd