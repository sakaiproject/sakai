if (window.self !== window.top) {
  document.body.classList = [...window.top.document.body.classList].join(' ')
  document.getElementsByTagName("html")[0].classList = window.top.document.getElementsByTagName("html")[0].classList
}
