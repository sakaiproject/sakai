class FeedbackUtils {
  static renderTemplate(name, data, output) {
    const template = Handlebars.templates[name];
    document.getElementById(output).innerHTML = template(data);
  }

  static translate(key, options) {
    let ret = feedback.i18n[key];
    if (options !== undefined) {
      for (const prop in options.hash) {
        ret = ret.replace("{${prop}}", options.hash[prop]);
      }
    }
    return new Handlebars.SafeString(ret);
  }
}

export default FeedbackUtils;