jQuery('#qrcodeCanvas').qrcode({
  text: "http://jetienne.com/"
});

Dropzone.autoDiscover = false;

function getExtention(fname) {
  return fname.slice((fname.lastIndexOf(".") - 1 >>> 0) + 2);
}

function pathJoin(parts, sep) {
  var separator = sep || '/';
  var replace = new RegExp(separator + '{1,}', 'g');
  return parts.join(separator).replace(replace, separator);
}

function getQueryString(name) {
  var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
  var r = decodeURI(window.location.search).substr(1).match(reg);
  if (r != null) return r[2].replace(/\+/g, ' ');
  return null;
}

function checkPathNameLegal(name) {
  var reg = new RegExp("[\\/:*<>|]");
  var r = name.match(reg)
  return r == null;
}

function showErrorMessage(jqXHR) {
  let errMsg = jqXHR.getResponseHeader("x-auth-authentication-message")
  if (errMsg == null) {
    errMsg = jqXHR.responseText
  }
  alert('showErrorMessage',String(jqXHR.status).concat(":", errMsg));
  console.error(errMsg)
}

function ErrorMessage(errMsg) {
  alert('Error Message',errMsg);
  console.error(errMsg)
}


var vm = new Vue({
  el: "#app",
  data: {
    user: {
      email: "",
      name: "",
    },
    item: {},
    location: window.location,
    breadcrumb: [],
    exts: [],
    showHidden: false,
    previewMode: false,
    preview: {
      filename: '',
      filetype: '',
      filesize: 0,
      contentHTML: '',
    },
    version: "loading",
    appInfo:{},
    mtimeTypeFromNow: false, // or fromNow
    auth: {},
    search: getQueryString("search"),
    files: [{
      name: "loading ...",
      path: "",
      size: "...",
      type: "dir",
    }],
    myDropzone: null,
  },
  computed: {
    computedFiles: function () {
      var that = this;
      that.preview.filename = null;
      $.ajax({
        url: '/.ext',
        method: 'GET',
        data: {
          ext: "ext"
        },
        success: function (res) {
          let lines = res.split('\n');
          let cleanedLines = lines.map(line => line.replace(/\r$/, ''));
          //console.log(cleanedLines);
          that.exts = cleanedLines;
        },
        error: function (err) {
          console.log(err)
        }
      })

      var files = this.files.filter(function (f) {
        if (f.name == 'README.md') {
          that.preview.filename = f.name;
        }
        if (!that.showHidden && f.name.slice(0, 1) === '.') {
          return false;
        }
        return true;
      });
      // console.log(this.previewFile)
      if (this.preview.filename) {
        var name = this.preview.filename; // For now only README.md
        console.log(pathJoin([location.pathname, 'README.md']))
        $.ajax({
          url: pathJoin([location.pathname, 'README.md']),
          method: 'GET',
          success: function (res) {
            var converter = new showdown.Converter({
              tables: true,
              omitExtraWLInCodeBlocks: true,
              parseImgDimensions: true,
              simplifiedAutoLink: true,
              literalMidWordUnderscores: true,
              tasklists: true,
              ghCodeBlocks: true,
              smoothLivePreview: true,
              simplifiedAutoLink: true,
              strikethrough: true,
            });

            var html = converter.makeHtml(res);
            that.preview.contentHTML = html;
          },
          error: function (err) {
            console.log(err)
          }
        })
      }

      return files;
    },
  },
  created: function () {
    this.myDropzone = new Dropzone("#upload-form", {
      paramName: "file",
      maxFilesize: 10240,
      addRemoveLinks: true,
      init: function () {
        this.on("uploadprogress", function (file, progress) {
          // console.log("File progress", progress);
        });
        this.on("complete", function (file) {
          console.log("reload file list")
          loadFileList()
        })
      }
    });
  },
  methods: {
    onDialogOpen: function (title,content,confirm){
      $('#alert-id').off('click');
      $("#alert-id").click(function() {
        if(confirm){
          confirm()
        }
        $("#file-alert-modal").modal("hide");
      });
      $("#file-alert-title").text(title);
      $("#file-alert-content").text(content);
      $("#file-alert-modal").modal("show");
    },
    getEncodePath: function (filepath) {
      return pathJoin([location.pathname].concat(filepath.split("/").map(v => encodeURIComponent(v))))
    },
    formatTime: function (timestamp) {
      var m = moment(timestamp);
      if (this.mtimeTypeFromNow) {
        return m.fromNow();
      }
      return m.format('YYYY-MM-DD HH:mm:ss');
    },
    toggleHidden: function () {
      this.showHidden = !this.showHidden;
    },
    removeAllUploads: function () {
      this.myDropzone.removeAllFiles();
    },
    parentDirectory: function (path) {
      return path.replace('\\', '/').split('/').slice(0, -1).join('/')
    },
    changeParentDirectory: function (path) {
      var parentDir = this.parentDirectory(path);
      loadFileOrDir(parentDir);
    },
    genInstallURL: function (name, noEncode) {
      var parts = [location.host];
      var pathname = decodeURI(location.pathname);
      if (!name) {
        parts.push(pathname);
      } else if (getExtention(name) == "ipa") {
        parts.push("/-/ipa/link", pathname, encodeURIComponent(name));
      } else {
        parts.push(pathname, name);
      }
      var urlPath = location.protocol + "//" + pathJoin(parts);
      return noEncode ? urlPath : encodeURI(urlPath);
    },
    genQrcode: function (name, title) {
      var urlPath = this.genInstallURL(name, true);
      $("#qrcode-title").html(title || name || location.pathname);
      $("#qrcode-link").attr("href", urlPath);
      $('#qrcodeCanvas').empty().qrcode({
        text: encodeURI(urlPath),
      });

      $("#qrcodeRight a").attr("href", urlPath);
      $("#qrcode-modal").modal("show");
    },
    genDownloadURL: function (f) {
      var search = location.search;
      var sep = search == "" ? "?" : "&"
      return location.origin + this.getEncodePath(f.name) + location.search + sep + "download=true";
    },
    clipboardDownloadURL: function (f) {
      var search = location.search;
      return location.origin + this.getEncodePath(f.name) + location.search;
    },
    shouldHaveQrcode: function (name) {
      return ['apk', 'ipa'].indexOf(getExtention(name)) !== -1;
    },
    genFileClass: function (f) {
      if (f.type == "dir") {
        if (f.name == '.git') {
          return 'fa-git-square';
        }
        return "fa-folder-open";
      }
      var ext = getExtention(f.name);
      switch (ext) {
        case "go":
        case "py":
        case "java":
        case "js":
        case "c":
        case "cpp":
        case "h":
          return "fa-file-code-o";
        case "doc":
        case "docx":
          return "fa-file-word-o";
        case "xlsx":
          return "fa-file-excel-o";
        case "ppt":
        case "pptx":
          return "fa-file-powerpoint-o";
        case "pdf":
          return "fa-file-pdf-o";
        case "zip":
        case "rar":
        case "gz":
        case "tar":
        case "tgz":
        case "7z":
          return "fa-file-zip-o";
        case "mp3":
        case "wav":
          return "fa-file-audio-o";
        case "jpg":
        case "png":
        case "gif":
        case "jpeg":
        case "tiff":
          return "fa-file-picture-o";
        case "ipa":
        case "dmg":
          return "fa-apple";
        case "mp4":
        case "mov":
          return "fa-file-video-o";
        case "apk":
          return "fa-android";
        case "exe":
          return "fa-windows";
        case "sql":
        case "db":
          return "fa-database";
        case "key":
          return "fa-key";
        case "txt":
          return "fa-file-text";
        case "css":
          return "fa-css3";
        case "html":
          return "fa-html5";
      }
      if (f.name.includes('darwin')){
        return "fa-apple";
      }else if (f.name.includes('linux')){
        return "fa-linux"
      }
      return "fa-file-text-o"
    },
    isTextFile: function (f) {
      if (f.type == "dir") {
        if (f.name == '.git') {
          return false;
        }
        return false;
      }
      if (f.name == '.ext'){
        return true;
      }
      var ext = getExtention(f.name);
      let index = this.exts.indexOf(ext); // 返回 2
      let exists = index !== -1; // 返回 true
      //console.log('js',index,exists)
      if (exists){
        return true;
      }else{
        return false;
      }
    },
    isZip: function (f) {
      var ext = getExtention(f.name);
      switch (ext) {
        case "zip":
        case "gz":
          return true;
      }
      return false;
    },
    clickFileOrDir: function (f, e) {
      var reqPath = this.getEncodePath(f.name)
      // TODO: fix here tomorrow
      if (f.type == "file") {
        window.location.href = reqPath;
        return;
      }
      loadFileOrDir(reqPath);
      e.preventDefault()
    },
    changePath: function (reqPath, e) {
      loadFileOrDir(reqPath);
      e.preventDefault()
    },
    showInfo: function (f) {
      console.log('showInfo',f,this.getEncodePath(f.name));
      $.ajax({
        url: this.getEncodePath(f.name),
        data: {
          op: "info",
        },
        method: "GET",
        success: function (res) {
          $("#file-info-title").text(f.name);
          $("#file-info-content").text(JSON.stringify(res, null, 4));
          $("#file-info-modal").modal("show");
          // console.log(JSON.stringify(res, null, 4));
        },
        error: function (jqXHR, textStatus, errorThrown) {
          showErrorMessage(jqXHR)
        }
      })
    },
    loginModel: function (f) {
      $("#login-title").text("wahaha");
      $("#login-modal").modal("show");
    },
    unZip: function (f) {
      this.onDialogOpen('Unzip File','Are you sure you want to unzip ' + f.name,()=>{
        showLoding()
        $.ajax({
          url: this.getEncodePath(f.name),
          data: {
            unzip: "true",
          },
          method: "GET",
          success: function (res) {
            showToast(f.name + ' Unzip Sucess')
            loadFileList()
            hideLoding()
          },
          error: function (jqXHR, textStatus, errorThrown) {
            showErrorMessage(jqXHR)
            hideLoding()
            showToast(f.name + ' Unzip Failed')
          }
        })
      })

    },
    loadFile: function (f) {
      console.log(f);
      this.item = Object.assign({}, f);
      showLoding()
      $.ajax({
        url: this.getEncodePath(f.name),
        data: {
          raw: "content",
        },
        method: "GET",
        success: function (res) {
          $("#file-raw-title").text(f.name);
          $("#file-raw-content").text(res);
          $("#file-raw-modal").modal("show");
          // console.log(JSON.stringify(res, null, 4));
          hideLoding()
        },
        error: function (jqXHR, textStatus, errorThrown) {
          showErrorMessage(jqXHR)
          hideLoding()
        }
      })
    },
    saveFile: function () {
      console.log('saveFile:', this.item);
      // $("#file-raw-modal").modal("dismiss");
      $('#file-raw-modal').modal('hide');
      var textToUpload = $('#file-raw-content').val();
      console.log('text',textToUpload)
      var apipath = this.getEncodePath(this.item.name) + "?putType=savefile";
      showLoding()
      $.ajax({
        url: apipath,
        dataType: "text",
        data: textToUpload,
        contentType: 'text/plain',
        method: "PUT",
        success: function (res) {
          console.log('saveFile.success', res);
          hideLoding()
          showToast(this.item.name + ' Save Sucess')
        },
        error: function (jqXHR, textStatus, errorThrown) {
          showErrorMessage(jqXHR)
          hideLoding()
          showToast(this.item.name + ' Save Failed')
        }
      })
    },
    makeDirectory: function () {
      var name = window.prompt("current path: " + location.pathname + "\nplease enter the new directory name", "")
      console.log(name)
      if (!name) {
        return
      }
      if(!checkPathNameLegal(name)) {
        alert("Name should not contains any of \\/:*<>|")
        return
      }
      $.ajax({
        url: this.getEncodePath(name),
        method: "POST",
        success: function (res) {
          console.log(res)
          loadFileList()
        },
        error: function (jqXHR, textStatus, errorThrown) {
          showErrorMessage(jqXHR)
        }
      })
    },
    deletePathConfirm: function (f, e) {
      e.preventDefault();
      if (!e.altKey) { // skip confirm when alt pressed
        // if (!window.confirm("Delete " + f.name + " ?")) {
        //   return;
        // }
        this.onDialogOpen('Delete Dialog','Are you sure you want to delete ' + f.name,()=>{
          showLoding()
          let url = this.getEncodePath(f.name)
          console.log('delete',url)
          $.ajax({
            url: url,
            method: 'DELETE',
            success: function (res) {
              loadFileList()
              hideLoding()
              showToast(f.name + ' Delete Sucess')
              console.log('delete ok',url)
            },
            error: function (jqXHR, textStatus, errorThrown) {
              hideLoding()
              showToast('Delete Failed')
              console.log('delete err',url,jqXHR)
            }
          });
        });
      }

    },
    updateBreadcrumb: function (pathname) {
      var pathname = decodeURI(pathname || location.pathname || "/");
      pathname = pathname.split('?')[0]
      var parts = pathname.split('/');
      this.breadcrumb = [];
      if (pathname == "/") {
        return this.breadcrumb;
      }
      var i = 2;
      for (; i <= parts.length; i += 1) {
        var name = parts[i - 1];
        if (!name) {
          continue;
        }
        var path = parts.slice(0, i).join('/');
        this.breadcrumb.push({
          name: name + (i == parts.length ? ' /' : ''),
          path: path
        })
      }
      return this.breadcrumb;
    },
    loadPreviewFile: function (filepath, e) {
      if (e) {
        e.preventDefault() // may be need a switch
      }
      var that = this;
      $.getJSON(pathJoin(['/-/info', location.pathname]))
          .then(function (res) {
            console.log(res);
            that.preview.filename = res.name;
            that.preview.filesize = res.size;
            return $.ajax({
              url: '/' + res.path,
              dataType: 'text',
            });
          })
          .then(function (res) {
            console.log(res)
            that.preview.contentHTML = '<pre>' + res + '</pre>';
            console.log("Finally")
          })
          .done(function (res) {
            console.log("done", res)
          });
    },
    loadAll: function () {
      // TODO: move loadFileList here
    },
    checkToken: function (sucess,over) {
      var token = localStorage.getItem('token');
      if (!token) {
        token = window.prompt("please input token", "")
        if (!token) {
          over()
          return
        }
      }
      var apipath = "/token?putType=token";
      $.ajax({
        url: apipath,
        method: "PUT",
        dataType: "text",
        contentType: 'text/plain',
        data: token,
        success: function (res) {
          console.log(res)
          localStorage.setItem('token', token);
          sucess(token)
        },
        error: function (jqXHR, textStatus, errorThrown) {
          console.log(jqXHR, textStatus, errorThrown)
          ErrorMessage(jqXHR.statusText)
          localStorage.removeItem('token')
          over()
        }
      })
    },
    checkDirectory: function (callback) {
      var that = this
      this.checkToken((token)=>{
        var directory = window.prompt("current path: " + location.pathname + "\nplease enter the directory(input ok back)", "")
        console.log('onShowDirClick',directory)
        if (!directory) {
          return
        }
        if (window.confirm('confirm change directory?\n'+ directory)) {
          //var encodePath = this.getEncodePath(directory)
          var apipath = "/showdir?putType=showdir";
          console.log('encodePath', apipath, directory)
          showLoding()
          $.ajax({
            url: apipath,
            headers: {
              'Token': token
            },
            method: "PUT",
            dataType: "text",
            contentType: 'text/plain',
            data: directory,
            success: function (res) {
              console.log(res)
              //loadFileList()
              callback(JSON.parse(res),directory)
              hideLoding()
            },
            error: function (jqXHR, textStatus, errorThrown) {
              ErrorMessage(jqXHR.statusText)
              hideLoding()
            }
          })
        }
      },()=>{
          hideLoding()
      });
    },
    onShowDirClick: function () {
      //$('.dropdown-toggle').dropdown();
      //setTimeout(this.hideLoading, 5000); // 3秒后隐藏加载框
      this.checkDirectory((res,directory)=>{
        res.files = _.sortBy(res.files, function (f) {
          var weight = f.type == 'dir' ? 1000 : 1;
          return -weight * f.mtime;
        })
        vm.files = res.files;
        vm.auth = res.auth;
        vm.updateBreadcrumb('');
      })
    },
    onTest: function () {
      showToast('wahaha')
      showLoding()
      var that = this;
      setTimeout(function () {
        hideLoding()
      },5000)
    },
  }
})

