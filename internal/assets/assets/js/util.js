
function showToast(content) {
    if (!content)
        return
    Toast(content,3)
}

function Toast(content,timeout) {
    var toastElement = document.getElementById("toast");
    // 设置Toast文本
    toastElement.innerText = content;
    // 显示Toast
    toastElement.style.display = "block";
    // 3秒后隐藏Toast
    setTimeout(function () {
        toastElement.style.display = "none";
    }, 1000*timeout);
}


function showLoding() {
    $('#loadingModal').modal('show');
}
function hideLoding() {
    $('#loadingModal').modal('hide');
}

function formatBytes(bytes, decimals = 2) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const dm = decimals < 0 ? 0 : decimals;
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(dm)) + ' ' + sizes[i];
}


function defaultHandle(event){
    var dt = event.dataTransfer;
    var files = dt.files;
    if (files.length > 0) {
        // alert("文件已拖拽上传！");
        handleFiles(files)
    }
}

function testHandle() {
    const items = event.dataTransfer.items;
    const formData = new FormData();
    for (let i = 0; i < items.length; i++) {
        const item = items[i].webkitGetAsEntry();
        if (item) {
            for (let i = 0; i < items.length; i++) {
                const item = items[i].webkitGetAsEntry();
                if (item) {
                    traverseFileTree(item, '');
                    console.log('form',formData)
                }
            }
        }
        console.log('form',formData)
    }
    console.log('form',formData)
    function traverseFileTree(item, path) {
        if (item.isFile) {
            item.file((file) => {
                const fullPath = path ? `${path}/${file.name}` : file.name;
                formData.append('files[]', file, fullPath);
                console.log('form',formData)
            });
        } else if (item.isDirectory) {
            const dirReader = item.createReader();
            dirReader.readEntries((entries) => {
                for (let i = 0; i < entries.length; i++) {
                    traverseFileTree(entries[i], path ? `${path}/${item.name}` : item.name);
                }
            });
        }
    }
}


function detectBrowser() {
    var userAgent = navigator.userAgent;

    if (userAgent.match(/firefox/i)) {
        return 'Firefox';
    } else if (userAgent.match(/chrome|chromium/i)) {
        return 'Chrome';
    } else if (userAgent.match(/safari/i)) {
        return 'Safari';
    } else if (userAgent.match(/edge/i)) {
        return 'Edge';
    } else if (userAgent.match(/trident/i)) {
        return 'Internet Explorer';
    } else if (userAgent.match(/opera|opr/i)) {
        return 'Opera';
    } else {
        return 'Other';
    }
}

var browser = detectBrowser();
console.log('Browser: ' + browser);