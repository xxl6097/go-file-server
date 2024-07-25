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


for i in "${!array[@]}"; do
  file=${array[$i]}
  if [ -e "$file" ]; then
    if [ -d $file ]; then
        directory_name=$(basename "$file")
        echo "目录[$directory_name]：$file"
        for f in $file/*
        do
          if [ -f "$f" ]; then
              echo "dist file:$f"
          fi
        done
    else
        echo "文件:$file"
    fi
  else
      echo "2-->$file"
  fi
done