window.onpopstate = function (event) {
  if (location.search.match(/\?search=/)) {
    location.reload();
    return;
  }
  loadFileList()
}

function loadFileOrDir(reqPath) {
  let requestUri = reqPath + location.search
  var retObj = loadFileList(requestUri)
  if (retObj !== null) {
    retObj.done(function () {
      window.history.pushState({}, "", requestUri);
    });
  }

}

function loadFileList(pathname) {
  showLoding()
  var pathname = pathname || location.pathname + location.search;
  var retObj = null
  if (getQueryString("raw") !== "false") { // not a file preview
    var sep = pathname.indexOf("?") === -1 ? "?" : "&"
    retObj = $.ajax({
      url: pathname + sep + "json=true",
      dataType: "json",
      cache: false,
      success: function (res) {
        res.files = _.sortBy(res.files, function (f) {
          var weight = f.type == 'dir' ? 1000 : 1;
          return -weight * f.mtime;
        })
        vm.files = res.files;
        vm.auth = res.auth;
        vm.updateBreadcrumb(pathname);
        hideLoding()
      },
      error: function (jqXHR, textStatus, errorThrown) {
        showErrorMessage(jqXHR)
        hideLoding()
      },
    });

  }

  vm.previewMode = getQueryString("raw") == "false";
  if (vm.previewMode) {
    vm.loadPreviewFile();
  }
  return retObj
}

