// var upfiles = [];
let filesArray = [];
const fileList = document.getElementById('file_list');
// 监听拖拽进入事件
function dragEnter(event) {
    event.stopPropagation();
    event.preventDefault();
    // document.getElementById('headers').style.display = 'none';
    console.log('drop dragEnter')
    event.target.style.border = "2px dashed #fff";
    // event.target.style.padding = "5px";
    // event.target.style.textAlign = "center"
    // event.target.style.backgroundColor = "#fff"
    event.target.style.color = "#fff";
    event.target.style.width = "100%"
    event.target.style.height = "100%"
    // event.target.style.display = "block"

}

// 监听拖拽离开事件
function dragLeave(event) {
    event.stopPropagation();
    event.preventDefault();
    // event.target.style.border = "2px dashed #ccc";
    // event.target.removeAttribute('style')
    // document.getElementById('headers').style.display = 'block';
    event.target.style.cssText = ''
    event.target.style.width = "100%"
    event.target.style.height = "100%"
    //event.target.style.display = "none"
    console.log('drop dragLeave')
}

// 监听拖拽结束事件
function dragEnd(event) {
    // event.stopPropagation();
    // event.preventDefault();
    event.target.style.border = "2px dashed #ccc";
    console.log('drop dragEnd')
}


// 监听拖拽释放事件
function drop(event) {
    event.stopPropagation();
    event.preventDefault();
    // event.target.style.border = "2px dashed #ccc";
    // mainView.className = 'dropzone-out'
    console.log('drop release')

    // document.getElementById('mainid').style.display = 'block';
    event.target.style.cssText = ''
    event.target.style.width = "100%"
    event.target.style.height = "100%"

    // defaultHandle(event)
    handleFiles(event)
}


