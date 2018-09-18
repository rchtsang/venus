'use strict';
var venuspackage = {
  id: "autograder",
  requires: ["tester"],
  load: function(setting) {
      if (setting.includes("init")) {
          var state = "disabled";
          if (setting.includes("enabled")) {
              state = "enabled"
          }
          console.log("AG COMMING SOON!");
          return;
      }
      if (setting.includes("enabled")) {
          if (typeof window.tester === "undefined") {
              return;
          }
          window.tester.enable();
      }
  },
  unload: function(setting) {
      if (typeof window.tester === "undefined") {
        return;
      }
      if (setting.includes("disable")) {
          window.tester.disable();
      }
      if (setting.includes("remove")) {
          window.tester = undefined;
      }
  }
};

function testerloadScript(url, onfail, onload) {
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