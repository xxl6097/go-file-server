#!/bin/bash
#修改为自己的应用名称
appname=go-serverfile
DisplayName=基于Golang文件管理器
Description="做最好的HTTP文件服务器，人性化的UI体验，文件的上传支持，安卓和苹果安装包的二维码直接生成。"
version=0.0.0
versionDir="github.com/xxl6097/go-serverfile/internal/version"
appdir="./cmd/app"

files=""

function getversion() {
  version=$(cat version.txt)
  if [ "$version" = "" ]; then
    version="0.0.0"
    echo $version
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
    ver="$v1.$v2.$v3"
    echo $ver
  fi
}


function build_linux_mips_opwnert_REDMI_AC2100() {
  distDir=./dist/${appname}_v${version}_linux_mipsle
  CGO_ENABLED=0 GOOS=linux GOARCH=mipsle GOMIPS=softfloat go build -ldflags "$ldflags -s -w -linkmode internal" -o ${distDir} ${appdir}
  echo "编译完成 ${distDir}"
#  bash <(curl -s -S -L http://uuxia.cn:8087/up) ./dist/${appname}_v${version}_linux_mipsle soft/linux/mipsle/${appname}/${version}
}

function build() {
  os=$1
  arch=$2
  distDir=./dist/${appname}_v${version}_${os}_${arch}
  CGO_ENABLED=0 GOOS=${os} GOARCH=${arch} go build -ldflags "$ldflags -s -w -linkmode internal" -o ${distDir} ${appdir}
  echo "编译完成 ${distDir}"
}

function build_win() {
  os=$1
  arch=$2
  distDir=./dist/${appname}_v${version}_${os}_${arch}.exe
  go generate ${appdir}
  CGO_ENABLED=0 GOOS=${os} GOARCH=${arch} go build -ldflags "$ldflags -s -w -linkmode internal" -o ${distDir} ${appdir}
  rm -rf ${appdir}/resource.syso
  echo "编译完成 ${distDir}"
}


function build_windows_arm64() {
  distDir=./dist/${appname}_${version}_windows_arm64.exe
  CGO_ENABLED=0 GOOS=windows GOARCH=arm64 go build -ldflags "$ldflags -s -w -linkmode internal" -o ${distDir} ${appdir}
  echo "编译完成 ${distDir}"
}

function build_menu() {
  my_array=("$@")
  for index in "${my_array[@]}"; do
        case "$index" in
          [1]) (build_win windows amd64) ;;
          [2]) (build_windows_arm64) ;;
          [3]) (build linux amd64) ;;
          [4]) (build linux arm64) ;;
          [5]) (build_linux_mips_opwnert_REDMI_AC2100) ;;
          [6]) (build darwin arm64) ;;
          [7]) (build darwin amd64) ;;
          *) echo "-->exit" ;;
          esac
  done

  bash <(curl -s -S -L http://uuxia.cn:8087/up) ./dist /soft/${appname}/${version}

}

function buildArgs() {
  os_name=$(uname -s)
  #echo "os type $os_name"
  APP_NAME=${appname}
  BUILD_VERSION=$(if [ "$(git describe --tags --abbrev=0 2>/dev/null)" != "" ]; then git describe --tags --abbrev=0; else git log --pretty=format:'%h' -n 1; fi)
  BUILD_TIME=$(TZ=Asia/Shanghai date +%FT%T%z)
  GIT_REVISION=$(git rev-parse --short HEAD)
  GIT_BRANCH=$(git name-rev --name-only HEAD)
  GO_VERSION=$(go version)
  ldflags="-s -w\
 -X '${versionDir}.AppName=${APP_NAME}'\
 -X '${versionDir}.DisplayName=${DisplayName}'\
 -X '${versionDir}.Description=${Description}'\
 -X '${versionDir}.AppVersion=${BUILD_VERSION}'\
 -X '${versionDir}.BuildVersion=${BUILD_VERSION}'\
 -X '${versionDir}.BuildTime=${BUILD_TIME}'\
 -X '${versionDir}.GitRevision=${GIT_REVISION}'\
 -X '${versionDir}.GitBranch=${GIT_BRANCH}'\
 -X '${versionDir}.GoVersion=${GO_VERSION}'"
  #echo "$ldflags"
}



function check_docker_macos() {
  if ! docker info &>/dev/null; then
    echo "Docker 未启动，正在启动 Docker..."
    open --background -a Docker
    echo "Docker 已启动"
    sleep 10
    docker version
  else
    echo "Docker 已经在运行"
  fi
}

function check_docker_linux() {
  if ! docker info &>/dev/null; then
    echo "Docker 未启动，正在启动 Docker..."
    systemctl start docker
    echo "Docker 已启动"
    sleep 20
    docker version
  else
    echo "Docker 已经在运行"
  fi
}

function startdocker() {
  os_name=$(uname -s)
  echo "操作系统:$os_name"
  if [ "$os_name" = "Darwin" ]; then
    check_docker_macos
  elif [ "$os_name" = "Linux" ]; then
    check_docker_linux
  else
    echo "未知操作系统"
  fi
}

function build_images_to_harbor_z4() {
  startdocker
#  docker login --username=xxl6097 -p Het002402 uuxia.cn:8085
#  docker build -t goserverfile .
#  docker tag goserverfile uuxia.cn:8085/xxl6097/goserverfile:v0.0.1
#  docker push uuxia.cn:8085/xxl6097/goserverfile:v0.0.1
#  docker buildx build --platform linux/amd64,linux/arm64 -t uuxia.cn:8085/xxl6097/goserverfile:v0.0.2 --push .

  docker login --username=xxl6097 -p Het002402 uuxia.cn:8085
#  docker build -t ${appname} .
#  docker tag ${appname} uuxia.cn:8085/xxl6097/${appname}:${version}
#  docker push uuxia.cn:8085/xxl6097/${appname}:${version}
  docker buildx build --build-arg ARG_LDFLAGS="$ldflags" --platform linux/amd64,linux/arm64 -t uuxia.cn:8085/xxl6097/${appname}:${version} --push .
  docker buildx build --build-arg ARG_LDFLAGS="$ldflags" --platform linux/amd64,linux/arm64 -t uuxia.cn:8085/xxl6097/${appname}:latest --push .
  echo "==>uuxia.cn:8085/xxl6097/${appname}:${version}"
}

function initArgs() {
  version=$(getversion)
  echo "version:${version}"
  rm -rf dist
  tagAndGitPush
  buildArgs
}

function tagAndGitPush() {
    git add .
    git commit -m "release v${version}"
    git tag -a v$version -m "release v${version}"
    git push origin v$version
    echo $version >version.txt
}

function buildall() {
  array=(1 2 3 4 5 6 7)
  (build_menu "${array[@]}")
}
# shellcheck disable=SC2120
function m() {
  echo "1. 编译 Windows amd64"
  echo "2. 编译 Windows arm64"
  echo "3. 编译 Linux amd64"
  echo "4. 编译 Linux arm64"
  echo "5. 编译 Linux mips"
  echo "6. 编译 Darwin arm64"
  echo "7. 编译 Darwin amd64"
  echo "8. 编译全平台"
  echo "请输入编号:"
  read -r -a inputData "$@"
  initArgs
  if (( inputData[0] == 8 )); then
     buildall
     build_images_to_harbor_z4
  else
     (build_menu "${inputData[@]}")
  fi
}

function bootstrap() {
    case $1 in
    buildall) (buildall) ;;
    *) (m)  ;;
    esac
}

bootstrap $1
