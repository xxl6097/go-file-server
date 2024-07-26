---
title: 小米9基于PixelExperience源码编译步骤
date: 2022-04-28 23:05:47
tags: [cepheus,proxy,android]
---

# Pixel Experience #

### Sync ###

```bash

# Initialize local repository
repo init -u https://github.com/PixelExperience/manifest -b twelve

# Sync
repo sync -c -j$(nproc --all) --force-sync --no-clone-bundle --no-tags
```

### Build ###

```bash

# Set up environment
$ . build/envsetup.sh

# Choose a target
$ lunch aosp_cepheus-userdebug

$ lunch aosp_cepheus-user

# Build the code
$ mka bacon -j4
```

### Turn on caching to speed up build ###
Make use of ccache if you want to speed up subsequent builds by running:
```
export USE_CCACHE=1
export CCACHE_EXEC=/usr/bin/ccache
```

and adding that line to your ~/.bashrc file. Then, specify the maximum amount of disk space you want ccache to use by typing this:

```
ccache -M 50G
```

where 50G corresponds to 50GB of cache. This needs to be run once. Anywhere from 25GB-100GB will result in very noticeably increased build speeds (for instance, a typical 1hr build time can be reduced to 20min). If you’re only building for one device, 25GB-50GB is fine. If you plan to build for several devices that do not share the same kernel source, aim for 75GB-100GB. This space will be permanently occupied on your drive, so take this into consideration.

You can also enable the optional ccache compression. While this may involve a slight performance slowdown, it increases the number of files that fit in the cache. To enable it, run:
```
ccache -o compression=true
```


### Start the build ###
Time to start building! Now, type:
```
croot
mka bacon -j$(nproc --all)
```


### Install the build ###
Assuming the build completed without errors (it will be obvious when it finishes), type the following in the terminal window the build ran in:

```
cd $OUT
```



There you’ll find all the files that were created. The two files of more interest are:

recovery.img, which is the PixelExperience recovery image.

A zip file whose name starts with ‘PixelExperience_’, which is the PixelExperience installer package.

Success! So… what’s next?
You’ve done it! Welcome to the elite club of self-builders. You’ve built your operating system from scratch, from the ground up. You are the master/mistress of your domain… and hopefully you’ve learned a bit on the way and had some fun too.



### Submitting Patches ###

Patches are always welcome! Please submit your patches to our Gerrit.

[Gerrit push guide](https://wiki.pixelexperience.org/help/submit-patch/)



```

unzip platform-tools-latest-linux.zip -d ~

# Now you have to add adb and fastboot to your PATH. Open ~/.profile and add the following:
# add Android SDK platform tools to path
if [ -d "$HOME/platform-tools" ] ; then
    PATH="$HOME/platform-tools:$PATH"
fi

curl https://storage.googleapis.com/git-repo-downloads/repo > /root/code/twelve/repo
chmod a+x /root/code/twelve/repo
curl https://storage.googleapis.com/git-repo-downloads/repo > /media/psf/aosp/twelve/repo
chmod a+x /media/psf/aosp/twelve/repo

./repo init -u https://github.com/PixelExperience/manifest -b twelve

repo init -u https://github.com/PixelExperience/manifest -b twelve


git config --global user.email "xiaoli.xia@uuxiclife.cn"
git config --global user.name "uuxia"

./repo sync -c -j$(nproc --all) --force-sync --no-clone-bundle --no-tags

# Running configuration script:
cd ~/
git clone https://github.com/akhilnarang/scripts
cd scripts
./setup/android_build_env.sh


./repo sync -c -j$(nproc --all) --force-sync --no-clone-bundle --no-tags

source build/envsetup.sh
lunch aosp_cepheus-userdebug

export USE_CCACHE=1
export CCACHE_EXEC=/usr/bin/ccache

# and adding that line to your ~/.bashrc file. Then, specify the maximum amount of disk space you want ccache to use by typing this:
ccache -M 50G

ccache -o compression=true

croot
mka bacon -j$(nproc --all)


export http_proxy=http://127.0.0.1:1087
export https_proxy=http://127.0.0.1:1087
git config --global http.proxy http://127.0.0.1:1087
git config --global https.proxy http://127.0.0.1:1087
git config --global core.gitproxy http://127.0.0.1:1087


export http_proxy=http://127.0.0.1:1080
export https_proxy=http://127.0.0.1:1080
git config --global http.proxy http://127.0.0.1:1080
git config --global https.proxy http://127.0.0.1:1080
git config --global core.gitproxy http://127.0.0.1:1080

git config --global --get http.proxy
git config --global --get https.proxy
git config --global --get core.gitproxy

git config --global --unset http.proxy
git config --global --unset https.proxy
git config --global --unset core.gitproxy


https://mirrors.tuna.tsinghua.edu.cn/git/AOSP/


https://android.googlesource.com

sudo chown -R root:root /root/cepheus





```