function traverseFileTree(item, path = '') {
    if (item.isFile) {
        item.file((file) => {
            const fullPath = path ? `${path}/${file.name}` : file.name;
            file.fullPath = fullPath; // 在文件对象中添加完整路径属性
            filesArray.push(file); // 将文件对象添加到文件数组
            const li = document.createElement('li');
            //listItem.textContent = file.name + ' - ' + formatBytes(file.size);
            li.textContent = fullPath + ' - ' + formatBytes(file.size);
            fileList.appendChild(li);
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
// 你可以在需要的时候访问 filesArray 数组，例如：
function uploadFiles() {
    showLoding()
    var path = $("#up_file_path_id").val();
    const formData = new FormData();
    filesArray.forEach(file => {
        formData.append('file', file, file.fullPath);
    });
    fetch(path, {
        method: 'POST',
        body: formData
    })
        .then(response => {
            response.text()
            $("#file-list-modal").modal("hide");
            hideLoding()
        })
        .then(data => {
            console.log('成功上传:', data);
            showToast('成功上传')
        })
        .catch(error => {
            console.error('上传失败:', error);
            showToast('上传失败')
        });
}

function processTransferItems(event) {
    const items = event.dataTransfer.items;
    filesArray = []; // 清空文件数组
    fileList.innerHTML = ''; // 清空文件列表
    for (let i = 0; i < items.length; i++) {
        const item = items[i].webkitGetAsEntry();
        if (item) {
            traverseFileTree(item);
        }
    }
}
function processTransfer(event) {
    var dt = event.dataTransfer;
    var files = dt.files;
    files.forEach(file => {
        const fullPath = path ? `${path}/${file.name}` : file.name;
        file.fullPath = fullPath; // 在文件对象中添加完整路径属性
        filesArray.push(file); // 将文件对象添加到文件数组
        const li = document.createElement('li');
        li.textContent = fullPath + ' - ' + formatBytes(file.size);
        fileList.appendChild(li);
    });
}
function handleFiles(event) {
    var browser = detectBrowser();
    console.log('Browser: ' + browser);
    switch (browser) {
        case 'Firefox':
            processTransfer(event)
            break;
        default:
            processTransferItems(event)
    }
    handleModalDialog(filesArray)
}

function handleModalDialog() {
    $('#upload-footer-id').css('display', 'block');
    $('#upload-close-id').css('display', 'block');
    $('#upload-speed-id').css('display', 'none');
    $('#modal-progress-id').css('display', 'none');

    var progressBar = $('#myProgressBar');
    progressBar.css("width", (0) + "%").attr("aria-valuenow", (0).toString());
    sucess = function (){
        console.log(new Date().toISOString(),'sucess')
        filesArray = []; // 清空文件数组
        fileList.innerHTML = ''; // 清空文件列表
        $("#file-list-modal").modal("hide");
        hideLoding()
        loadFileList()
        showToast('Upload Sucess')
    }
    failed = function (){
        console.log(new Date().toISOString(),'failed')
        filesArray = []; // 清空文件数组
        fileList.innerHTML = ''; // 清空文件列表
        $("#file-list-modal").modal("hide");
        hideLoding()
        showToast('Upload Failed')
    }

    processEvent = function (event){
        $('#modal-progress-id').css('display', 'block');
        var percentComplete = (event.loaded / event.total) * 100;
        var roundedResult = percentComplete.toFixed(1);
        progressBar.css("width", (percentComplete) + "%").attr("aria-valuenow", (percentComplete).toString());
        progressBar.find('span').text(roundedResult + '%');
        if (roundedResult >= 100){
            progressBar.find('span').text('File Saving...');
            console.log(new Date().toISOString(),'roundedResult')
        }
    }
    speedEvent = function (speedText,progress){
        progressBar.value = progress;
        $('#upload-speed-id').css('display', 'block');
        $("#upload-speed-id").text('speed：' + speedText);
    }

    $('#on-upload-ok').off('click');
    $("#on-upload-ok").click(function() {
        $('#upload-footer-id').css('display', 'none');
        $('#upload-close-id').css('display', 'none');
        var path = $("#up_file_path_id").val();
        console.log(new Date().toISOString(),'点击上传',filesArray)
        var filecount = filesArray.length
        if (filecount <= 0) {
            console.log(new Date().toISOString(),'点击上传',filesArray.length)
           return
        }
        upload(sucess,failed,processEvent,speedEvent)
    });

    $('#on-upload-cancel').off('click');
    $("#on-upload-cancel").click(function() {
        filesArray = []; // 清空文件数组
        fileList.innerHTML = ''; // 清空文件列表
    });

    //modal.style.display = 'block';
    $("#file-list-title").text('Files Upload');
    $('#file-list-modal').modal({
        backdrop: false
    });
    $("#file-list-modal").modal("show");
}


function upload(sucess,failed,processEvent,speedEvent){
    var path = $("#up_file_path_id").val();
    const formData = new FormData();
    var total_size = 0;
    filesArray.forEach(file => {
        formData.append('file', file, file.fullPath);
        total_size += file.size
    });
    console.log('total size',formatBytes(total_size))
    var xhr = new XMLHttpRequest();
    const formatSpeed = (bytesPerSecond) => {
        const kiloBytesPerSecond = bytesPerSecond / (1024*1024);
        return kiloBytesPerSecond.toFixed(2) + ' MB/s';
    };// 记录上传开始时间

    let startTime;
    let startBytes = 0;
    startTime = new Date().getTime();

    // 监听进度事件
    xhr.upload.addEventListener('progress', function (event) {
        //console.log(new Date().toISOString(),'progress',event)
        if (event.lengthComputable) {
            // var percentComplete = (event.loaded / event.total) * 100;
            // document.getElementById('progress').style.width = percentComplete + '%';
            // var roundedResult = percentComplete.toFixed(1);
            // document.getElementById('progress').textContent = roundedResult + '%';
        }
        if (processEvent){
            processEvent(event)
        }

        const currentTime = new Date().getTime();
        const elapsedSeconds = (currentTime - startTime) / 1000;
        const uploadedBytes = event.loaded;
        const speed = (uploadedBytes - startBytes) / elapsedSeconds;

        // 更新上传速度
        const speedText = formatSpeed(speed);
        // 更新进度条
        const progress = (uploadedBytes / total_size) * 100;

        // speedElement.textContent = `上传速度: ${speedText}`;
        // progressBar.value = progress;

        // 更新起始时间和字节数
        startTime = currentTime;
        startBytes = uploadedBytes;
        if (speedEvent){
            speedEvent(speedText,progress)
        }
    });

    xhr.onreadystatechange = function () {
        //console.log(new Date().toISOString(),'xhr',xhr)
        if (xhr.readyState === 1) {
            // 在这里处理loading状态，例如显示loading动画
            console.log('Loading...');
        }else if (xhr.readyState === 4) {
            if (xhr.status === 200 && xhr.response && xhr.response.length > 0) {
                // 文件上传成功
                console.log('File uploaded successfully!');
                console.log(xhr)
                sucess()
                console.log('成功了哦')
            } else {
                // 文件上传失败
                console.error('文件上传失败，请重新上传',xhr.status,xhr.statusText);
                failed()
                console.log(new Date().toISOString(),'文件上传失败，请重新上传',xhr.status,xhr.statusText)
            }
        }
    };
    var url = `${path}`
    xhr.open('POST', url, true);
    xhr.send(formData);
}