Vue.filter('fromNow', function (value) {
  return moment(value).fromNow();
})

Vue.filter('formatBytes', function (value) {
  var bytes = parseFloat(value);
  if (bytes < 0) return "-";
  else if (bytes < 1024) return bytes + " B";
  else if (bytes < 1048576) return (bytes / 1024).toFixed(0) + " KB";
  else if (bytes < 1073741824) return (bytes / 1048576).toFixed(1) + " MB";
  else return (bytes / 1073741824).toFixed(1) + " GB";
})

$(function () {
  $.scrollUp({
    scrollText: '', // text are defined in css
  });

  // For page first loading
  loadFileList(location.pathname + location.search)
  // update version
  // $.getJSON("/-/sysinfo", function (res) {
  //   vm.version = res.version;
  // })

  $.ajax({
    url: "/-/sysinfo",
    method: "get",
    success: function (res) {
      if (res) {
        vm.appInfo = res
      }
    }.bind(this)
  })

  var clipboard = new Clipboard('.btn');
  clipboard.on('success', function (e) {
    console.info('Action:', e.action);
    console.info('Text:', e.text);
    console.info('Trigger:', e.trigger);
    $(e.trigger)
        .tooltip('show')
        .mouseleave(function () {
          $(this).tooltip('hide');
        })

    e.clearSelection();
  });
});
