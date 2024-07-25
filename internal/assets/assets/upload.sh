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
dirName=""
for i in "${!array[@]}"; do
  file=${array[$i]}
  ## 文件或者目录是否存在
  if [ -e "$file" ]; then
    # 是否为目录
    if [ -d $file ]; then
      dirName=$(basename "$file")
      # 遍历目录下的文件，并添加到上传参数
      for f in $file/*
      do
        if [ -f "$f" ]; then
            files+="-F \"file=@$f\" "
        fi
      done
    else
      files+="-F \"file=@$file\" "
    fi
  else
     # 最后一个，那肯定就是上传目录
     if(( (i+1) == size )); then
       dir=$file
     fi
  fi
done

# dir为空
if [ -z "$dir" ]; then
    dir=$(date "+%Y/%m/%d/")
    if [ -n "$dirName" ] ; then
        dir=$(date "+%Y/")
        dir="$dir$dirName"
    fi
fi

# 校验dir前面的/
if [[ "$dir" =~ ^/ ]]; then
    while [[ "${dir:0:1}" == "/" ]]; do
        dir="${dir:1}"
    done
fi
cmd="curl -H $files http://127.0.0.1:8000/$dir"
echo "$cmd"
eval $cmd