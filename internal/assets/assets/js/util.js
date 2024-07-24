
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
