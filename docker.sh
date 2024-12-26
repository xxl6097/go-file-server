#!/bin/bash
module=$(grep "module" go.mod | cut -d ' ' -f 2)
appname=$(basename $module)
version=0.0.0
versionDir="$module/pkg"
ldflags=""

function writeVersionGoFile() {
  if [ ! -d "./pkg" ]; then
    mkdir "./pkg"
  fi
bTime=$(date +"%Y-%m-%d %H:%M:%S")
cat <<EOF > ./pkg/version.go
package pkg

import "fmt"

var (
	AppName      string // 应用名称
	AppVersion   string // 应用版本
	BuildVersion string // 编译版本
	BuildTime    string // 编译时间
	GitRevision  string // Git版本
	GitBranch    string // Git分支
	GoVersion    string // Golang信息
)

const Build_Time = "${bTime}"

// Version 版本信息
func Version() {
	fmt.Printf("App Name:\t%s\n", AppName)
	fmt.Printf("App Version:\t%s\n", AppVersion)
	fmt.Printf("Build version:\t%s\n", BuildVersion)
	fmt.Printf("Build time:\t%s\n", BuildTime)
	fmt.Printf("Git revision:\t%s\n", GitRevision)
	fmt.Printf("Git branch:\t%s\n", GitBranch)
	fmt.Printf("Golang Version: %s\n", GoVersion)
}

EOF
}

function upgradeVersion() {
  version=$(cat version.txt)
  if [ "$version" = "" ]; then
    version="0.0.0"
  else
    v3=$(echo $version | awk -F'.' '{print($3);}')
    v2=$(echo $version | awk -F'.' '{print($2);}')
    v1=$(echo $version | awk -F'.' '{print($1);}')
    if [[ $(expr $v3 \>= 99) == 1 ]]; then
      v3=0
      if [[ $(expr $v2 \>= 99) == 1 ]]; then
        v2=0
        v1=$(expr $v1 + 1)
      else
        v2=$(expr $v2 + 1)
      fi
    else
      v3=$(expr $v3 + 1)
    fi
    version="$v1.$v2.$v3"
    echo $version > version.txt
  fi
}

function buildGoArgs() {
  APP_NAME=${appname}
  APP_VERSION=${version}
  BUILD_VERSION=$(if [ "$(git describe --tags --abbrev=0 2>/dev/null)" != "" ]; then git describe --tags --abbrev=0; else git log --pretty=format:'%h' -n 1; fi)
  BUILD_TIME=$(TZ=Asia/Shanghai date +"%Y-%m-%d %H:%M:%S")
  GIT_REVISION=$(git rev-parse --short HEAD)
  GIT_BRANCH=$(git name-rev --name-only HEAD)
  GO_VERSION=$(go version)
  ldflags="-s -w\
 -X '${versionDir}.AppName=${APP_NAME}'\
 -X '${versionDir}.AppVersion=${APP_VERSION}'\
 -X '${versionDir}.BuildVersion=${BUILD_VERSION}'\
 -X '${versionDir}.BuildTime=${BUILD_TIME}'\
 -X '${versionDir}.GitRevision=${GIT_REVISION}'\
 -X '${versionDir}.GitBranch=${GIT_BRANCH}'\
 -X '${versionDir}.GoVersion=${GO_VERSION}'"
}

function buildInCodingJenkins() {
  echo "开始在coding Jenkins构建镜像"
  echo "clife-devops-docker.pkg.coding.net/public-repository/{{DEPLOY_ENV}}/{{SERVICE_NAMES}}:{{SERVICE_VERSION}}"
  docker build --build-arg ARG_LDFLAGS="$ldflags" -t clife-devops-docker.pkg.coding.net/public-repository/{{DEPLOY_ENV}}/{{SERVICE_NAMES}}:{{SERVICE_VERSION}} -f Dockerfile  .
  docker push clife-devops-docker.pkg.coding.net/public-repository/{{DEPLOY_ENV}}/{{SERVICE_NAMES}}:{{SERVICE_VERSION}}
  echo '上传镜像到制品库完成!!!'
  ls -lh
}


function buildWithCoding() {
  docker login -u prdsl-1683373983040 -p ffd28ef40d69e45f4e919e6b109d5a98601e3acd clife-devops-docker.pkg.coding.net
  docker buildx build --build-arg ARG_LDFLAGS="$ldflags" --platform linux/amd64,linux/arm64 -t clife-devops-docker.pkg.coding.net/public-repository/prdsl/${appname}:${version} --push .
  echo "docker pull clife-devops-docker.pkg.coding.net/public-repository/prdsl/${appname}:${version}"
}

function buildWithDockerHub() {
  #这个地方登录一次就够了
  docker login -u xxl6097 -p het002402
  #docker login ghcr.io --username xxl6097 --password-stdin
#  docker build --build-arg ARG_LDFLAGS="$ldflags" -t ${appname} .
#  docker tag ${appname}:${version} xxl6097/${appname}:${version}
  docker buildx build --build-arg ARG_LDFLAGS="$ldflags" --platform linux/amd64,linux/arm64 -t xxl6097/${appname}:${version} --push .

  docker tag ${appname}:${version} xxl6097/${appname}:latest
  docker buildx build --build-arg ARG_LDFLAGS="$ldflags" --platform linux/amd64,linux/arm64 -t xxl6097/${appname}:latest --push .
  echo "docker pull xxl6097/${appname}:${version}"
}

function build_darwin_arm64() {
  CGO_ENABLED=0 GOOS=darwin GOARCH=arm64 go build -ldflags "$ldflags" -o ${appname} ./cmd/app/*.go
}

function menu() {
  #更新版本号
  upgradeVersion
  echo "1. Docker Hub镜像打包"
  echo "2. 数联Coding镜像打包"
  echo "请输入编号:"
  read index
  case "$index" in
  [1]) (buildWithDockerHub) ;;
  [2]) (buildWithCoding) ;;
  *) echo "exit" ;;
  esac
}

function main_pre() {
  #1. 获取版本号
  version=$(cat version.txt)
  #2. 构建go语言编译信息
  buildGoArgs
  #3. 在pkg下创建version.go文件
  writeVersionGoFile
}

function main() {
  main_pre
  if [ "$1" == "coding" ]; then
    buildInCodingJenkins
  else
    menu
  fi
}

main $1
