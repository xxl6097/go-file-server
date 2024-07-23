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

    var dt = event.dataTransfer;
    var files = dt.files;
    if (files.length > 0) {
        alert("文件已拖拽上传！");
    }
}