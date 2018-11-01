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
    enable() {
        var panel = document.getElementById("tester-panel");
        var paneltabs = document.getElementById("tester-panel-tabs");
        paneltabs.insertAdjacentHTML('beforeend', `<a id="autograder-tab" onclick="window.autograder.openAutoGrader();">Autograder</a>`);
        panel.insertAdjacentHTML('beforeend', this.agTabView);
        window.tester.infoTabs.push("autograder");
    },
    disable() {
        var agp = document.getElementById("autograder-tab");
        if (agp.classList.contains("is-active")) {
            tester.openTestCases();
        }
        agp.parentNode.removeChild(agp);
        var agpt = document.getElementById("autograder-tab-view");
        agpt.parentNode.removeChild(agpt);
        window.tester.infoTabs.splice(window.tester.infoTabs.indexOf("autograder"), 1);
    },
    openAutoGrader() {
        window.tester.openTab("autograder", window.tester.infoTabs);
    },
    agTabView: `<nav id="autograder-tab-view" class="panel" style="display: none;">
                    <div id="ag-tab-view-div" class="panel-block">ag</div>
                </nav>`,
};