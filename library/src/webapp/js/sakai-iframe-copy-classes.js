if (window.self !== window.top) {
  const observer = new MutationObserver(function() {
    if (document.body) {
      // It exists now
      document.body.classList = [...window.top.document.body.classList].join(' ')
      document.getElementsByTagName("html")[0].classList = window.top.document.getElementsByTagName("html")[0].classList

      observer.disconnect();
    }
  });
  observer.observe(document.documentElement, {childList: true});
}
