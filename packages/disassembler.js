'use strict';
var venuspackage = {
  id: "disassembler",
  load: function(setting) {
      if (setting.includes("enabled")) {
          loadDecoder();
      }
  },
  unload: function(setting) {
      removeDecoder();
  }
};
function dissassemblerloadScript(url, onfail, onload) {
    var urlelm = document.getElementById(url);
    if (urlelm) {
        urlelm.parentNode.removeChild(urlelm)
    }
    var script = document.createElement('script');
    script.setAttribute("onerror", onfail);
    script.setAttribute("onload", onload);
    script.setAttribute("src", url);
    script.setAttribute("id", url);
    document.getElementsByTagName("head")[0].appendChild(script);
}
function loadDecoder(){
    dissassemblerloadScript('packages/support/decoder.js',
        `alert("Could not load the decoder! Please try to reload the page to get it to load.");` ,
        `loadScript('packages/support/venus.decoder.js', "", "");`
    );
}