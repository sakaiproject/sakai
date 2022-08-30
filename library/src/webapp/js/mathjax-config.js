// Remember to update the Java version on HtmlPageFilter too.
let scriptTag = document.createElement("script");
scriptTag.type = "text/x-mathjax-config";

// window.top is needed to get the properties even from an iFrame (for DeliveryBean).
let format = (window.top.portal.mathJaxConfig.format);
let ext = [];
let input = [];
let defaultDelimiters = false;

function useDefaultFormat(){
    ext.push("tex2jax.js");
    input.push("input/TeX");
    defaultDelimiters = true;
}

if (format === undefined) {
    console.error("No property for MathJax config was specified. Using LaTeX as default.");
    useDefaultFormat();
} else {
    format.forEach(extension => {
        switch (extension) {
            case "LaTeX":
                useDefaultFormat();
                break;
            case "AsciiMath":
                ext.push("asciimath2jax.js");
                input.push("input/AsciiMath");
                break;
            default:
                console.error(extension + " is not a supported format." +
                " Check available options on Sakai default properties");
                break;
        }
    });
}

if (ext.length === 0) {
    console.error("None of the received formats match the supported ones. Using LaTeX as default.");
    useDefaultFormat();
}

let confObject = {
    extensions: ext,
    jax: [...input, "output/HTML-CSS"],
    messageStyle: "none"
}

if (defaultDelimiters) {
    confObject["tex2jax"] = { inlineMath: [['$$','$$'],['\\(','\\)']] };
}

// Add the final MathJax config as string inside the script tag to make it work.
scriptTag.innerHTML = `MathJax.Hub.Config (${JSON.stringify(confObject)});`;
document.head.appendChild(scriptTag);
