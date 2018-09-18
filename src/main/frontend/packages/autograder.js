'use strict';
var venuspackage = {
  id: "autograder",
  requires: ["tester"],
  load: function(setting) {
      if (setting.includes("enabled")) {
        window.autograder.enable();
      }
  },
  unload: function(setting) {
      if (typeof window.autograder === "undefined") {
        return;
      }
      if (setting.includes("disable")) {
          window.autograder.disable();
      }
      if (setting.includes("remove")) {
          window.autograder = undefined;
      }
  }
};

var autograder = {
    enable: function() {
        console.log("AG WIP");
    },
    disable: function () {
        console.log("Not much to disable when there isn't anything enabled!");
    }
};